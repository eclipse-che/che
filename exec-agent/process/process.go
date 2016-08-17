package process

import (
	"errors"
	"flag"
	"fmt"
	"github.com/eclipse/che/exec-agent/op"
	"os"
	"os/exec"
	"sync"
	"sync/atomic"
	"syscall"
	"time"
)

const (
	StdoutBit        = 1 << iota
	StderrBit        = 1 << iota
	ProcessStatusBit = 1 << iota
	DefaultMask      = StderrBit | StdoutBit | ProcessStatusBit

	DateTimeFormat = time.RFC3339Nano

	StdoutKind = "STDOUT"
	StderrKind = "STDERR"
)

var (
	prevPid   uint64 = 0
	processes        = &processesMap{items: make(map[uint64]*MachineProcess)}
	logsDist         = NewLogsDistributor()
	LogsDir   string
)

type Command struct {
	Name        string `json:"name"`
	CommandLine string `json:"commandLine"`
	Type        string `json:"type"`
}

// Defines machine process model
type MachineProcess struct {
	// The virtual id of the process, it is guaranteed  that pid
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

	mutex sync.RWMutex

	// When the process was last time used by client
	lastUsed time.Time

	// Called once before any of process events is published
	// and after process is started
	beforeEventsHook func(process *MachineProcess)
}

type Subscriber struct {
	Id      string
	Mask    uint64
	Channel chan *op.Event
}

type LogMessage struct {
	Kind string    `json:"kind"`
	Time time.Time `json:"time"`
	Text string    `json:"text"`
}

// Lockable map for storing processes
type processesMap struct {
	sync.RWMutex
	items map[uint64]*MachineProcess
}

func init() {
	curDir, _ := os.Getwd()
	curDir += string(os.PathSeparator) + "logs"
	flag.StringVar(&LogsDir, "logs-dir", curDir, "Base directory for process logs")
}

func NewProcess(newCommand Command) *MachineProcess {
	return &MachineProcess{
		Name:        newCommand.Name,
		CommandLine: newCommand.CommandLine,
		Type:        newCommand.Type,
	}
}

// Sets the hook which will be called once before
// process subscribers notified with any of the process events,
// and after process is started.
func (process *MachineProcess) BeforeEventsHook(f func(p *MachineProcess)) *MachineProcess {
	process.beforeEventsHook = f
	return process
}

func (process *MachineProcess) Start() error {
	// wrap command to be able to kill child processes see https://github.com/golang/go/issues/8854
	cmd := exec.Command("setsid", "sh", "-c", process.CommandLine)

	// getting stdout pipe
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		return err
	}

	// getting stderr pipe
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return err
	}

	// starting a new process
	err = cmd.Start()
	if err != nil {
		return err
	}

	// increment current pid & assign it to the value
	pid := atomic.AddUint64(&prevPid, 1)

	// Figure out the place for logs file
	dir, err := logsDist.DirForPid(LogsDir, pid)
	if err != nil {
		return err
	}
	filename := fmt.Sprintf("%s%cpid-%d", dir, os.PathSeparator, pid)

	fileLogger, err := NewLogger(filename)
	if err != nil {
		return err
	}

	// save process
	process.Pid = pid
	process.Alive = true
	process.NativePid = cmd.Process.Pid
	process.command = cmd
	process.pumper = NewPumper(stdout, stderr)
	process.logfileName = filename
	process.fileLogger = fileLogger
	process.lastUsed = time.Now()

	processes.Lock()
	processes.items[pid] = process
	processes.Unlock()

	// register logs consumers
	process.pumper.AddConsumer(fileLogger)
	process.pumper.AddConsumer(process)

	if process.beforeEventsHook != nil {
		process.beforeEventsHook(process)
	}

	// before pumping is started publish process_started event
	startPublished := make(chan bool)
	go func () {
		body := &ProcessStatusEventBody{
			ProcessEventBody: ProcessEventBody{Pid: process.Pid},
			NativePid:        process.NativePid,
			Name:             process.Name,
			CommandLine:      process.CommandLine,
		}
		process.notifySubs(op.NewEventNow(ProcessStartedEventType, body), ProcessStatusBit)
		startPublished <- true
	}();


	// start pumping after start event is published 'pumper.Pump' is blocking
	go func() {
		<- startPublished
		process.pumper.Pump()
	}()

	return nil
}

func Get(pid uint64) (*MachineProcess, bool) {
	processes.RLock()
	defer processes.RUnlock()
	item, ok := processes.items[pid]
	return item, ok
}

func GetProcesses(all bool) []*MachineProcess {
	processes.RLock()
	defer processes.RUnlock()

	pArr := make([]*MachineProcess, 0, len(processes.items))
	for _, v := range processes.items {
		if all || v.Alive {
			pArr = append(pArr, v)
		}
	}
	return pArr
}

func (mp *MachineProcess) Kill() error {
	// workaround for killing child processes see https://github.com/golang/go/issues/8854
	return syscall.Kill(-mp.NativePid, syscall.SIGKILL)
}

