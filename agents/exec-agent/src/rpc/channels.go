//
// Copyright (c) 2012-2017 Codenvy, S.A.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Codenvy, S.A. - initial API and implementation
//

package rpc

import (
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"time"

	"github.com/eclipse/che-lib/websocket"
	"github.com/eclipse/che/agents/exec-agent/rest"
)

const (
	ConnectedEventType = "connected"
)

var (
	upgrader = websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}

	prevChanId uint64 = 0

	channels = channelsMap{items: make(map[string]Channel)}

	HttpRoutes = rest.RoutesGroup{
		"Channel Routes",
		[]rest.Route{
			{
				"GET",
				"Connect to Exec-Agent(webscoket)",
				"/connect",
				registerChannel,
			},
		},
	}

	messageHandler = jsonrpc2_0MessageHandler{}
)

// Published when websocket connection is established
// and channel is ready for interaction
type ChannelConnected struct {
	Timed
	ChannelId string `json:"channel"`
	Text      string `json:"text"`
}

// Describes channel which is websocket connection
// with additional properties required by the application
type Channel struct {
	// Unique channel identifier
	Id string `json:"id"`

	// When the connection was established
	Connected time.Time `json:"connected"`

	// the uri of the request that established this connection
	RequestURI string `json:"-"`

	// Go channel for sending events to the websocket.
	// All the events are encoded to the json messages and
	// send to websocket connection defined by this channel.
	Events chan *Event

	// Everything passed to this channel will be encoded
	// to json and send to the client.
	output chan interface{}

	// If any value is send to this channel then
	// physical connection associated with it along with
	// output channel will be immediately closed.
	drop chan bool

	// Websocket connection
	conn *websocket.Conn
}

// A struct for reading raw websocket messages
type WsMessage struct {
	err   error
	bytes []byte
}

// Handles raw messages received from websocket channel
type MessageHandler interface {
	// handles a message in implementation specific way
	handle(message *WsMessage, channel Channel)
}

// Defines lockable map for managing channels
type channelsMap struct {
	sync.RWMutex
	items map[string]Channel
}

// Gets channel by the channel id, if there is no such channel
// then returned 'ok' is false.
func GetChannel(chanId string) (Channel, bool) {
	channels.RLock()
	defer channels.RUnlock()
	item, ok := channels.items[chanId]
	return item, ok
}

// Returns all the currently registered channels.
func GetChannels() []Channel {
	channels.RLock()
	defer channels.RUnlock()
	all := make([]Channel, len(channels.items))
	idx := 0
	for _, v := range channels.items {
		all[idx] = v
		idx++
	}
	return all
}

// Drops the channel with the given id.
func DropChannel(id string) {
	if c, ok := GetChannel(id); ok {
		c.drop <- true
	}
}

// Saves the channel with the given identifier and returns true.
// If the channel with the given identifier already exists then false is returned
// and the channel is not saved.
func saveChannel(channel Channel) bool {
	channels.Lock()
	defer channels.Unlock()
	_, ok := channels.items[channel.Id]
	if ok {
		return false
	}
	channels.items[channel.Id] = channel
	return true
}

// Removes channel
func removeChannel(channel Channel) {
	channels.Lock()
	defer channels.Unlock()
	delete(channels.items, channel.Id)
}

func registerChannel(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Couldn't establish websocket connection " + err.Error())
		return nil
	}

	channel := Channel{
		Id:         "channel-" + strconv.Itoa(int(atomic.AddUint64(&prevChanId, 1))),
		Connected:  time.Now(),
		RequestURI: r.RequestURI,
		Events:     make(chan *Event),
		output:     make(chan interface{}),
		drop:       make(chan bool),
		conn:       conn,
	}
	saveChannel(channel)

	log.Printf("A new channel with id '%s' successfully opened", channel.Id)

	go transferAsJson(conn, channel.output)
	go redirectEventsToOutput(channel)
	go handleMessages(readMessages(conn), channel)

	// Say hello to the client
	channel.Events <- NewEvent(ConnectedEventType, &ChannelConnected{
		Timed:     Timed{Time: channel.Connected},
		ChannelId: channel.Id,
		Text:      "Hello!",
	})
	return nil
}

