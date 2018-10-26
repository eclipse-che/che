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

package jsonrpc

import (
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"strconv"
	"sync"
	"sync/atomic"
	"time"
)

const (
	// DefaultVersion is a version which is used by this package
	// for all the types of sent/received data.
	DefaultVersion = "2.0"

	// ConnectedNotificationMethodName notification sent by Tunnel.SayHello().
	ConnectedNotificationMethodName = "connected"

	// DefaultAllowedResponseDelay is allowed time for reply to be sent.
	// RespTransmitter waits for MethodHandler to respond to the request
	// or replies with timeout error if timeout is reached.
	// The value is experimental.
	DefaultAllowedResponseDelay = time.Minute

	// DefaultWatchQueuePeriod is how often request queue lookups requests.
	// The value is experimental.
	DefaultWatchQueuePeriod = time.Minute

	// DefaultMaxRequestHoldTimeout is how long a request stays in the queue
	// and waits for the response to come, if reply is not made in time
	// RespHandlerFunc is called with TimeoutError.
	// Note that request stays in request queue from DefaultMaxRequestHoldTimeout
	// to DefaultMaxRequestHoldTimeout + DefaultWatchQueuePeriod time.
	DefaultMaxRequestHoldTimeout = time.Minute
)

const (
	// The initial state of created tunnel, tunnel is not running(Go func is not called).
	stateCreated = 0

	// Go func was called on the tunnel, tunnel is running.
	stateRunning = 1

	// Tunnel is closed. Tunnel may be closed only if it was running before.
	stateClosed = -1
)

var (
	prevTunID uint64
	prevReqID int64
)

// Tunnel is high level jsonrpc transport layer which
// uses native connection to access low level transport routines.
type Tunnel struct {

	// id is the unique identifier of this tunnel.
	id string

	// created is the time when this tunnel was created.
	created time.Time

	// conn is a native connection on which this tunnel is based.
	conn NativeConn

	// Anything put to this tunnel will be packaged to json and sent to the client.
	jsonOut chan interface{}

	// Helps to dispatch request to the right method handler.
	reqDispatcher ReqDispatcher

	// Queue for requests.
	q *requestQ

	// Helps to close the tunnel.
	closer *closer

	// Keeps current state of the tunnel.
	state int32
}

// NewTunnel creates a new tunnel.
// Use tunnel.Go() to start communication.
func NewTunnel(conn NativeConn, dispatcher ReqDispatcher) *Tunnel {
	q := &requestQ{
		pairs:                make(map[int64]*rqPair),
		allowedResponseDelay: DefaultAllowedResponseDelay,
		stop:                 make(chan bool, 1),
		ticker:               time.NewTicker(DefaultWatchQueuePeriod),
	}
	tunnel := &Tunnel{
		id:            "tunnel-" + strconv.Itoa(int(atomic.AddUint64(&prevTunID, 1))),
		created:       time.Now(),
		conn:          conn,
		jsonOut:       make(chan interface{}),
		reqDispatcher: dispatcher,
		q:             q,
		state:         stateCreated,
	}
	tunnel.closer = &closer{tunnel: tunnel, outClosed: make(chan bool, 1)}
	return tunnel
}

// NewManagedTunnel creates a new tunnel using given connection
// and DefaultRouter as request dispatcher, then the tunnel is saved
// in default registry and Go func is called.
func NewManagedTunnel(conn NativeConn) *Tunnel {
	tunnel := NewTunnel(conn, DefaultRouter)
	Save(tunnel)
	tunnel.Go()
	return tunnel
}

// ReqDispatcher is a single handler for all the tunnel incoming requests.
// The main responsibility is to provide the right method handler for incoming request.
type ReqDispatcher interface {

	// FindHandler must return a handler for a given method and ok=true,
	// if there is no such handler func must return ok=false
	FindHandler(method string) (MethodHandler, bool)
}

// RespHandleFunc used to handle requests responses.
// The request is sent by one of the Request, RequestBare methods.
// If the response doesn't arrive in time the handler func will be called
// with an error with code TimeoutErrorCode.
type RespHandleFunc func(result []byte, err *Error)

