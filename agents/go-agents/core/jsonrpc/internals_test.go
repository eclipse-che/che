//
// Copyright (c) 2012-2018 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

package jsonrpc

import (
	"encoding/json"
	"errors"
	"sync"
	"testing"
	"time"
)

func TestRespTransmitterSendsBody(t *testing.T) {
	tunnel := &Tunnel{jsonOut: make(chan interface{}, 1)}
	transmitter := &respTransmitter{
		reqID:  "1",
		tunnel: tunnel,
		once:   &sync.Once{},
		done:   make(chan bool, 1),
	}

	type body struct{ Message string }
	sentBody := body{"transmitted response"}
	transmitter.Send(&sentBody)

	resp := waitResp(t, tunnel.jsonOut)

	if resp.ID != transmitter.reqID {
		t.Fatalf("Expected transmitter to use request id '%v' as response id but used '%v' ", transmitter.reqID, resp.ID)
	}
	if resp.Error != nil {
		t.Fatalf("Expected transmitter sends no error in response, but found %v", resp.Error)
	}
	if resp.Version != DefaultVersion {
		t.Fatalf("Expected respnonse version to be %s but found %s", DefaultVersion, resp.Version)
	}
	respBody := body{}
	if err := json.Unmarshal(resp.Result, &respBody); err != nil {
		t.Fatal(err)
	}
	if respBody != sentBody {
		t.Fatalf("Expected to receieve response result %v but received %v", sentBody, respBody)
	}
}

func TestRespTransmitterSendsError(t *testing.T) {
	c := &Tunnel{jsonOut: make(chan interface{}, 1)}
	transmitter := &respTransmitter{
		reqID:  "1",
		tunnel: c,
		once:   &sync.Once{},
		done:   make(chan bool, 1),
	}

	errMessage := "no!no!no!"
	transmitter.SendError(NewArgsError(errors.New(errMessage)))

	resp := waitResp(t, c.jsonOut)

	if resp.ID != transmitter.reqID {
		t.Fatalf("Expected transmitter to use request id '%v' as response id but used '%v' ", transmitter.reqID, resp.ID)
	}
	if resp.Version != DefaultVersion {
		t.Fatalf("Expected respnonse version to be %s but found %s", DefaultVersion, resp.Version)
	}
	if resp.Result != nil {
		t.Fatalf("Expected transmitter sends no result in response, but result is %v", resp.Result)
	}
	if resp.Error == nil {
		t.Fatal("Expected transmitter sends error in response")
	}
	if resp.Error.Message != errMessage {
		t.Fatalf("Expected to receive error message %s but received %s", errMessage, resp.Error.Message)
	}
	if resp.Error.Code != InvalidParamsErrorCode {
		t.Fatalf("Expected to receive error code %d but received %d", InvalidParamsErrorCode, resp.Error.Code)
	}
}

func TestTransmitterDoesNotSendBodyTwice(t *testing.T) {
	c := &Tunnel{jsonOut: make(chan interface{}, 2)}
	transmitter := &respTransmitter{
		reqID:  "1",
		tunnel: c,
		once:   &sync.Once{},
		done:   make(chan bool, 1),
	}

	type body struct{ Message string }
	transmitter.Send(&body{"hello"})
	transmitter.Send(&body{"hello"})

	// read first time
	select {
	case <-c.jsonOut:
	default:
		t.Fatal("Must send first message to the tunnel")
	}

	// must not read the second time
	select {
	case <-c.jsonOut:
		t.Fatal("Must not send second second message to the tunnel")
	default:
		// ok
	}
}

func TestTransmitterRespondsIfTimeoutIsReached(t *testing.T) {
	c := &Tunnel{jsonOut: make(chan interface{}, 1)}
	transmitter := &respTransmitter{
		reqID:  "1",
		tunnel: c,
		once:   &sync.Once{},
		done:   make(chan bool, 1),
	}
	transmitter.watch(0)

	resp := waitResp(t, c.jsonOut)

	if resp.ID != transmitter.reqID {
		t.Fatalf("Expected transmitter to use request id '%v' as response id but used '%v' ", transmitter.reqID, resp.ID)
	}
	if resp.Version != DefaultVersion {
		t.Fatalf("Expected respnonse version to be %s but found %s", DefaultVersion, resp.Version)
	}
	if resp.Error == nil {
		t.Fatal("Expected transmitter sends error in response")
	}
	if resp.Error.Code != TimeoutErrorCode {
		t.Fatalf("Expected to receive error code %d but received %d", TimeoutErrorCode, resp.Error.Code)
	}
}

func TestRequestQueueRemovesResponsesAfterTimeout(t *testing.T) {
	q := &requestQ{pairs: make(map[int64]*rqPair)}

	now := time.Now()
	cases := []struct {
		id            int64
		time          time.Time
		mustBeDropped bool
	}{
		{id: 1, time: now.Add(-time.Second * 30), mustBeDropped: true},
		{id: 2, time: now.Add(-time.Minute * 30), mustBeDropped: true},
		{id: 3, time: now.Add(-time.Hour * 30), mustBeDropped: true},
		{id: 4, time: now.Add(-time.Second * 15), mustBeDropped: true},

		{id: 5, time: now.Add(-time.Second * 14), mustBeDropped: false},
		{id: 6, time: now.Add(-time.Second * 5), mustBeDropped: false},
		{id: 7, time: now, mustBeDropped: false},
	}

	dropped := 0
	mustBeDropped := 0
	for _, _case := range cases {
		if _case.mustBeDropped {
			mustBeDropped++
			q.add(_case.id, &Request{}, _case.time, func(res []byte, err *Error) {
				if err == nil {
					t.Fatal("Must receive timeout error, but no error received")
				}
				if err.Code != TimeoutErrorCode {
					t.Fatalf("Must receive TimeoutError, but received %T %s", err, err.Error())
				}
				dropped++
			})
		} else {
			q.add(_case.id, &Request{}, _case.time, func(res []byte, err *Error) {
				t.Fatalf("The handler for request %d must not be called", _case.id)
			})
		}
	}

	q.allowedResponseDelay = time.Second * 15
	q.dropOutdatedOnce()

	if dropped != mustBeDropped {
		t.Fatalf("Must be dropped %d but actually dropped %d", mustBeDropped, dropped)
	}
	for _, _case := range cases {
		if _case.mustBeDropped {
			if _, ok := q.pairs[_case.id]; ok {
				t.Fatalf("The request with id %d must not be present in q", _case.id)
			}
		}
	}
}

func waitResp(t *testing.T, c chan interface{}) *Response {
	select {
	case v := <-c:
		if r, ok := v.(*Response); !ok {
			t.Fatal("Expected to receive response")
		} else {
			return r
		}
	case <-time.After(time.Second):
		t.Fatal("Didn't receive response in specified timeout")
	}
	return nil
}
