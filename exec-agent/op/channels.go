package op

import (
	"github.com/gorilla/websocket"
	"sync"
	"time"
)

const (
	ConnectedEventType = "connected"
)

var (
	channels = channelsMap{items: make(map[string]Channel)}
)

// Published when websocket connection is established
// and channel is ready for interaction
type ChannelConnected struct {
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
