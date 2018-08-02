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

package process

import (
	"time"
)

// Types of process events.
const (
	StartedEventType = "process_started"
	DiedEventType    = "process_died"
	StdoutEventType  = "process_stdout"
	StderrEventType  = "process_stderr"
)

// Event is a common interface for all the process events.
type Event interface {
	Type() string
}

// EventConsumer is a process events client.
type EventConsumer interface {
	Accept(event Event)
}

// EventConsumerFunc is a function which is also consumer,
// allows to use anonymous functions as process event consumers.
type EventConsumerFunc func(event Event)

func (f EventConsumerFunc) Accept(event Event) { f(event) }

// StartedEvent published once when process is started.
type StartedEvent struct {
	Time        time.Time `json:"time"`
	Pid         uint64    `json:"pid"`
	NativePid   int       `json:"nativePid"`
	Name        string    `json:"name"`
	CommandLine string    `json:"commandLine"`
}

// Type returns StartedEventType.
func (se *StartedEvent) Type() string { return StartedEventType }

func newStartedEvent(mp MachineProcess) *StartedEvent {
	return &StartedEvent{
		Time:        time.Now(),
		Pid:         mp.Pid,
		NativePid:   mp.NativePid,
		Name:        mp.Name,
		CommandLine: mp.CommandLine,
	}
}

// DiedEvent published once after process death.
type DiedEvent struct {
	Time        time.Time `json:"time"`
	Pid         uint64    `json:"pid"`
	NativePid   int       `json:"nativePid"`
	Name        string    `json:"name"`
	CommandLine string    `json:"commandLine"`
	ExitCode    int       `json:"exitCode"`
}

// Type returns DiedEventType.
func (de *DiedEvent) Type() string { return DiedEventType }

func newDiedEvent(mp MachineProcess) *DiedEvent {
	return &DiedEvent{
		Time:        time.Now(),
		Pid:         mp.Pid,
		NativePid:   mp.NativePid,
		Name:        mp.Name,
		CommandLine: mp.CommandLine,
		ExitCode:    mp.ExitCode,
	}
}

// OutputEvent is published each time when process writes to stdout or stderr.
type OutputEvent struct {
	Time time.Time `json:"time"`
	Pid  uint64    `json:"pid"`
	Text string    `json:"text"`

	_type string
}

// Type returns one of StdoutEventType, StderrEventType.
func (se *OutputEvent) Type() string { return se._type }

func newStderrEvent(pid uint64, text string, when time.Time) Event {
	return &OutputEvent{
		Time:  when,
		Pid:   pid,
		Text:  text,
		_type: StderrEventType,
	}
}

func newStdoutEvent(pid uint64, text string, when time.Time) Event {
	return &OutputEvent{
		Time:  when,
		Pid:   pid,
		Text:  text,
		_type: StdoutEventType,
	}
}