// MethodHandler handles a certain method.
// First raw request parameters are decoded using Unmarshal function
// and then if call returned no error handle is called.
type MethodHandler interface {

	// Unmarshal decodes request raw request parameters
	// e.g. parses json and returns instance of structured params.
	// If the handler does not need parameters - (nil, nil) should be returned.
	// If returned error is different from nil Call is not executed.
	Unmarshal(params []byte) (interface{}, error)

	// Call calls handler of this request.
	// If no Send method is called on response transmitter instance
	// timeout error reply will be sent, unless request is notification.
	Call(tun *Tunnel, params interface{}, rt RespTransmitter)
}

// RespTransmitter provides interface which allows to respond to request from any method handler.
// The implementation must guarantee that reply will be eventually sent
// for those requests which are not notifications.
// Functions Send & SendError MUST not be called both or twice on the same
// instance of transmitter. The request id MUST be included to the response.
type RespTransmitter interface {

	// Send sends jsonrpc response with a given result in body.
	// This function can be called only once on one transmitter instance.
	Send(result interface{})

	// SendError sends jsonrpc response with a given error in body.
	// This function can be called only once on one transmitter instance.
	SendError(err *Error)
}

// ID returns the identifier of this tunnel.
func (tun *Tunnel) ID() string { return tun.id }

// Conn returns the connection this tunnel is based on.
func (tun *Tunnel) Conn() NativeConn { return tun.conn }

// Go starts this tunnel, makes it functional.
func (tun *Tunnel) Go() {
	if atomic.CompareAndSwapInt32(&tun.state, stateCreated, stateRunning) {
		go tun.mainWriteLoop()
		go tun.mainReadLoop()
		go tun.q.watch()
	}
}

// Notify sends notification(request without id) using given params as its body.
func (tun *Tunnel) Notify(method string, params interface{}) error {
	marshaledParams, err := json.Marshal(params)
	if err != nil {
		return err
	}
	return tun.trySend(&Request{
		Version: DefaultVersion,
		Method:  method,
		Params:  marshaledParams,
	})
}

// NotifyBare sends notification like Notify does but
// sends no request parameters in it.
func (tun *Tunnel) NotifyBare(method string) error {
	return tun.trySend(&Request{Version: DefaultVersion, Method: method})
}

// Request sends request marshalling a given params as its body.
// RespHandleFunc will be called as soon as the response arrives,
// or response arrival timeout reached, in that case error of type
// TimeoutError will be passed to the handler.
func (tun *Tunnel) Request(method string, params interface{}, rhf RespHandleFunc) error {
	marshaledParams, err := json.Marshal(params)
	if err != nil {
		return err
	}
	id := atomic.AddInt64(&prevReqID, 1)
	request := &Request{
		ID:     id,
		Method: method,
		Params: marshaledParams,
	}
	tun.q.add(id, request, time.Now(), rhf)
	return tun.trySend(request)
}

// RequestBare sends the request like Request func does
// but sends no params in it.
func (tun *Tunnel) RequestBare(method string, rhf RespHandleFunc) error {
	id := atomic.AddInt64(&prevReqID, 1)
	request := &Request{ID: id, Method: method}
	tun.q.add(id, request, time.Now(), rhf)
	return tun.trySend(request)
}

// Close closes native connection and internal sources, so started
// go routines should be eventually stopped.
func (tun *Tunnel) Close() {
	tun.closer.closeOnce()
}

// IsClosed returns true if this tunnel is closed and false otherwise.
func (tun *Tunnel) IsClosed() bool {
	return atomic.LoadInt32(&tun.state) == stateClosed
}

// SayHello sends hello notification.
func (tun *Tunnel) SayHello() {
	tun.Notify(ConnectedNotificationMethodName, &TunnelNotification{
		Time:      tun.created,
		ChannelID: tun.id,
		TunnelID:  tun.id,
		Text:      "Hello!",
	})
}

// TunnelNotification struct describing notification params sent by SayHello.
type TunnelNotification struct {

	// Time is the time channel was created.
	Time time.Time `json:"time"`

	// ChannelID is the id of the tunnel.
	// The value is the same to TunnelID, its kept for backward comp.
	ChannelID string `json:"channel"`

	// TunnelID the id of the tunnel.
	TunnelID string `json:"tunnel"`

	// Text event message.
	Text string `json:"text"`
}

func (tun *Tunnel) trySend(notMarshaled interface{}) (err error) {
	defer func() {
		if r := recover(); r != nil {
			err = NewCloseError(errors.New("Connection closed"))
		}
	}()
	tun.jsonOut <- notMarshaled
	return nil
}

