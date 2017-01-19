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

import (
	"errors"
	"fmt"
	"os"
	"os/exec"
	"sync"
	"sync/atomic"
	"syscall"
	"time"

	"github.com/eclipse/che/agents/exec-agent/rpc"
)

const (
	StdoutBit        = 1 << iota
	StderrBit        = 1 << iota
	ProcessStatusBit = 1 << iota
	DefaultMask      = StderrBit | StdoutBit | ProcessStatusBit

	DateTimeFormat = time.RFC3339Nano

	StdoutKind = "STDOUT"
	StderrKind = "STDERR"

	DefaultShellInterpreter = "/bin/bash"
)

var (
	prevPid          uint64 = 0
	processes               = &processesMap{items: make(map[uint64]*MachineProcess)}
	logsDist                = NewLogsDistributor()
	LogsDir          string
	ShellInterpreter string = DefaultShellInterpreter
)

type Command struct {
	Name        string `json:"name"`
	CommandLine string `json:"commandLine"`
	Type        string `json:"type"`
}

// Defines machine process model
type MachineProcess struct {

	// The virtual id of the process, it is guaranteed that pid
	// is always unique, while NativePid may occur twice or more(when including dead processes)
	Pid uint64 `json:"pid"`

	// The name of the process, it is equal to the Command.Name which this process created from.
	// It doesn't have to be unique, at least machine agent doesn't need such constraint,
	// as pid is used for identifying process
	Name string `json:"name"`

	// The command line executed by this process.
	// It is equal to the Command.CommandLine which this process created from
	CommandLine string `json:"commandLine"`

	// The type of the command line, this field is rather useful meta
	// information  than something used for functioning. It is equal
	// to the Command.Type which this process created from
	Type string `json:"type"`

	// Whether this process is alive or dead
	Alive bool `json:"alive"`

	// The native(OS) pid, it is unique per alive processes,
	// but those which are not alive, may have the same NativePid
	NativePid int `json:"nativePid"`

	// Process log filename
	logfileName string

	// Command executed by this process.
	// If process is not alive then the command value is set to nil
	command *exec.Cmd

	// Stdout/stderr pumper.
	// If process is not alive then the pumper value is set to nil
	pumper *LogsPumper

	// Process subscribers, all the outgoing events are go through those subscribers.
	// If process is not alive then the subscribers value is set to nil
	subs []*Subscriber

	// Process file logger
	fileLogger *FileLogger

	// Process mutex should be used to sync process data
	// or block on process related operations such as events publications
	mutex sync.RWMutex

	// When the process was last time used by client
	lastUsed     time.Time
	lastUsedLock sync.RWMutex

	// Called once before any of process events is published
	// and after process is started
	beforeEventsHook func(process MachineProcess)
}

type Subscriber struct {
	Id      string
	Mask    uint64
	Channel chan *rpc.Event
}

type LogMessage struct {
	Kind string    `json:"kind"`
	Time time.Time `json:"time"`
	Text string    `json:"text"`
}

type NoProcessError struct {
	error
	Pid uint64
}

type NotAliveError struct {
	error
	Pid uint64
}

// Lockable map for storing processes
type processesMap struct {
	sync.RWMutex
	items map[uint64]*MachineProcess
}

func Start(process MachineProcess) (MachineProcess, error) {
	// wrap command to be able to kill child processes see https://github.com/golang/go/issues/8854
	cmd := exec.Command("setsid", ShellInterpreter, "-c", process.CommandLine)

	// getting stdout pipe
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		return process, err
	}

	// getting stderr pipe
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return process, err
	}

	// starting a new process
	err = cmd.Start()
	if err != nil {
		return process, err
	}

	// increment current pid & assign it to the value
	pid := atomic.AddUint64(&prevPid, 1)

	// Figure out the place for logs file
	dir, err := logsDist.DirForPid(LogsDir, pid)
	if err != nil {
		return process, err
	}
	filename := fmt.Sprintf("%s%cpid-%d", dir, os.PathSeparator, pid)

	fileLogger, err := NewLogger(filename)
	if err != nil {
		return process, err
	}

	// save process
	process.Pid = pid
	process.Alive = true
	process.NativePid = cmd.Process.Pid
	process.command = cmd
	process.pumper = NewPumper(stdout, stderr)
	process.logfileName = filename
	process.fileLogger = fileLogger
	process.updateLastUsedTime()

	processes.Lock()
	processes.items[pid] = &process
	processes.Unlock()

	// register logs consumers
	process.pumper.AddConsumer(fileLogger)
	process.pumper.AddConsumer(&process)

	if process.beforeEventsHook != nil {
		process.beforeEventsHook(process)
	}

	// before pumping is started publish process_started event
	startPublished := make(chan bool)
	go func() {
		process.notifySubs(newStartedEvent(process), ProcessStatusBit)
		startPublished <- true
	}()

	// start pumping after start event is published 'pumper.Pump' is blocking
	go func() {
		<-startPublished
		process.pumper.Pump()
	}()

	return process, nil
}

