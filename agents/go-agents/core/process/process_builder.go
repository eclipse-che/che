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

//Builder simplifies creation of MachineProcess.
type Builder struct {
	command          Command
	beforeEventsHook func(p MachineProcess)
	subscribers      []*Subscriber
}

// NewBuilder creates new instance of ProcessBuilder.
func NewBuilder() *Builder {
	return &Builder{}
}

// Cmd sets command of process.
func (pb *Builder) Cmd(command Command) *Builder {
	pb.command = command
	return pb
}

// CmdLine sets command line of process.
func (pb *Builder) CmdLine(cmdLine string) *Builder {
	pb.command.CommandLine = cmdLine
	return pb
}

// CmdType sets type of command that creates a process.
func (pb *Builder) CmdType(cmdType string) *Builder {
	pb.command.Type = cmdType
	return pb
}

// CmdName sets name of command that creates a process.
func (pb *Builder) CmdName(cmdName string) *Builder {
	pb.command.Name = cmdName
	return pb
}

// BeforeEventsHook sets the hook which will be called once before
// process subscribers notified with any of the process events,
// and after process is started.
func (pb *Builder) BeforeEventsHook(hook func(p MachineProcess)) *Builder {
	pb.beforeEventsHook = hook
	return pb
}

//Subscribe subscribes to the process events.
func (pb *Builder) Subscribe(id string, mask uint64, consumer EventConsumer) *Builder {
	pb.subscribers = append(pb.subscribers, &Subscriber{
		ID:       id,
		Mask:     mask,
		Consumer: consumer,
	})
	return pb
}

//SubscribeDefault subscribes to the process events using process.DefaultMask.
func (pb *Builder) SubscribeDefault(id string, consumer EventConsumer) *Builder {
	return pb.Subscribe(id, DefaultMask, consumer)
}

// Build creates MachineProcess from this builder.
func (pb *Builder) Build() MachineProcess {
	p := MachineProcess{
		Name:             pb.command.Name,
		CommandLine:      pb.command.CommandLine,
		Type:             pb.command.Type,
		beforeEventsHook: pb.beforeEventsHook,
		subs:             pb.subscribers,
	}
	return p
}

// Start creates MachineProcess from this builder and starts this process.
func (pb *Builder) Start() (MachineProcess, error) {
	return Start(pb.Build())
}
