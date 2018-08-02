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
	"errors"
	"fmt"
	"log"
	"os"
	"os/exec"
	"runtime"
	"sync"
	"sync/atomic"
	"syscall"
	"time"
)

const (
	// StdoutBit is set when subscriber is interested in stdout logs
	StdoutBit = 1 << iota
	// StderrBit is set when subscriber is interested in stdout logs
	StderrBit = 1 << iota
	// StatusBit is set when subscriber is interested in a process events
	StatusBit = 1 << iota
	// DefaultMask is set by default and identifies receiving of both logs and events of a process
	DefaultMask = StderrBit | StdoutBit | StatusBit

	// DefaultShellInterpreter is default shell that executes commands
	// unless another one is configured
	DefaultShellInterpreter = "/bin/bash"
)

var (
	prevPid uint64

	// the directory under which all the logs are written
	logsDir string

	// in memory storage of alive & dead processes
	processes = &processesMap{items: make(map[uint64]*MachineProcess)}

	// used by process to point to the file for a process with given pid
	logsDistributor LogsDistributor = NewLogsDistributor()

	// shell that executes commands
	shellInterpreter = DefaultShellInterpreter
)

// SetLogsDir sets the path to the directory to write logs to.
func SetLogsDir(dir string) {
	logsDir = dir
}

// WipeLogs removes logs dir and all the files and directories under it.
func WipeLogs() error {
	return os.RemoveAll(logsDir)
}

// SetLogsDistributor changes the default strategy of logs distribution to a given one.
func SetLogsDistributor(ld LogsDistributor) {
	if ld != nil {
		logsDistributor = ld
	}
}

// SetShellInterpreter changes the default
// shell interpreter which is '/bin/bash' to the given one.
func SetShellInterpreter(si string) {
	if si != "" {
		shellInterpreter = si
	}
}

// Command represents command that is used in command execution API.
type Command struct {
	Name        string `json:"name"`
	CommandLine string `json:"commandLine"`
	Type        string `json:"type"`
}

// MachineProcess defines machine process model.
type MachineProcess struct {
	// The virtual id of the process, it is guaranteed that pid
	// is always unique, while NativePid may occur twice or more(when including dead processes).
	Pid uint64 `json:"pid"`

	// The name of the process, it is equal to the Command.Name which this process created from.
	// It doesn't have to be unique, at least machine agent doesn't need such constraint,
	// as pid is used for identifying process.
	Name string `json:"name"`

	// The command line executed by this process.
	// It is equal to the Command.CommandLine which this process created from.
	CommandLine string `json:"commandLine"`

	// The type of the command line, this field is rather useful meta
	// information  than something used for functioning. It is equal
	// to the Command.Type which this process created from.
	Type string `json:"type"`

	// Whether this process is alive or dead.
	Alive bool `json:"alive"`

	// The native(OS) pid, it is unique per alive processes,
	// but those which are not alive, may have the same NativePid.
	NativePid int `json:"nativePid"`

	// The exit code of the process.
	// The value is set after the process died, the value is -1 while the process is alive.
	ExitCode int `json:"exitCode"`

	// Process log filename.
	logfileName string

	// Command executed by this process.
	// If process is not alive then the command value is set to nil.
	command *exec.Cmd

	// Stdout/stderr pumper.
	// If process is not alive then the pumper value is set to nil.
	pumper *LogsPumper

	// Process subscribers, all the outgoing events are go through those subscribers.
	// If process is not alive then the subscribers value is set to nil.
	subs []*Subscriber

	// Process file logger.
	// The value is set only if process logs directory is configured.
	fileLogger *FileLogger

	// Process mutex should be used to sync process data
	// or block on process related operations such as events publications.
	mutex *sync.RWMutex

	// The time when the process died.
	deathTime time.Time

	// Called once before any of process events is published
	// and after process is started.
	beforeEventsHook func(process MachineProcess)
}

// Subscriber receives process events.
type Subscriber struct {
	ID       string
	Mask     uint64
	Consumer EventConsumer
}

// NoProcessError is returned when requested process doesn't exist.
type NoProcessError struct {
	error
	Pid uint64
}

// NotAliveError is returned when process that is target of an action is not alive anymore.
type NotAliveError struct {
	error
	Pid uint64
}

// Lockable map for storing processes.
type processesMap struct {
	sync.RWMutex
	items map[uint64]*MachineProcess
}