// Gets process by pid.
// If process doesn't exist then error of type NoProcessError is returned.
func Get(pid uint64) (MachineProcess, error) {
	p, ok := directGet(pid)
	if ok {
		return *p, nil
	}
	return MachineProcess{}, noProcess(pid)
}

func GetProcesses(all bool) []MachineProcess {
	processes.RLock()
	defer processes.RUnlock()

	pArr := make([]MachineProcess, 0, len(processes.items))
	for _, p := range processes.items {
		if all {
			pArr = append(pArr, *p)
		} else {
			p.mutex.RLock()
			if p.Alive {
				pArr = append(pArr, *p)
			}
			p.mutex.RUnlock()
		}
	}
	return pArr
}

// Kills process by given pid.
// Returns an error when any error occurs during process kill.
// If process doesn't exist error of type NoProcessError is returned.
func Kill(pid uint64) error {
	p, ok := directGet(pid)
	if !ok {
		return noProcess(pid)
	}
	if !p.Alive {
		return notAlive(pid)
	}
	// workaround for killing child processes see https://github.com/golang/go/issues/8854
	return syscall.Kill(-p.NativePid, syscall.SIGKILL)
}

// Reads process logs between [from, till] inclusive.
// Returns an error if any error occurs during logs reading.
// If process doesn't exist error of type NoProcessError is returned.
func ReadLogs(pid uint64, from time.Time, till time.Time) ([]*LogMessage, error) {
	p, ok := directGet(pid)
	if !ok {
		return nil, noProcess(pid)
	}
	fl := p.fileLogger
	if p.Alive {
		fl.Flush()
	}
	return NewLogsReader(p.logfileName).From(from).Till(till).ReadLogs()
}

// Reads all process logs.
// Returns an error if any error occurs during logs reading.
// If process doesn't exist error of type NoProcessError is returned.
func ReadAllLogs(pid uint64) ([]*LogMessage, error) {
	return ReadLogs(pid, time.Time{}, time.Now())
}

// Unsubscribe subscriber with given id from process events.
// If process doesn't exist then error of type NoProcessError is returned.
func RemoveSubscriber(pid uint64, id string) error {
	p, ok := directGet(pid)
	if !ok {
		return noProcess(pid)
	}
	if !p.Alive {
		return notAlive(pid)
	}
	p.mutex.Lock()
	defer p.mutex.Unlock()
	for idx, sub := range p.subs {
		if sub.Id == id {
			p.subs = append(p.subs[0:idx], p.subs[idx+1:]...)
			break
		}
	}
	return nil
}

// Subscribe to the process output.
// An error of type NoProcessError is returned when process
// with given pid doesn't exist, a regular error is returned
// if the process is dead or subscriber with such id already subscribed
// to the process output.
func AddSubscriber(pid uint64, subscriber Subscriber) error {
	p, ok := directGet(pid)
	if !ok {
		return noProcess(pid)
	}
	p.mutex.Lock()
	defer p.mutex.Unlock()
	if !p.Alive && p.NativePid != 0 {
		return errors.New("Can't subscribe to the events of dead process")
	}
	for _, sub := range p.subs {
		if sub.Id == subscriber.Id {
			return errors.New("Already subscribed")
		}
	}
	p.subs = append(p.subs, &subscriber)
	return nil
}

