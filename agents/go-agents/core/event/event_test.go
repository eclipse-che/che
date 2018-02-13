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

package event_test

import (
	"sync"
	"testing"

	"github.com/eclipse/che/agents/go-agents/core/event"
)

func TestSub(t *testing.T) {
	bus := event.NewBus()

	c := &testConsumer{}
	bus.Sub(c, "test")

	bus.Pub(&testEvent{"test"})

	checkAccepts(t, c, 1)
}

func TestSubAny(t *testing.T) {
	bus := event.NewBus()

	c1 := &testConsumer{}
	c2 := &testConsumer{}

	bus.SubAny(c1, "test", "test2")
	bus.SubAny(c2, "test3")

	bus.Pub(&testEvent{"test"})
	bus.Pub(&testEvent{"test2"})
	bus.Pub(&testEvent{"test3"})

	checkAccepts(t, c1, 2)
	checkAccepts(t, c2, 1)
}

func TestRmIf(t *testing.T) {
	bus := event.NewBus()

	c1 := &testConsumer{id: "first"}
	c2 := &testConsumer{id: "second"}

	bus.SubAny(c1, "test", "test2")
	bus.SubAny(c2, "test3")

	bus.RmIf(func(c event.Consumer) bool {
		return c.(*testConsumer).id == "first"
	})

	bus.Pub(&testEvent{"test"})
	bus.Pub(&testEvent{"test2"})
	bus.Pub(&testEvent{"test3"})

	checkAccepts(t, c1, 0)
	checkAccepts(t, c2, 1)
}

func TestRmItself(t *testing.T) {
	bus := event.NewBus()

	c := &selfRemovingConsumer{bus: bus}
	bus.Sub(c, "test")

	bus.Pub(&testEvent{"test"})
	bus.Pub(&testEvent{"test"})

	if c.accepts != 1 {
		t.Fatalf("Expected consumer to be removed after the first publication")
	}
}

func TestManyTmpConsumersAreProperlyHandled(t *testing.T) {
	publishingRoutinesCount := 5
	eventsToPublish := 1000
	subscribersCount := 100

	bus := event.NewBus()
	for i := 0; i < subscribersCount; i++ {
		bus.Sub(&testTmpConsumer{hitsUntilDone: publishingRoutinesCount * eventsToPublish}, "test")
	}

	startWaiter := &sync.WaitGroup{}
	startWaiter.Add(publishingRoutinesCount)

	completionWaiter := &sync.WaitGroup{}
	completionWaiter.Add(publishingRoutinesCount)

	for i := 0; i < publishingRoutinesCount; i++ {
		go func() {
			startWaiter.Done()
			startWaiter.Wait()

			for j := 0; j < eventsToPublish; j++ {
				bus.Pub(&testEvent{"test"})
			}

			completionWaiter.Done()
		}()
	}

	completionWaiter.Wait()

	if len(bus.Clear()) != 0 {
		t.Fatal("All TmpConsumers must be removed")
	}
}

type testEvent struct{ eType string }

func (te *testEvent) Type() string { return te.eType }

type testConsumer struct {
	id      string
	accepts int
}

func (tc *testConsumer) Accept(e event.E) { tc.accepts++ }

type selfRemovingConsumer struct {
	bus     *event.Bus
	accepts int
}

func (c *selfRemovingConsumer) Accept(e event.E) {
	c.accepts++
}

func (c *selfRemovingConsumer) IsDone() bool {
	return c.accepts > 0
}

func checkAccepts(t *testing.T, tc *testConsumer, expected int) {
	if tc.accepts != expected {
		t.Fatalf("Consumer id = '%s'. Expected accepts %d != Actual %d", tc.id, expected, tc.accepts)
	}
}

type testTmpConsumer struct {
	sync.Mutex
	hitsUntilDone int
}

func (c *testTmpConsumer) Accept(e event.E) {
	c.Lock()
	defer c.Unlock()
	c.hitsUntilDone--
}

func (c *testTmpConsumer) IsDone() bool {
	c.Lock()
	defer c.Unlock()
	return c.hitsUntilDone <= 0
}
