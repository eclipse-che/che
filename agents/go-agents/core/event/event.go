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

// Package event provides lightweight primitives for event-bus-consumer communication.
package event

import "sync"

// E is an interface for event.
// Only instances of this interface can be published in Bus.
type E interface {

	// Type returns a type of this event.
	// Bus uses returned type to retrieve and notify consumers.
	Type() string
}

// Consumer is an interface for event handlers.
type Consumer interface {

	// Accept handles given event.
	Accept(event E)
}

// TmpConsumer allows consumer to be removed from bus each time after accept.
// Accept is called at least once for such consumers.
type TmpConsumer interface {
	Consumer

	// IsDone called by bus after accept, if func returns true
	// this consumer will be removed from bus.
	IsDone() bool
}

// Bus is a event-consumer communication layer.
// It manages events publications and handling, its synchronous
// and safe to use from multiple goroutines.
// Use NewBus() func to get an instance of it.
type Bus struct {
	sync.RWMutex
	consumers map[string][]Consumer
}

// ConsumerF is a function which is also consumer,
// allows to use anonymous functions as consumers.
type ConsumerF func(event E)

// Accept calls consumer function passing event to it.
func (f ConsumerF) Accept(event E) { f(event) }

// NewBus creates a new bus.
func NewBus() *Bus {
	return &Bus{consumers: make(map[string][]Consumer)}
}

// Pub publishes an event to the interested consumers.
func (bus *Bus) Pub(e E) {
	bus.RLock()
	consumers, ok := bus.consumers[e.Type()]
	bus.RUnlock()

	if ok {
		for _, v := range consumers {
			v.Accept(e)
			if tmpCons, ok := v.(TmpConsumer); ok && tmpCons.IsDone() {
				bus.Rm(tmpCons)
			}
		}
	}
}

// Sub subscribes to the events of given type so
// given consumer will receive them.
func (bus *Bus) Sub(consumer Consumer, eType string) {
	bus.Lock()
	defer bus.Unlock()
	bus.consumers[eType] = append(bus.copyConsumers(eType), consumer)
}

// SubAny does the same as Sub func, but for multiple event types.
func (bus *Bus) SubAny(consumer Consumer, types ...string) {
	bus.Lock()
	defer bus.Unlock()
	for _, t := range types {
		bus.consumers[t] = append(bus.copyConsumers(t), consumer)
	}
}

// Rm removes given consumer from the bus, == operator is used
// to determine what elements should be removed.
// Returns true if consumer is removed.
func (bus *Bus) Rm(consumer Consumer) bool {
	return bus.RmIf(func(c Consumer) bool { return c == consumer })
}

// RmIf removes consumers for which predicate func returns true.
// Returns true if any consumer was removed.
func (bus *Bus) RmIf(predicate func(c Consumer) bool) bool {
	bus.Lock()
	defer bus.Unlock()
	ok := false
	for _type, candidates := range bus.consumers {
		for idx, candidate := range candidates {
			if predicate(candidate) {
				bus.rm(_type, idx)
				ok = true
			}
		}
	}
	return ok
}

// Clear assigns new map value to bus consumers and returns old ones.
// The modification of returned map won't affect bus consumers.
func (bus *Bus) Clear() map[string][]Consumer {
	bus.Lock()
	defer bus.Unlock()
	tmp := bus.consumers
	bus.consumers = make(map[string][]Consumer)
	return tmp
}

// Removes consumer subscribed to a given type and placed at a
// given index. If it is the last consumer then the whole slice is removed,
// otherwise a copy of slice is used, so concurrently publishing go-routines are not affected.
// Note that this function MUST be called within an appropriate lock.
func (bus *Bus) rm(key string, idx int) {
	arr := bus.consumers[key]
	if len(arr) == 1 {
		delete(bus.consumers, key)
	} else {
		newArr := make([]Consumer, 0, len(arr)-1)
		newArr = append(newArr, arr[:idx]...)
		newArr = append(newArr, arr[idx+1:]...)
		bus.consumers[key] = newArr
	}
}

// Returns a copy of consumers of a given event type.
// Note that this function MUST be called within an appropriate lock.
func (bus *Bus) copyConsumers(eType string) []Consumer {
	consumers := bus.consumers[eType]
	snapshot := make([]Consumer, len(consumers))
	copy(snapshot, consumers)
	return snapshot
}