// Adds a new process subscriber by reading all the logs between
// given 'after' and now and publishing them to the channel.
// Returns an error of type NoProcessError if process with given id doesn't exist,
// returns a regular error if process is alive an subscriber with such id
// already subscribed.
func RestoreSubscriber(pid uint64, subscriber Subscriber, after time.Time) error {
	p, ok := directGet(pid)
	if !ok {
		return noProcess(pid)
	}
	p.mutex.Lock()
	defer p.mutex.Unlock()

	// Read logs between after and now
	logs, err := ReadLogs(pid, after, time.Now())
	if err != nil {
		return err
	}

	// If process is dead there is no need to subscribe to it
	// as it is impossible to get it alive again, but it is still
	// may be useful for client to get missed logs, that's why this
	// function doesn't throw any errors in the case of dead process
	if p.Alive {
		for _, sub := range p.subs {
			if sub.Id == subscriber.Id {
				return errors.New("Already subscribed")
			}
		}
		p.subs = append(p.subs, &subscriber)
	}

	// Publish all the logs between (after, now]
	for i := 0; i < len(logs); i++ {
		message := logs[i]
		if message.Time.After(after) {
			if message.Kind == StdoutKind {
				subscriber.Channel <- newStdoutEvent(p.Pid, message.Text, message.Time)
			} else {
				subscriber.Channel <- newStderrEvent(p.Pid, message.Text, message.Time)
			}
		}
	}

	// Publish died event after logs are published and process is dead
	if !p.Alive {
		subscriber.Channel <- newDiedEvent(*p)
	}

	return nil
}

// Updates subscriber with given id.
// An error of type NoProcessError is returned when process
// with given pid doesn't exist, a regular error is returned
// if the process is dead.
func UpdateSubscriber(pid uint64, id string, newMask uint64) error {
	p, ok := directGet(pid)
	if !ok {
		return noProcess(pid)
	}
	if !p.Alive {
		return notAlive(pid)
	}
	p.mutex.Lock()
	defer p.mutex.Unlock()
	for _, sub := range p.subs {
		if sub.Id == id {
			sub.Mask = newMask
			return nil
		}
	}
	return errors.New(fmt.Sprintf("No subscriber with id '%s'", id))
}

func (process *MachineProcess) OnStdout(line string, time time.Time) {
	process.notifySubs(newStdoutEvent(process.Pid, line, time), StdoutBit)
}

func (process *MachineProcess) OnStderr(line string, time time.Time) {
	process.notifySubs(newStderrEvent(process.Pid, line, time), StderrBit)
}

func (mp *MachineProcess) Close() {
	// Cleanup command resources
	mp.command.Wait()
	// Cleanup machine process resources before dead event is sent
	mp.mutex.Lock()
	mp.Alive = false
	mp.command = nil
	mp.pumper = nil
	mp.mutex.Unlock()

	mp.notifySubs(newDiedEvent(*mp), ProcessStatusBit)

	mp.mutex.Lock()
	mp.subs = nil
	mp.mutex.Unlock()

	mp.updateLastUsedTime()
}

func (p *MachineProcess) notifySubs(event *rpc.Event, typeBit uint64) {
	p.mutex.RLock()
	subs := p.subs
	for _, subscriber := range subs {
		// Check whether subscriber needs such kind of event and then try to notify it
		if subscriber.Mask&typeBit == typeBit && !tryWrite(subscriber.Channel, event) {
			// Impossible to write to the channel, remove the channel from the subscribers list.
			// It may happen when writing to the closed channel
			defer RemoveSubscriber(p.Pid, subscriber.Id)
		}
	}
	p.mutex.RUnlock()
}

func (mp *MachineProcess) updateLastUsedTime() {
	mp.lastUsedLock.Lock()
	mp.lastUsed = time.Now()
	mp.lastUsedLock.Unlock()
}

// Writes to a channel and returns true if write is successful,
// otherwise if write to the channel failed e.g. channel is closed then returns false
func tryWrite(eventsChan chan *rpc.Event, event *rpc.Event) (ok bool) {
	defer func() {
		if r := recover(); r != nil {
			ok = false
		}
	}()
	eventsChan <- event
	return true
}

func directGet(pid uint64) (*MachineProcess, bool) {
	processes.RLock()
	defer processes.RUnlock()
	item, ok := processes.items[pid]
	if ok {
		item.updateLastUsedTime()
	}
	return item, ok
}

// Returns an error indicating that process with given pid doesn't exist
func noProcess(pid uint64) *NoProcessError {
	return &NoProcessError{
		error: errors.New(fmt.Sprintf("Process with id '%d' does not exist", pid)),
		Pid:   pid,
	}
}

// Returns an error indicating that process with given pid is not alive
func notAlive(pid uint64) *NotAliveError {
	return &NotAliveError{
		error: errors.New(fmt.Sprintf("Process with id '%d' is not alive", pid)),
		Pid:   pid,
	}
}