func (mp *MachineProcess) ReadLogs(from time.Time, till time.Time) ([]*LogMessage, error) {
	mp.mutex.Lock()
	mp.lastUsed = time.Now()
	mp.mutex.Unlock()
	fl := mp.fileLogger
	if mp.Alive {
		fl.Flush()
	}
	return NewLogsReader(mp.logfileName).From(from).Till(till).ReadLogs()
}

func (mp *MachineProcess) RemoveSubscriber(id string) {
	mp.mutex.Lock()
	defer mp.mutex.Unlock()
	mp.lastUsed = time.Now()
	for idx, sub := range mp.subs {
		if sub.Id == id {
			mp.subs = append(mp.subs[0:idx], mp.subs[idx+1:]...)
			break
		}
	}
}

func (mp *MachineProcess) AddSubscriber(subscriber *Subscriber) error {
	mp.mutex.Lock()
	defer mp.mutex.Unlock()
	if !mp.Alive && mp.NativePid != 0 {
		return errors.New("Can't subscribe to the events of dead process")
	}
	for _, sub := range mp.subs {
		if sub.Id == subscriber.Id {
			return errors.New("Already subscribed")
		}
	}
	mp.subs = append(mp.subs, subscriber)
	return nil
}

// Adds a new process subscriber by reading all the logs between
// given 'after' and now and publishing them to the channel
func (mp *MachineProcess) RestoreSubscriber(subscriber *Subscriber, after time.Time) error {
	mp.mutex.Lock()
	defer mp.mutex.Unlock()

	mp.lastUsed = time.Now()

	// Read logs between after and now
	logs, err := mp.ReadLogs(after, time.Now())
	if err != nil {
		return err
	}

	// If process is dead there is no need to subscribe to it
	// as it is impossible to get it alive again, but it is still
	// may be useful for client to get missed logs, that's why this
	// function doesn't throw any errors in the case of dead process
	if mp.Alive {
		for _, sub := range mp.subs {
			if sub.Id == subscriber.Id {
				return errors.New("Already subscribed")
			}
		}
		mp.subs = append(mp.subs, subscriber)
	}

	// Publish all the logs between (after, now]
	for i := 1; i < len(logs); i++ {
		message := logs[i]
		subscriber.Channel <- newOutputEvent(mp.Pid, message.Kind, message.Text, message.Time)
	}

	return nil
}

func (mp *MachineProcess) UpdateSubscriber(id string, newMask uint64) error {
	mp.mutex.Lock()
	defer mp.mutex.Unlock()
	if !mp.Alive {
		return errors.New("Can't update subscriber, the process is dead")
	}
	for _, sub := range mp.subs {
		if sub.Id == id {
			sub.Mask = newMask
			break
		}
	}
	return nil
}

func (process *MachineProcess) OnStdout(line string, time time.Time) {
	process.notifySubs(newOutputEvent(process.Pid, StdoutEventType, line, time), StdoutBit)
}

func (process *MachineProcess) OnStderr(line string, time time.Time) {
	process.notifySubs(newOutputEvent(process.Pid, StderrEventType, line, time), StderrBit)
}

func (mp *MachineProcess) Close() {
	// Cleanup command resources
	mp.command.Wait()
	// Cleanup machine process resources before dead event is sent
	mp.mutex.Lock()
	mp.lastUsed = time.Now()
	mp.Alive = false
	mp.command = nil
	mp.pumper = nil
	mp.mutex.Unlock()

	body := &ProcessStatusEventBody{
		ProcessEventBody: ProcessEventBody{Pid: mp.Pid},
		NativePid:        mp.NativePid,
		Name:             mp.Name,
		CommandLine:      mp.CommandLine,
	}
	mp.notifySubs(op.NewEventNow(ProcessDiedEventType, body), ProcessStatusBit)

	mp.mutex.Lock()
	mp.subs = nil
	mp.mutex.Unlock()
}

func (mp *MachineProcess) notifySubs(event *op.Event, typeBit uint64) {
	mp.mutex.RLock()
	defer mp.mutex.RUnlock()
	subs := mp.subs
	for _, subscriber := range subs {
		// Check whether subscriber needs such kind of event and then try to notify it
		if subscriber.Mask&typeBit == typeBit && !tryWrite(subscriber.Channel, event) {
			// Impossible to write to the channel, remove the channel from the subscribers list.
			// It may happen when writing to the closed channel
			defer mp.RemoveSubscriber(subscriber.Id)
		}
	}
}

// Writes to a channel and returns true if write is successful,
// otherwise if write to the channel failed e.g. channel is closed then returns false
func tryWrite(eventsChan chan *op.Event, event *op.Event) (ok bool) {
	defer func() {
		if r := recover(); r != nil {
			ok = false
		}
	}()
	eventsChan <- event
	return true
}

func newOutputEvent(pid uint64, kind string, line string, time time.Time) *op.Event {
	body := &ProcessOutputEventBody{
		ProcessEventBody: ProcessEventBody{Pid: pid},
		Text:             line,
	}
	return op.NewEvent(kind, body, time)
}
