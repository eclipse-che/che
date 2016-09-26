package process

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/eclipse/che/exec-agent/rpc"
	"math"
	"time"
)

const (
	StartMethod            = "process.start"
	KillMethod             = "process.kill"
	SubscribeMethod        = "process.subscribe"
	UnsubscribeMethod      = "process.unsubscribe"
	UpdateSubscriberMethod = "process.updateSubscriber"
	GetLogsMethod          = "process.getLogs"
	GetProcessMethod       = "process.getProcess"
	GetProcessesMethod     = "process.getProcesses"

	NoSuchProcessErrorCode = -32000
)

var RpcRoutes = rpc.RoutesGroup{
	"Process Routes",
	[]rpc.Route{
		{
			StartMethod,
			func(body []byte) (interface{}, error) {
				b := StartParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			startProcessReqHF,
		},
		{
			KillMethod,
			func(body []byte) (interface{}, error) {
				b := KillParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			killProcessReqHF,
		},
		{
			SubscribeMethod,
			func(body []byte) (interface{}, error) {
				b := SubscribeParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			subscribeReqHF,
		},
		{
			UnsubscribeMethod,
			func(body []byte) (interface{}, error) {
				b := UnsubscribeParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			unsubscribeReqHF,
		},
		{
			UpdateSubscriberMethod,
			func(body []byte) (interface{}, error) {
				b := UpdateSubscriberParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			updateSubscriberReqHF,
		},
		{
			GetLogsMethod,
			func(body []byte) (interface{}, error) {
				b := GetLogsParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			getProcessLogsReqHF,
		},
		{
			GetProcessMethod,
			func(body []byte) (interface{}, error) {
				b := GetProcessParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			getProcessReqHF,
		},
		{
			GetProcessesMethod,
			func(body []byte) (interface{}, error) {
				b := GetProcessesParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			getProcessesReqHF,
		},
	},
}

type ProcessResult struct {
	Pid  uint64 `json:"pid"`
	Text string `json:"text"`
}

//-- process start
type StartParams struct {
	Name        string `json:"name"`
	CommandLine string `json:"commandLine"`
	Type        string `json:"type"`
	EventTypes  string `json:"eventTypes"`
}

func startProcessReqHF(body interface{}, t *rpc.Transmitter) error {
	startBody := body.(StartParams)

	// Creating command
	command := Command{
		Name:        startBody.Name,
		CommandLine: startBody.CommandLine,
		Type:        startBody.Type,
	}
	if err := checkCommand(&command); err != nil {
		return rpc.NewArgsError(err)
	}

	// Detecting subscription mask
	subscriber := &Subscriber{
		Id:      t.Channel.Id,
		Mask:    parseTypes(startBody.EventTypes),
		Channel: t.Channel.Events,
	}

	process := NewProcess(command).BeforeEventsHook(func(process *MachineProcess) {
		t.Send(process)
	})
	if subscriber != nil {
		if err := process.AddSubscriber(subscriber); err != nil {
			return err
		}
	}

	return process.Start()
}

//-- process kill
type KillParams struct {
	Pid       uint64 `json:"pid"`
	NativePid uint64 `json:"nativePid"`
}

func killProcessReqHF(body interface{}, t *rpc.Transmitter) error {
	killBody := body.(KillParams)
	p, ok := Get(killBody.Pid)
	if !ok {
		return newNoSuchProcessError(killBody.Pid)
	}
	if err := p.Kill(); err != nil {
		return err
	}
	t.Send(&ProcessResult{
		Pid:  killBody.Pid,
		Text: "Successfully killed",
	})
	return nil
}

//-- process subscribe
type SubscribeResult struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
	Text       string `json:"text"`
}

type SubscribeParams struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
	After      string `json:"after"`
}

func subscribeReqHF(body interface{}, t *rpc.Transmitter) error {
	subscribeBody := body.(SubscribeParams)
	p, ok := Get(subscribeBody.Pid)
	if !ok {
		return newNoSuchProcessError(subscribeBody.Pid)
	}

	subscriber := &Subscriber{
		Id:      t.Channel.Id,
		Mask:    parseTypes(subscribeBody.EventTypes),
		Channel: t.Channel.Events,
	}

	// Check whether subscriber should see previous logs or not
	if subscribeBody.After == "" {
		if err := p.AddSubscriber(subscriber); err != nil {
			return err
		}
	} else {
		after, err := time.Parse(DateTimeFormat, subscribeBody.After)
		if err != nil {
			return rpc.NewArgsError(errors.New("Bad format of 'after', " + err.Error()))
		}
		if err := p.RestoreSubscriber(subscriber, after); err != nil {
			return err
		}
	}
	t.Send(&SubscribeResult{
		Pid:        p.Pid,
		EventTypes: subscribeBody.EventTypes,
		Text:       "Successfully subscribed",
	})
	return nil
}

//-- process unsubscribe
type UnsubscribeParams struct {
	Pid uint64 `json:"pid"`
}

func unsubscribeReqHF(call interface{}, t *rpc.Transmitter) error {
	unsubscribeBody := call.(UnsubscribeParams)
	p, ok := Get(unsubscribeBody.Pid)
	if !ok {
		return errors.New(fmt.Sprintf("Process with id '%s' doesn't exist", unsubscribeBody.Pid))
	}
	p.RemoveSubscriber(t.Channel.Id)
	t.Send(&ProcessResult{
		Pid:  p.Pid,
		Text: "Successfully unsubscribed",
	})
	return nil
}

//-- process update subscriber
type UpdateSubscriberParams struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
}

func updateSubscriberReqHF(body interface{}, t *rpc.Transmitter) error {
	updateBody := body.(UpdateSubscriberParams)
	p, ok := Get(updateBody.Pid)
	if !ok {
		return newNoSuchProcessError(updateBody.Pid)
	}
	if updateBody.EventTypes == "" {
		return rpc.NewArgsError(errors.New("'eventTypes' required for subscriber update"))
	}

	if err := p.UpdateSubscriber(t.Channel.Id, maskFromTypes(updateBody.EventTypes)); err != nil {
		return err
	}
	t.Send(&SubscribeResult{
		Pid:        p.Pid,
		EventTypes: updateBody.EventTypes,
		Text:       "Subscriber successfully updated",
	})
	return nil
}

//-- process get logs
type GetLogsParams struct {
	Pid   uint64 `json:"pid"`
	From  string `json:"from"`
	Till  string `json:"till"`
	Limit int    `json:"limit"`
	Skip  int    `json:"skip"`
}

func getProcessLogsReqHF(body interface{}, t *rpc.Transmitter) error {
	args := body.(GetLogsParams)
	p, ok := Get(args.Pid)
	if !ok {
		return newNoSuchProcessError(args.Pid)
	}

	from, err := parseTime(args.From, time.Time{})
	if err != nil {
		return rpc.NewArgsError(errors.New("Bad format of 'from', " + err.Error()))
	}

	till, err := parseTime(args.Till, time.Now())
	if err != nil {
		return rpc.NewArgsError(errors.New("Bad format of 'till', " + err.Error()))
	}

	logs, err := p.ReadLogs(from, till)
	if err != nil {
		return err
	}

	limit := DefaultLogsPerPageLimit
	if args.Limit != 0 {
		if limit < 1 {
			return rpc.NewArgsError(errors.New("Required 'limit' to be > 0"))
		}
		limit = args.Limit
	}

	skip := 0
	if args.Skip != 0 {
		if skip < 0 {
			return rpc.NewArgsError(errors.New("Required 'skip' to be >= 0"))
		}
		skip = args.Skip
	}

	logsLen := len(logs)
	fromIdx := int(math.Max(float64(logsLen-limit-skip), 0))
	toIdx := logsLen - int(math.Min(float64(skip), float64(logsLen)))

	t.Send(logs[fromIdx:toIdx])
	return nil
}

//-- get process
type GetProcessParams struct {
	Pid uint64 `json:"pid"`
}

func getProcessReqHF(body interface{}, t *rpc.Transmitter) error {
	params := body.(GetProcessParams)
	mp, ok := Get(params.Pid)
	if !ok {
		return newNoSuchProcessError(params.Pid)
	}
	t.Send(mp)
	return nil
}

//-- get processes
type GetProcessesParams struct {
	All bool `json:"all"`
}

func getProcessesReqHF(body interface{}, t *rpc.Transmitter) error {
	params := body.(GetProcessesParams)
	t.Send(GetProcesses(params.All))
	return nil
}

func newNoSuchProcessError(pid uint64) rpc.Error {
	return rpc.NewError(errors.New(fmt.Sprintf("No process with id '%d'", pid)), NoSuchProcessErrorCode)
}
