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

package jsonrpctest

import (
	"encoding/json"
	"errors"
	"sync"
	"time"

	"github.com/eclipse/che-go-jsonrpc"
)

// ConnRecorder is a fake connection which records all writes
// and provides functionality to push reads.
type ConnRecorder struct {
	mutex          *sync.Mutex
	cond           *sync.Cond
	capturedWrites [][]byte
	nextChan       chan []byte
	closed         bool
}

// NewConnRecorder returns a new conn recorder.
func NewConnRecorder() *ConnRecorder {
	mx := &sync.Mutex{}
	return &ConnRecorder{
		mutex:          mx,
		cond:           sync.NewCond(mx),
		capturedWrites: make([][]byte, 0),
		nextChan:       make(chan []byte),
	}
}

// NativeConnWaitPredicate is used to wait on recorder until the condition
// behind this predicate is met.
type NativeConnWaitPredicate func(recorder *ConnRecorder) bool

// WriteCalledAtLeast is a predicate that allows to wait until write is called at
// least given number of times.
func WriteCalledAtLeast(times int) NativeConnWaitPredicate {
	return func(recorder *ConnRecorder) bool {
		return len(recorder.capturedWrites) >= times
	}
}

// ReqSent is a predicate that waits until a given method is requested.
func ReqSent(method string) NativeConnWaitPredicate {
	return func(recorder *ConnRecorder) bool {
		wLen := len(recorder.capturedWrites)
		if wLen == 0 {
			return false
		}
		for _, v := range recorder.capturedWrites {
			req := &jsonrpc.Request{}
			if err := json.Unmarshal(v, req); err != nil {
				return false
			}
			if req.Method == method {
				return true
			}
		}
		return false
	}
}

// Get returns bytes which were wrote (idx+1)th to this connection.
func (recorder *ConnRecorder) Get(idx int) []byte {
	return recorder.capturedWrites[idx]
}

// GetAll returns all captured writes.
func (recorder *ConnRecorder) GetAll() [][]byte {
	recorder.mutex.Lock()
	defer recorder.mutex.Unlock()
	cp := make([][]byte, len(recorder.capturedWrites))
	copy(cp, recorder.capturedWrites)
	return cp
}

// CloseAfter closes this request after specified timeout.
func (recorder *ConnRecorder) CloseAfter(dur time.Duration) {
	go func() {
		<-time.NewTimer(dur).C
		recorder.Close()
	}()
}

// Unmarshal parses bytes wrote (idx+1)th and applies it on given value.
func (recorder *ConnRecorder) Unmarshal(idx int, v interface{}) error {
	return json.Unmarshal(recorder.Get(idx), v)
}

// GetRequest gets bytes wrote (idx+1)th and unmarshals them as *jsonrpc.Request.
func (recorder *ConnRecorder) GetRequest(idx int) (*jsonrpc.Request, error) {
	req := &jsonrpc.Request{}
	err := recorder.Unmarshal(idx, req)
	return req, err
}

// GetAllRequests goes through all captured data tries to unmarshal it
// to the requests and then returns the result.
// Result will contains only those requests which method is different from "".
func (recorder *ConnRecorder) GetAllRequests() ([]*jsonrpc.Request, error) {
	recorder.mutex.Lock()
	defer recorder.mutex.Unlock()
	res := make([]*jsonrpc.Request, 0)
	for _, v := range recorder.capturedWrites {
		req := &jsonrpc.Request{}
		err := json.Unmarshal(v, req)
		if err != nil {
			return nil, err
		}
		if req.Method != "" {
			res = append(res, req)
		}
	}
	return res, nil
}

// GetResponse gets bytes wrote (idx+1)th and unmarshals them as *jsonrpc.Response.
func (recorder *ConnRecorder) GetResponse(idx int) (*jsonrpc.Response, error) {
	resp := &jsonrpc.Response{}
	err := recorder.Unmarshal(idx, resp)
	if floatID, ok := resp.ID.(float64); ok {
		resp.ID = int(floatID)
	}
	return resp, err
}

// UnmarshalResponseResult unmarshals response.Result wrote (idx+1)th to a given value.
func (recorder *ConnRecorder) UnmarshalResponseResult(idx int, v interface{}) error {
	resp, err := recorder.GetResponse(idx)
	if err != nil {
		return err
	}
	return json.Unmarshal(resp.Result, v)
}

// UnmarshalRequestParams unmarshals request.Params wrote (idx+1)th to a given value.
func (recorder *ConnRecorder) UnmarshalRequestParams(idx int, v interface{}) error {
	req, err := recorder.GetRequest(idx)
	if err != nil {
		return err
	}
	return json.Unmarshal(req.Params, &v)
}

// WaitUntil waits until either recorder is closed or predicate returned true,
// if closed before predicate condition is met, error is returned.
func (recorder *ConnRecorder) WaitUntil(p NativeConnWaitPredicate) error {
	recorder.cond.L.Lock()
	for {
		if recorder.closed {
			recorder.cond.L.Unlock()
			return errors.New("Closed before condition met")
		}
		if p(recorder) {
			recorder.cond.L.Unlock()
			return nil
		}
		recorder.cond.Wait()
	}
	recorder.cond.L.Unlock()
	return nil
}

// PushNextRaw pushes marshaled
// data to the read channel, so if Next() is called the data is returned.
func (recorder *ConnRecorder) PushNextRaw(data []byte) {
	recorder.nextChan <- data
}

// PushNext marshals a given value to json and
// pushes marshaled data to the read channel as PushNextRaw func does.
func (recorder *ConnRecorder) PushNext(v interface{}) error {
	marshaled, err := json.Marshal(v)
	if err != nil {
		return err
	}
	recorder.PushNextRaw(marshaled)
	return nil
}

// PushNextReq marshals a given method and params as requests and pushes it using PushNextRaw.
func (recorder *ConnRecorder) PushNextReq(method string, params interface{}) error {
	marshaledParams, err := json.Marshal(params)
	if err != nil {
		return err
	}
	return recorder.PushNext(&jsonrpc.Request{
		ID:     "test",
		Method: method,
		Params: marshaledParams,
	})
}

// Write captures given bytes.
func (recorder *ConnRecorder) Write(body []byte) error {
	recorder.mutex.Lock()
	recorder.capturedWrites = append(recorder.capturedWrites, body)
	recorder.cond.Broadcast()
	recorder.mutex.Unlock()
	return nil
}

// Next returns bytes pushed by one of Push* functions.
func (recorder *ConnRecorder) Next() ([]byte, error) {
	data, ok := <-recorder.nextChan
	if !ok {
		return nil, jsonrpc.NewCloseError(errors.New("Closed"))
	}
	return data, nil
}

// Close closes this recorder, so all the waiters wake up and receive an error.
func (recorder *ConnRecorder) Close() error {
	recorder.mutex.Lock()
	defer recorder.mutex.Unlock()
	if !recorder.closed {
		recorder.closed = true
		recorder.cond.Broadcast()
		close(recorder.nextChan)
	}
	return nil
}
