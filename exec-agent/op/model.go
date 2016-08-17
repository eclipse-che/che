package op

import (
	"encoding/json"
	"time"
)

// Describes named operation which is called
// on the websocket client's side and performed
// on the servers's side, if appropriate Route exists.
type Call struct {

	// The operation name which should be proceeded by this call
	// usually dot separated resource and action e.g. 'process.start'.
	Operation string `json:"operation"`

	// The unique identifier of this operation call.
	// If a client needs to identify the result of the operation execution,
	// the id should be passed by the client, then it is guaranteed
	// that the client will receive the result frame with the same id.
	// The uniqueness of the identifier must be controlled by the client,
	// if client doesn't specify the identifier in the operation call,
	// the response won't contain the identifier as well.
	//
	// It is preferable to specify identifier for those calls which may
	// either validate data, or produce such information which can't be
	// identified by itself.
	Id interface{} `json:"id"`

	// Call related data, parameters which are needed for operation execution.
	RawBody json.RawMessage `json:"body"`
}

// A message from the server to the client,
// which may notify client about any activity that the client is interested in.
// The difference from the 'op.Result' is that the event may happen periodically,
// before or even after some operation calls, while the 'op.Result' is more like
// result of the operation call execution, which is sent to the client immediately
// after the operation execution is done.
type Event struct {

	// A type of this operation event, must be always set.
	// The type must be generally unique.
	EventType string `json:"type"`

	// The time corresponding to the event occurrence, must be always set.
	Time time.Time `json:"time"`

	// Event related data.
	Body interface{} `json:"body"`
}

// A message from the server to the client,
// which represents the result of the certain operation execution.
// The result is sent to the client only once per operation.
type Result struct {

	// The operation call identifier, will be set only
	// if the operation contains it. See 'op.Call.Id'
	Id interface{} `json:"id"`

	// The actual result data, the operation execution result.
	Body interface{} `json:"body"`

	// Body and Error are mutual exclusive.
	// Present only if the operation execution fails due to an error.
	Error *Error `json:"error"`
}

func NewEventNow(eType string, Body interface{}) *Event {
	return &Event{
		EventType: eType,
		Time:      time.Now(),
		Body:      Body,
	}
}

func NewEvent(eType string, Body interface{}, time time.Time) *Event {
	return &Event{
		EventType: eType,
		Time:      time,
		Body:      Body,
	}
}
