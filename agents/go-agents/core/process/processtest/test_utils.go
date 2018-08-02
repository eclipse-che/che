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

// Package processtest provides utils for process testing.
package processtest

import (
	"sync"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/process"
)

// NewEventsCaptor create a new instance of events captor
func NewEventsCaptor(deathEventType string) *EventsCaptor {
	return &EventsCaptor{DeathEventType: deathEventType}
}

// EventsCaptor helps to Capture process events and wait for them.
type EventsCaptor struct {
	sync.Mutex

	// Result events.
	events []process.Event

	// Events channel. Close of this channel considered as immediate interruption,
	// to hold until execution completes use captor.Wait(timeout) channel.
	eventsChan chan process.Event

	// Channel used as internal approach to interrupt capturing.
	interruptChan chan bool

	// Captor sends true if finishes reaching DeathEventType
	// and false if interrupted while waiting for event of DeathEventType.
	done chan bool

	// The last event after which events capturing stopped.
	DeathEventType string
}

func (ec *EventsCaptor) addEvent(e process.Event) {
	ec.Lock()
	defer ec.Unlock()
	ec.events = append(ec.events, e)
}

// Events returns all the captured events.
func (ec *EventsCaptor) Events() []process.Event {
	ec.Lock()
	defer ec.Unlock()
	cp := make([]process.Event, len(ec.events))
	copy(cp, ec.events)
	return cp
}

// Capture starts capturing events, until one of the
// following conditions is met:
// - event of type EventsCaptor.deathEventType received.
//   In this case capturing is successful done <- true
// - events channel closed.
//   In this case capturing is interrupted done <- false
func (ec *EventsCaptor) Capture() {
	ec.eventsChan = make(chan process.Event)
	ec.interruptChan = make(chan bool, 1)
	ec.done = make(chan bool)

	go func() {
		for {
			select {
			case event, ok := <-ec.eventsChan:
				if ok {
					ec.addEvent(event)
					if event.Type() == ec.DeathEventType {
						// death event reached - capturing is done
						ec.done <- true
						return
					}
				} else {
					// events channel closed interrupt immediately
					ec.done <- false
					return
				}
			case <-ec.interruptChan:
				close(ec.eventsChan)
			}
		}
	}()
}

// Waits a timeout and if deadTypeEvent isn't reached interrupts captor.
// Returns done channel if the value received from the channel is true
// then the captor finished capturing successfully catching deathEventType,
// otherwise it was interrupted.
func (ec *EventsCaptor) Wait(timeout time.Duration) chan bool {
	go func() {
		<-time.NewTimer(timeout).C
		ec.interruptChan <- true
	}()
	return ec.done
}

// Interrupts capturing immediately, returns done channel.
func (ec *EventsCaptor) Stop() chan bool {
	return ec.Wait(0)
}

// Accept notifies the captor about incoming event.
func (ec *EventsCaptor) Accept(e process.Event) { ec.eventsChan <- e }