func (tun *Tunnel) respond(r *Response) {
	if err := tun.trySend(r); err != nil {
		log.Printf("Trying to send response '%v' to the closed connection", r)
	}
}

func (tun *Tunnel) mainWriteLoop() {
	for message := range tun.jsonOut {
		if bytes, err := json.Marshal(message); err != nil {
			log.Printf("Couldn't marshal message: %T, %v to json. Error %s", message, message, err.Error())
		} else {
			if err := tun.conn.Write(bytes); err != nil {
				log.Printf("Couldn't write message to the tunnel. Message: %T, %v", message, message)
			}
		}
	}
	tun.closer.outClosed <- true
}

func (tun *Tunnel) mainReadLoop() {
	for {
		binMessage, err := tun.conn.Next()
		if err == nil {
			tun.handleMessage(binMessage)
		} else {
			tun.closer.closeOnce()
			return
		}
	}
}

func (tun *Tunnel) handleResponse(r *Response) {
	if r.ID == nil {
		log.Print("Received response with empty identifier, response will be ignored")
		return
	}

	// float64 used for json numbers https://blog.golang.org/json-and-go
	floatID, ok := r.ID.(float64)
	if !ok {
		log.Printf("Received response with non-numeric identifier %T %v, "+
			"response will be ignored", r.ID, r.ID)
		return
	}

	id := int64(floatID)
	rqPair, ok := tun.q.remove(id)
	if ok {
		rqPair.respHandlerFunc(r.Result, r.Error)
	} else {
		log.Printf("Response handler for request id '%v' is missing which means that response "+
			"arrived to late, or response provides a wrong id", id)
	}
}

func (tun *Tunnel) handleMessage(binMessage []byte) {
	draft := &draft{}
	err := json.Unmarshal(binMessage, draft)

	// parse error indicated
	if err != nil {
		tun.respond(&Response{
			Version: DefaultVersion,
			ID:      nil,
			Error:   NewError(ParseErrorCode, errors.New("Error while parsing request")),
		})
		return
	}

	// version check
	if draft.Version == "" {
		draft.Version = DefaultVersion
	} else if draft.Version != DefaultVersion {
		err := fmt.Errorf("Version %s is not supported, please use %s", draft.Version, DefaultVersion)
		tun.respond(&Response{
			Version: DefaultVersion,
			ID:      nil,
			Error:   NewError(InvalidRequestErrorCode, err),
		})
		return
	}

	if draft.Method == "" {
		tun.handleResponse(&Response{
			Version: draft.Version,
			ID:      draft.ID,
			Result:  draft.Result,
			Error:   draft.Error,
		})
	} else {
		tun.handleRequest(&Request{
			Version: draft.Version,
			Method:  draft.Method,
			ID:      draft.ID,
			Params:  draft.RawParams,
		})
	}
}

func (tun *Tunnel) handleRequest(r *Request) {
	handler, ok := tun.reqDispatcher.FindHandler(r.Method)

	if !ok {
		if !r.IsNotification() {
			tun.jsonOut <- &Response{
				ID:      r.ID,
				Version: DefaultVersion,
				Error:   NewErrorf(MethodNotFoundErrorCode, "No such method %s", r.Method),
			}
		}
		return
	}

	// handle params decoding
	decodedParams, err := handler.Unmarshal(r.Params)
	if err != nil {
		if !r.IsNotification() {
			tun.respond(&Response{
				ID:      r.ID,
				Version: DefaultVersion,
				Error:   NewError(ParseErrorCode, errors.New("Couldn't parse params")),
			})
		}
		return
	}

	// pick the right transmitter
	var transmitter RespTransmitter
	if r.IsNotification() {
		transmitter = &doNothingTransmitter{r}
	} else {
		transmitter = newWatchingRespTransmitter(r.ID, tun)
	}

	// make a method call
	handler.Call(tun, decodedParams, transmitter)
}

// both request and response
type draft struct {
	Version   string          `json:"jsonrpc"`
	Method    string          `json:"method"`
	ID        interface{}     `json:"id"`
	RawParams json.RawMessage `json:"params"`
	Result    json.RawMessage `json:"result,omitempty"`
	Error     *Error          `json:"error,omitempty"`
}