// Handles all the messages from the given channel
// until an error occurs or a drop signal is sent.
// Clears all the associated resources.
func handleMessages(messageChan chan *WsMessage, channel Channel) {
	for {
		select {
		case message := <-messageChan:
			if message.err == nil {
				messageHandler.handle(message, channel)
			} else {
				closeErr, ok := message.err.(*websocket.CloseError)
				if !ok || !isNormallyClosed(closeErr.Code) {
					log.Println("Error reading message, " + message.err.Error())
				}
				closeChannel(channel)
				return
			}
		case <-channel.drop:
			closeChannel(channel)
			return
		}
	}
}

// Closes all associated go channels(events, output, drop)
// and physical websocket connection.
func closeChannel(channel Channel) {
	close(channel.Events)
	close(channel.output)
	close(channel.drop)
	if err := channel.conn.Close(); err != nil {
		log.Println("Error closing connection, " + err.Error())
	}
	removeChannel(channel)
	log.Printf("Channel with id '%s' successfully closed", channel.Id)
}

// Reads the message from the websocket connection until error is received,
// returns the channel which should be used for reading such messages.
func readMessages(conn *websocket.Conn) chan *WsMessage {
	messagesChan := make(chan *WsMessage)
	go func() {
		for {
			_, bytes, err := conn.ReadMessage()
			messagesChan <- &WsMessage{err: err, bytes: bytes}
			if err != nil {
				close(messagesChan)
				break
			}
		}
	}()
	return messagesChan
}

func redirectEventsToOutput(channel Channel) {
	for event := range channel.Events {
		channel.output <- event
	}
}

// transfers data from channel to physical connection,
// tries to transform data to json.
func transferAsJson(conn *websocket.Conn, c chan interface{}) {
	for message := range c {
		err := conn.WriteJSON(message)
		if err != nil {
			log.Printf("Couldn't write message to the channel. Message: %T, %v", message, message)
		}
	}
}

// handles messages as jsonrpc as described by package doc
type jsonrpc2_0MessageHandler struct{}

func (h *jsonrpc2_0MessageHandler) handle(message *WsMessage, channel Channel) {
	req := &Request{}

	// try to unmarshal the request
	if err := json.Unmarshal(message.bytes, req); err != nil {
		// Respond parse error according to specification
		channel.output <- &Response{
			Version: "2.0",
			Error: &Error{
				Code:    ParseErrorCode,
				Message: "Invalid json object",
			},
		}
		log.Printf("Error decoding request '%s', Error: %s \n", string(message.bytes), err.Error())
		return
	}

	// ensure provided version is supported
	if req.Version != "" && strings.Trim(req.Version, " ") != "2.0" {
		channel.output <- &Response{
			Version: "2.0",
			Error: &Error{
				Code:    InvalidRequestErrorCode,
				Message: "'2.0' is the only supported version, use it or omit version at all",
			},
		}
		return
	}

	transmitter := &Transmitter{Channel: channel, id: req.Id}

	opRoute, ok := routes.get(req.Method)
	if !ok {
		m := fmt.Sprintf("No route for the operation '%s'", req.Method)
		transmitter.SendError(NewError(errors.New(m), MethodNotFoundErrorCode))
		return
	}

	decodedBody, err := opRoute.DecoderFunc(req.RawParams)
	if err != nil {
		m := fmt.Sprintf("Error decoding body for the operation '%s'. Error: '%s'", req.Method, err.Error())
		transmitter.SendError(NewError(errors.New(m), InvalidRequestErrorCode))
		return
	}

	if err := opRoute.HandlerFunc(decodedBody, transmitter); err != nil {
		opError, ok := err.(Error)
		if ok {
			transmitter.SendError(opError)
		} else {
			transmitter.SendError(NewError(err, InternalErrorCode))
		}
	}
}

func isNormallyClosed(code int) bool {
	return code == websocket.CloseGoingAway ||
		code == websocket.CloseNormalClosure ||
		code == websocket.CloseNoStatusReceived
}