// Start starts MachineProcess.
func Start(newProcess MachineProcess) (MachineProcess, error) {

	var cmd *exec.Cmd
	if runtime.GOOS == "linux" { // also can be specified to FreeBSD
		// wrap command to be able to kill child processes see https://github.com/golang/go/issues/8854
		cmd = exec.Command("setsid", shellInterpreter, "-c", newProcess.CommandLine)
	} else {
		cmd = exec.Command(shellInterpreter, "-c", newProcess.CommandLine)
	}

	// getting stdout pipe
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		return newProcess, err
	}

	// getting stderr pipe
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return newProcess, err
	}

	// starting a new process
	err = cmd.Start()
	if err != nil {
		return newProcess, err
	}

	// increment current pid & assign it to the value
	pid := atomic.AddUint64(&prevPid, 1)

	// set shared data
	newProcess.Pid = pid
	newProcess.Alive = true
	newProcess.NativePid = cmd.Process.Pid
	newProcess.ExitCode = -1

	// create an internal copy of the new process
	internalProcess := newProcess

	pumper := NewPumper(stdout, stderr)
	fileLogger, err := newFileLogger(pid)
	if err != nil {
		return newProcess, err
	}

	// set internal data
	internalProcess.command = cmd
	internalProcess.pumper = pumper
	internalProcess.mutex = &sync.RWMutex{}
	if fileLogger != nil {
		internalProcess.fileLogger = fileLogger
		internalProcess.logfileName = fileLogger.filename
	}

	// register logs consumers
	if fileLogger != nil {
		pumper.AddConsumer(fileLogger)
	}
	pumper.AddConsumer(&internalProcess)

	// save(publish) process instance
	processes.Lock()
	processes.items[pid] = &internalProcess
	processes.Unlock()

	if newProcess.beforeEventsHook != nil {
		newProcess.beforeEventsHook(newProcess)
	}

	// before pumping is started publish process_started event
	startPublished := make(chan bool)
	go func() {
		internalProcess.notifySubs(newStartedEvent(newProcess), StatusBit)
		startPublished <- true
	}()

	// start pumping after start event is published 'pumper.Pump' is blocking
	go func() {
		<-startPublished
		pumper.Pump()
	}()

	return newProcess, nil
}

// Get retrieves process by pid.
// If process doesn't exist then error of type NoProcessError is returned.
func Get(pid uint64) (MachineProcess, error) {
	p, ok := directGet(pid)
	if !ok {
		return MachineProcess{}, noProcess(pid)
	}
	p.mutex.RLock()
	defer p.mutex.RUnlock()
	return *p, nil

}

// GetProcesses retrieves list of processes.
// If parameter all is true then returns all processes,
// otherwise returns only live processes.
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

// Kill kills process by given pid.
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

// ReadLogs reads process logs between [from, till] inclusive.
// Returns an error if any error occurs during logs reading.
// If process doesn't exist error of type NoProcessError is returned.
func ReadLogs(pid uint64, from time.Time, till time.Time) ([]*LogMessage, error) {
	p, ok := directGet(pid)
	if !ok {
		return nil, noProcess(pid)
	}

	p.mutex.RLock()
	reader, err := newLogsReader(p, from, till)
	p.mutex.RUnlock()

	if err != nil {
		return nil, err
	}
	return reader.ReadLogs()
}

// ReadAllLogs reads all process logs.
// Returns an error if any error occurs during logs reading.
// If process doesn't exist error of type NoProcessError is returned.
func ReadAllLogs(pid uint64) ([]*LogMessage, error) {
	return ReadLogs(pid, time.Time{}, time.Now())
}

// RemoveSubscriber unsubscribes subscriber with given id from process events.
// If process doesn't exist then error of type NoProcessError is returned.
func RemoveSubscriber(pid uint64, id string) error {
	p, ok := directGet(pid)
	if !ok {
		return noProcess(pid)
	}

	p.mutex.Lock()
	defer p.mutex.Unlock()

	if !p.Alive {
		return notAlive(pid)
	}
	for idx, sub := range p.subs {
		if sub.ID == id {
			p.subs = append(p.subs[0:idx], p.subs[idx+1:]...)
			break
		}
	}
	return nil
}

// AddSubscriber subscribes provided subscriber to the process output.
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
		if sub.ID == subscriber.ID {
			return errors.New("Already subscribed")
		}
	}
	p.subs = append(p.subs, &subscriber)
	return nil
}

