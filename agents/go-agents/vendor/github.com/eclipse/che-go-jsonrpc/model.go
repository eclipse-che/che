//
// Copyright (c) 2012-2018 Red Hat, Inc.
// This program and the accompanying materials are made
// available under the terms of the Eclipse Public License 2.0
// which is available at https://www.eclipse.org/legal/epl-2.0/
//
// SPDX-License-Identifier: EPL-2.0
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

// Package jsonrpc provides lightweight implementation of JSONRPC 2.0 protocol.
// See http://www.jsonrpc.org/specification.
//
// - the implementation does not support 'Batch' operations.
// - the implementation supports 2.0 version only.
// - the implementation uses 2.0 version for those requests that do not specify the version.
package jsonrpc

import (
	"encoding/json"
	"fmt"
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
	// This error type should be returned directly from the Handle
	InvalidParamsErrorCode = -32602

	// InternalErrorCode is returned when error returned from the Route Handle is different from Error type.
	InternalErrorCode = -32603

	// TimeoutErrorCode is returned when timeout is reached where response should arrive.
	TimeoutErrorCode = -32001

	// -32000 to -32099 Reserved for implementation-defined server-errors.
)

// Request is the identified call of the method.
// Server MUST eventually reply on the response and include
// the same identifier value as the request provides.
//
// Request without id is Notification.
// Server MUST NOT reply to Notification.
type Request struct {

	// Version of this request e.g. '2.0'.
	//
	// The version field is required.
	Version string `json:"jsonrpc"`

	// Method is the name which will be proceeded by this request.
	//
	// Must not start with "rpc" + (U+002E or ASCII 46), such methods are
	// reserved for rpc internal methods and extensions.
	//
	// The method field is required.
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
	//
	// If id is set then the object is Request otherwise it's Notification.
	ID interface{} `json:"id,omitempty"`

	// Params parameters which are needed for operation execution.
	// Params are either json array or json object, for json objects
	// names of the parameters are case sensitive.
	//
	// The params field is optional.
	Params json.RawMessage `json:"params"`
}

// IsNotification tests if this request is notification(id is not set).
func (r *Request) IsNotification() bool {
	if r.ID == nil {
		return true
	} else if id, ok := r.ID.(string); ok {
		return id == ""
	} else if id, ok := r.ID.(int); ok {
		return id == 0
	}
	return false
}

// Response is a reply on a certain request, which represents the result
// of the certain operation execution.
// Response MUST provide the same identifier as the request which forced it.
type Response struct {

	// Version of this response e.g. '2.0'.
	// The version is required.
	Version string `json:"jsonrpc"`

	// The operation call identifier, will be set only
	// if the operation contains it.
	ID interface{} `json:"id,omitempty"`

	// Result is the result of the method call.
	// Result can be anything determined by the operation(method).
	// Result and Error are mutually exclusive.
	Result json.RawMessage `json:"result,omitempty"`

	// Result and Error are mutually exclusive.
	// Present only if the operation execution fails due to an error.
	Error *Error `json:"error,omitempty"`
}

// Error indicates any exceptional situation during operation execution,
// e.g an attempt to perform operation using invalid data.
type Error struct {
	error `json:"-"`

	// Code is the value indicating the certain error type.
	Code int `json:"code"`

	// Message is the description of this error.
	Message string `json:"message"`

	// Data any kind of data which provides additional
	// information about the error e.g. stack trace, error time.
	Data json.RawMessage `json:"data,omitempty"`
}

// NewArgsError creates error object from provided error and sets error code InvalidParamsErrorCode.
func NewArgsError(err error) *Error {
	return NewError(InvalidParamsErrorCode, err)
}

// NewError creates an error from the given error and code.
func NewError(code int, err error) *Error {
	return &Error{
		error:   err,
		Code:    code,
		Message: err.Error(),
	}
}

// NewErrorf creates an error from the given code and formatted message.
func NewErrorf(code int, format string, args ...interface{}) *Error {
	return NewError(code, fmt.Errorf(format, args...))
}
