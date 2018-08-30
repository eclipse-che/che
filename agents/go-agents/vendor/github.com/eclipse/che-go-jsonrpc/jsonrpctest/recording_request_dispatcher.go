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
	"errors"
	"sync"
	"time"

	"github.com/eclipse/che-go-jsonrpc"
)

// ReqRecorder helps to catch/record jsonrpc.Tunnel incoming requests.
type ReqRecorder struct {
	mutex    *sync.Mutex
	cond     *sync.Cond
	closed   bool
	requests []*reqPair
}

// NewReqRecorder creates a new recorder.
func NewReqRecorder() *ReqRecorder {
	mx := &sync.Mutex{}
	return &ReqRecorder{
		mutex:    mx,
		cond:     sync.NewCond(mx),
		closed:   false,
		requests: make([]*reqPair, 0),
	}
}

// ReqRecorderWaitPredicate is used to wait on recorder until the condition
// behind this predicate is met.
type ReqRecorderWaitPredicate func(req *ReqRecorder) bool

// ResponseArrivedAtLeast is a predicate that allows to wait until write is called at
// least given number of times.
func ResponseArrivedAtLeast(times int) ReqRecorderWaitPredicate {
	return func(recorder *ReqRecorder) bool {
		return len(recorder.requests) >= times
	}
}

// WaitUntil waits until either recorder is closed or predicate returned true,
// if closed before predicate returned true, error is returned.
func (recorder *ReqRecorder) WaitUntil(p ReqRecorderWaitPredicate) error {
	recorder.cond.L.Lock()
	for {
		if recorder.closed {
			recorder.cond.L.Unlock()
			return errors.New("Closed before condition is met")
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

// CloseAfter closes this request after specified timeout.
func (recorder *ReqRecorder) CloseAfter(dur time.Duration) {
	go func() {
		<-time.NewTimer(dur).C
		recorder.Close()
	}()
}

// FindHandler returns this recorder.
func (recorder *ReqRecorder) FindHandler(method string) (jsonrpc.MethodHandler, bool) {
	return recorder, true
}

// Unmarshal returns given params.
func (recorder *ReqRecorder) Unmarshal(params []byte) (interface{}, error) {
	return params, nil
}

// Call records a call of method handler.
func (recorder *ReqRecorder) Call(tun *jsonrpc.Tunnel, params interface{}, rt jsonrpc.RespTransmitter) {
	recorder.mutex.Lock()
	defer recorder.mutex.Unlock()
	if byteParams, ok := params.([]byte); ok {
		recorder.requests = append(recorder.requests, &reqPair{
			request: &jsonrpc.Request{
				Params: byteParams,
			},
			transmitter: rt,
		})
		recorder.cond.Broadcast()
	}
}

// Get returns request + response transmitter which were caught (idx+1)th.
func (recorder *ReqRecorder) Get(idx int) (*jsonrpc.Request, jsonrpc.RespTransmitter) {
	recorder.mutex.Lock()
	defer recorder.mutex.Unlock()
	pair := recorder.requests[idx]
	return pair.request, pair.transmitter
}

// Close closes this recorder all waiters will be notified about close
// and WaitUntil will return errors for them.
func (recorder *ReqRecorder) Close() {
	recorder.mutex.Lock()
	defer recorder.mutex.Unlock()
	if !recorder.closed {
		recorder.closed = true
		recorder.cond.Broadcast()
	}
}

type reqPair struct {
	request     *jsonrpc.Request
	transmitter jsonrpc.RespTransmitter
}