// RestoreSubscriber adds a new process subscriber by reading all the logs between
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
	var logs []*LogMessage
	reader, err := newLogsReader(p, after, time.Now())
	if err == nil {
		if logs, err = reader.ReadLogs(); err != nil {
			return err
		}
	}

	// If process is dead there is no need to subscribe to it
	// as it is impossible to get it alive again, but it is still
	// may be useful for client to get missed logs, that's why this
	// function doesn't throw any errors in the case of dead process
	if p.Alive {
		for _, sub := range p.subs {
			if sub.ID == subscriber.ID {
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
				subscriber.Consumer.Accept(newStdoutEvent(p.Pid, message.Text, message.Time))
			} else {
				subscriber.Consumer.Accept(newStderrEvent(p.Pid, message.Text, message.Time))
			}
		}
	}

	// Publish died event after logs are published and process is dead
	if !p.Alive {
		subscriber.Consumer.Accept(newDiedEvent(*p))
	}

	return nil
}

// UpdateSubscriber updates subscriber with given id.
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
		if sub.ID == id {
			sub.Mask = newMask
			return nil
		}
	}
	return fmt.Errorf("No subscriber with id '%s'", id)
}

// OnStdout notifies subscribers about new output in stdout.
func (process *MachineProcess) OnStdout(line string, time time.Time) {
	process.notifySubs(newStdoutEvent(process.Pid, line, time), StdoutBit)
}

// OnStderr notifies subscribers about new output in stderr.
func (process *MachineProcess) OnStderr(line string, time time.Time) {
	process.notifySubs(newStderrEvent(process.Pid, line, time), StderrBit)
}

// Close cleanups process resources and notifies subscribers about process death.
func (process *MachineProcess) Close() {
	// Cleanup command resources
	exitCode := 0
	if err := process.command.Wait(); err != nil {
		if exiterr, ok := err.(*exec.ExitError); ok {
			status := exiterr.Sys().(syscall.WaitStatus)
			exitCode = status.ExitStatus()
		} else {
			log.Printf("Error occurs on process cleanup. %s", err)
		}
	}
	// Cleanup machine process resources before dead event is sent
	process.mutex.Lock()
	process.Alive = false
	process.deathTime = time.Now()
	process.command = nil
	process.pumper = nil
	process.fileLogger = nil
	process.ExitCode = exitCode
	process.mutex.Unlock()

	process.notifySubs(newDiedEvent(*process), StatusBit)

	process.mutex.Lock()
	process.subs = nil
	process.mutex.Unlock()
}

func (process *MachineProcess) notifySubs(event Event, typeBit uint64) {
	process.mutex.RLock()
	subs := process.subs
	for _, subscriber := range subs {
		// Check whether subscriber needs such kind of event and then try to notify it
		if subscriber.Mask&typeBit == typeBit && !tryAccept(subscriber.Consumer, event) {
			// Impossible to write to the channel, remove the channel from the subscribers list.
			// It may happen when writing to the closed channel
			defer RemoveSubscriber(process.Pid, subscriber.ID)
		}
	}
	process.mutex.RUnlock()
}

// Writes to a channel and returns true if write is successful,
// otherwise if write to the channel failed e.g. channel is closed then returns false.
func tryAccept(consumer EventConsumer, event Event) (ok bool) {
	defer func() {
		if r := recover(); r != nil {
			ok = false
		}
	}()
	consumer.Accept(event)
	return true
}

func directGet(pid uint64) (*MachineProcess, bool) {
	processes.RLock()
	defer processes.RUnlock()
	item, ok := processes.items[pid]
	return item, ok
}

// Creates a new logs reader for given process.
func newLogsReader(p *MachineProcess, from time.Time, till time.Time) (*LogsReader, error) {
	if p.logfileName == "" {
		return nil, fmt.Errorf("Logs file for process '%d' is missing", p.Pid)
	}
	if p.Alive {
		p.fileLogger.Flush()
	}
	return NewLogsReader(p.logfileName).From(from).Till(till), nil
}

// Creates a new file logger for given pid.
func newFileLogger(pid uint64) (*FileLogger, error) {
	if logsDir == "" {
		return nil, nil
	}

	// Figure out the place for logs file
	dir, err := logsDistributor.DirForPid(logsDir, pid)
	if err != nil {
		return nil, err
	}

	// TODO extract file process name generation to the strategy interface(consider LogsDistributor)
	filename := fmt.Sprintf("%s%cpid-%d", dir, os.PathSeparator, pid)
	fileLogger, err := NewLogger(filename)
	if err != nil {
		return nil, err
	}
	return fileLogger, nil
}

// Returns an error indicating that process with given pid doesn't exist.
func noProcess(pid uint64) *NoProcessError {
	return &NoProcessError{
		error: fmt.Errorf("Process with id '%d' does not exist", pid),
		Pid:   pid,
	}
}

// Returns an error indicating that process with given pid is not alive
func notAlive(pid uint64) *NotAliveError {
	return &NotAliveError{
		error: fmt.Errorf("Process with id '%d' is not alive", pid),
		Pid:   pid,
	}
}
