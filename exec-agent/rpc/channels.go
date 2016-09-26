package rpc

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/eclipse/che/exec-agent/auth"
	"github.com/eclipse/che/exec-agent/rest"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"time"
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
				"Connect to Machine-Agent(webscoket)",
				"/connect",
				registerChannel,
			},
		},
	}
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

	// Go channel for sending events to the websocket.
	// All the events are encoded to the json messages and
	// send to websocket connection defined by this channel.
	Events chan *Event

	// Everything passed to this channel will be encoded
	// to json and send to the client.
	output chan interface{}

	// Websocket connection
	conn *websocket.Conn
}

// Defines lockable map for managing channels
type channelsMap struct {
	sync.RWMutex
	items map[string]Channel
}

// Gets channel by the channel id, if there is no such channel
// then returned 'ok' is false
func GetChannel(chanId string) (Channel, bool) {
	channels.RLock()
	defer channels.RUnlock()
	item, ok := channels.items[chanId]
	return item, ok
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

func registerChannel(w http.ResponseWriter, r *http.Request) error {
	if auth.Enabled {
		if err := auth.AuthenticateOnMaster(r); err != nil {
			return err
		}
	}
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Couldn't establish websocket connection " + err.Error())
		return nil
	}

	// Generating unique channel identifier and save the connection
	// for future interactions with the API
	chanId := "channel-" + strconv.Itoa(int(atomic.AddUint64(&prevChanId, 1)))
	connectedTime := time.Now()
	outputChan := make(chan interface{})
	eventsChan := make(chan *Event)
	channel := Channel{
		Id:        chanId,
		Connected: connectedTime,
		Events:    eventsChan,
		output:    outputChan,
		conn:      conn,
	}
	saveChannel(channel)

	// Listen for the events from the server's side
	// and API calls from the channel client side
	go listenForOutputs(conn, channel)
	go redirectEventsToOutput(channel)
	go listenForRequests(conn, channel)

	// Say hello to the client
	eventsChan <- NewEvent(ConnectedEventType, &ChannelConnected{
		Timed:     Timed{Time: connectedTime},
		ChannelId: chanId,
		Text:      "Hello!",
	})
	return nil
}

func listenForRequests(conn *websocket.Conn, channel Channel) {
	for {
		// Read a message from the client
		_, message, err := conn.ReadMessage()
		if err != nil {
			if !websocket.IsCloseError(err, 1005) {
				log.Println("Error reading message, " + err.Error())
			}

			// Cleanup channel resources
			close(channel.Events)
			close(channel.output)
			err = channel.conn.Close()
			if err != nil {
				log.Println("Error closing connection, " + err.Error())
			}
			removeChannel(channel)
			break
		}

		// Decode the request and handle it by an appropriate route handler
		request := &Request{}
		if err := json.Unmarshal(message, &request); err != nil {
			// Respond parse error according to specification
			channel.output <- &Response{
				Version: "2.0",
				Error: &Error{
					Code:    ParseErrorCode,
					Message: "Invalid json object",
				},
			}
			log.Printf("Error decoding request '%s', Error: %s \n", string(message), err.Error())
		} else if request.Version != "" && strings.Trim(request.Version, " ") != "2.0" {
			channel.output <- &Response{
				Version: "2.0",
				Error: &Error{
					Code:    InvalidRequestErrorCode,
					Message: "'2.0' is the only supported version, use it or omit version at all",
				},
			}
		} else {
			handleRequest(request, channel)
		}
	}
}

func redirectEventsToOutput(channel Channel) {
	for event := range channel.Events {
		channel.output <- event
	}
}

func listenForOutputs(conn *websocket.Conn, channel Channel) {
	for message := range channel.output {
		err := conn.WriteJSON(message)
		if err != nil {
			log.Printf("Couldn't write message to the channel. Message: %T, %v", message, message)
		}
	}
}

func handleRequest(req *Request, channel Channel) {
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
