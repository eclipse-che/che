//
// Copyright (c) 2012-2017 Codenvy, S.A.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Codenvy, S.A. - initial API and implementation
//

package process

//ProcessBuilder simplifies creation of MachineProcess
type ProcessBuilder struct {
	command          Command
	beforeEventsHook func(p MachineProcess)
	firstSubscriber  *Subscriber
}

// NewBuilder creates new instance of ProcessBuilder
func NewBuilder() *ProcessBuilder {
	return &ProcessBuilder{}
}

// Cmd sets command of process
func (pb *ProcessBuilder) Cmd(command Command) *ProcessBuilder {
	pb.command = command
	return pb
}

// CmdLine sets command line of process
func (pb *ProcessBuilder) CmdLine(cmdLine string) *ProcessBuilder {
	pb.command.CommandLine = cmdLine
	return pb
}

// CmdType sets type of command that creates a process
func (pb *ProcessBuilder) CmdType(cmdType string) *ProcessBuilder {
	pb.command.Type = cmdType
	return pb
}

// CmdName sets name of command that creates a process
func (pb *ProcessBuilder) CmdName(cmdName string) *ProcessBuilder {
	pb.command.Name = cmdName
	return pb
}

// BeforeEventsHook sets the hook which will be called once before
// process subscribers notified with any of the process events,
// and after process is started.
func (pb *ProcessBuilder) BeforeEventsHook(hook func(p MachineProcess)) *ProcessBuilder {
	pb.beforeEventsHook = hook
	return pb
}

//FirstSubscriber sets first logs subscriber of process
func (pb *ProcessBuilder) FirstSubscriber(subscriber Subscriber) *ProcessBuilder {
	pb.firstSubscriber = &subscriber
	return pb
}

// Build creates MachineProcess from this builder
func (pb *ProcessBuilder) Build() MachineProcess {
	p := MachineProcess{
		Name:             pb.command.Name,
		CommandLine:      pb.command.CommandLine,
		Type:             pb.command.Type,
		beforeEventsHook: pb.beforeEventsHook,
	}
	if pb.firstSubscriber != nil {
		p.subs = []*Subscriber{pb.firstSubscriber}
	}
	return p
}

// Start creates MachineProcess from this builder and starts this process
func (pb *ProcessBuilder) Start() (MachineProcess, error) {
	return Start(pb.Build())
}
