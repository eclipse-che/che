package op

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
	"strconv"
	"sync/atomic"
	"time"
	"github.com/eclipse/che/exec-agent/auth"
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
)

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
	go listenForCalls(conn, channel)

	// Say hello to the client
	eventsChan <- NewEvent(ConnectedEventType, &ChannelConnected{ChannelId: chanId, Text: "Hello!"}, connectedTime)
	return nil
}

func listenForCalls(conn *websocket.Conn, channel Channel) {
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

		// Decode the message and dispatch it to an appropriate route handler
		call := &Call{}
		if err := json.Unmarshal(message, &call); err != nil {
			log.Printf("Error decoding operation call '%s', Error: %s \n", string(message), err.Error())
		} else {
			dispatchCall(call, channel)
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

func dispatchCall(call *Call, channel Channel) {
	transmitter := &Transmitter{Channel: channel, id: call.Id}

	opRoute, ok := routes.get(call.Operation)
	if !ok {
		m := fmt.Sprintf("No route for the operation '%s'", call.Operation)
		transmitter.SendError(NewError(errors.New(m), NoSuchRouteErrorCode))
		return
	}

	decodedBody, err := opRoute.DecoderFunc(call.RawBody)
	if err != nil {
		m := fmt.Sprintf("Error decoding body for the operation '%s'. Error: '%s'", call.Operation, err.Error())
		transmitter.SendError(NewError(errors.New(m), InvalidOperationBodyJsonErrorCode))
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
