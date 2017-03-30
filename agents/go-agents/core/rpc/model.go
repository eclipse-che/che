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

// Package rpc provides a lightweight implementation of jsonrpc2.0 protocol.
// The original jsonrpc2.0 specification - http://www.jsonrpc.org/specification
//
// The implementations does not fully implement the protocol
// and introduces a few modifications to its terminology in
// term of exec-agent transport needs.
//
// From the specification:
// The Client is defined as the origin of Request objects and the handler of Response objects.
// The Server is defined as the origin of Response objects and the handler of Request objects.
//
// Exec-agent serves as both, server and client as it receives
// Responses and sends Notifications at the same time.
//
// Request.
// It's a message from the physical websocket connection client to the server.
// Request(in it's origin form) is considered to be unidirectional.
// WS Client =---> WS Server.
//
// Response.
// It's a message from the the exec-agent server to a websocket client,
// indicates the result of the operation execution requested by certain request.
// Response doesn't exist without request. The response is considered to be unidirectional.
// WS Client <---= WS Server
//
// Event.
// Is a message from the exec-agent server to a websocket client, the analogue
// from the specification is Notification, which is defined as a request
// which doesn't need any response, that's also true for events.
// Events may happen periodically and don't need to be indicated by request.
// WS Client <---X WS Server
package rpc

import (
	"encoding/json"
	"time"
)

const (

	// ParseErrorCode indicates that invalid JSON was received by the server.
	ParseErrorCode = -32700

	// InvalidRequestErrorCode indicates that request object is not valid,
	// fails when route decoder can't decode params.
	InvalidRequestErrorCode = -32600

	// MethodNotFoundErrorCode indicates that there is no route for such method.
	MethodNotFoundErrorCode = -32601

	// InvalidParamsErrorCode indicates that handler parameters are considered as not valid.
	// This error type should be returned directly from the HandlerFunc
	InvalidParamsErrorCode = -32602

	// InternalErrorCode is returned when error returned from the Route HandlerFunc is different from Error type
	InternalErrorCode = -32603

	// -32000 to -32099 Reserved for implementation-defined server-errors.
)

// Request describes named operation which is called
// on the websocket client's side and performed
// on the servers's side, if appropriate Route exists.
type Request struct {

	// Version of this request e.g. '2.0'.
	Version string `json:"jsonrpc"`

	// The method name which should be proceeded by this call.
	// Usually it is dot separated resource and action e.g. 'process.start'.
	Method string `json:"method"`

	// The unique identifier of this operation request.
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
	ID interface{} `json:"id"`

	// Request data, parameters which are needed for operation execution.
	RawParams json.RawMessage `json:"params"`
}

// Response is a message from the server to the client,
// which represents the result of the certain operation execution.
// The result is sent to the client only once per operation.
type Response struct {

	// Version of this response e.g. '2.0'.
	Version string `json:"jsonrpc"`

	// The operation call identifier, will be set only
	// if the operation contains it. See 'rpc.Request.ID'
	ID interface{} `json:"id"`

	// The actual result data, the operation execution result.
	Result interface{} `json:"result,omitempty"`

	// Result and Error are mutual exclusive.
	// Present only if the operation execution fails due to an error.
	Error *Error `json:"error,omitempty"`
}

// Event is a message from the server to the client,
// which may notify client about any activity that the client is interested in.
// The difference from the 'rpc.Response' is that the event may happen periodically,
// before or even after some operation calls, while the 'rpc.Response' is more like
// result of the operation call execution, which is sent to the client immediately
// after the operation execution is done.
type Event struct {

	// Version of this notification e.g. '2.0'
	Version string `json:"jsonrpc"`

	// A type of this operation event, must be always set.
	// The type must be generally unique.
	EventType string `json:"method"`

	// Event related data.
	Body interface{} `json:"params"`
}

// Error may be returned by any of route HandlerFunc.
type Error struct {
	error `json:"-"`

	// An error code
	Code int `json:"code"`

	// A short description of the occurred error.
	Message string `json:"message"`
}

// Timed represents time identifier
type Timed struct {
	Time time.Time `json:"time"`
}

// NewEvent creates Event object by provided parameters.
// eType is type of event.
// body is main part of event that may hold information needed for the receiver of event.
func NewEvent(eType string, body interface{}) *Event {
	return &Event{
		Version:   "2.0",
		EventType: eType,
		Body:      body,
	}
}

// NewArgsError creates error object from provided error and sets error code InvalidParamsErrorCode
func NewArgsError(err error) Error {
	return NewError(err, InvalidParamsErrorCode)
}

// NewError creates error object that can be added into Response from provided error and error code
func NewError(err error, code int) Error {
	return Error{
		error:   err,
		Code:    code,
		Message: err.Error(),
	}
}