type closer struct {
	tunnel    *Tunnel
	outClosed chan bool
}

func (closer *closer) closeOnce() {
	if atomic.CompareAndSwapInt32(&closer.tunnel.state, stateRunning, stateClosed) {
		close(closer.tunnel.jsonOut)
		// wait write loop to complete
		<-closer.outClosed

		closer.tunnel.q.stopWatching()
		if err := closer.tunnel.conn.Close(); err != nil {
			log.Printf("Error while closing connection, %s", err.Error())
		}
	}
}

type rqPair struct {
	request         *Request
	respHandlerFunc RespHandleFunc
	saved           time.Time
}

// Request queue is used for internal request + response handler storage,
// which allows to handle future responses.
// The q does not support any request identifiers except int64.
type requestQ struct {
	sync.RWMutex
	pairs                map[int64]*rqPair
	ticker               *time.Ticker
	stop                 chan bool
	allowedResponseDelay time.Duration
}

func (q *requestQ) watch() {
	for {
		select {
		case <-q.ticker.C:
			q.dropOutdatedOnce()
		case <-q.stop:
			q.ticker.Stop()
			return
		}
	}
}

func (q *requestQ) dropOutdatedOnce() {
	dropTime := time.Now().Add(-q.allowedResponseDelay)
	dropout := make([]int64, 0)

	q.RLock()
	for id, pair := range q.pairs {
		if pair.saved.Before(dropTime) {
			dropout = append(dropout, id)
		}
	}
	q.RUnlock()

	for _, id := range dropout {
		if rqPair, ok := q.remove(id); ok {
			rqPair.respHandlerFunc(nil, NewError(TimeoutErrorCode, errors.New("Response didn't arrive in time")))
		}
	}
}

func (q *requestQ) stopWatching() { q.stop <- true }

func (q *requestQ) add(id int64, r *Request, time time.Time, rhf RespHandleFunc) {
	q.Lock()
	defer q.Unlock()
	q.pairs[id] = &rqPair{
		request:         r,
		respHandlerFunc: rhf,
		saved:           time,
	}
}

func (q *requestQ) remove(id int64) (*rqPair, bool) {
	q.Lock()
	defer q.Unlock()
	pair, ok := q.pairs[id]
	if ok {
		delete(q.pairs, id)
	}
	return pair, ok
}

func newWatchingRespTransmitter(id interface{}, c *Tunnel) *respTransmitter {
	t := &respTransmitter{
		reqID:  id,
		tunnel: c,
		once:   &sync.Once{},
		done:   make(chan bool, 1),
	}
	go t.watch(DefaultMaxRequestHoldTimeout)
	return t
}

type respTransmitter struct {
	reqID  interface{}
	tunnel *Tunnel
	once   *sync.Once
	done   chan bool
}

func (drt *respTransmitter) watch(timeout time.Duration) {
	timer := time.NewTimer(timeout)
	select {
	case <-timer.C:
		drt.SendError(NewErrorf(TimeoutErrorCode, "Server didn't respond to the request %v in time ", drt.reqID))
	case <-drt.done:
		timer.Stop()
	}
}

func (drt *respTransmitter) Send(result interface{}) {
	drt.release(func() {
		marshaled, _ := json.Marshal(result)
		drt.tunnel.respond(&Response{
			Version: DefaultVersion,
			ID:      drt.reqID,
			Result:  marshaled,
		})
	})
}

func (drt *respTransmitter) SendError(err *Error) {
	drt.release(func() {
		drt.tunnel.jsonOut <- &Response{
			Version: DefaultVersion,
			ID:      drt.reqID,
			Error:   err,
		}
	})
}

func (drt *respTransmitter) release(f func()) {
	drt.once.Do(func() {
		f()
		drt.done <- true
	})
}

type doNothingTransmitter struct {
	request *Request
}

func (nrt *doNothingTransmitter) logNoResponse(res interface{}) {
	log.Printf(
		"The response to the notification '%s' will not be send(jsonrpc2.0 spec). The response was %T, %v",
		nrt.request.Method,
		res,
		res,
	)
}

func (nrt *doNothingTransmitter) Send(result interface{}) { nrt.logNoResponse(result) }

func (nrt *doNothingTransmitter) SendError(err *Error) { nrt.logNoResponse(err) }
