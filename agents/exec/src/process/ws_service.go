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
	"encoding/json"
	"errors"
	"github.com/eclipse/che/agents/exec-agent/rpc"
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

	NoSuchProcessErrorCode   = -32000
	ProcessNotAliveErrorCode = -32001
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

func startProcessReqHF(params interface{}, t *rpc.Transmitter) error {
	startParams := params.(StartParams)
	command := Command{
		Name:        startParams.Name,
		CommandLine: startParams.CommandLine,
		Type:        startParams.Type,
	}
	if err := checkCommand(&command); err != nil {
		return rpc.NewArgsError(err)
	}

	_, err := NewBuilder().
		Cmd(command).
		FirstSubscriber(Subscriber{
			Id:      t.Channel.Id,
			Mask:    parseTypes(startParams.EventTypes),
			Channel: t.Channel.Events,
		}).
		BeforeEventsHook(func(process MachineProcess) {
			t.Send(process)
		}).
		Start()
	return err
}

//-- process kill
type KillParams struct {
	Pid       uint64 `json:"pid"`
	NativePid uint64 `json:"nativePid"`
}

func killProcessReqHF(params interface{}, t *rpc.Transmitter) error {
	killParams := params.(KillParams)
	if err := Kill(killParams.Pid); err != nil {
		return asRpcError(err)
	}
	t.Send(&ProcessResult{
		Pid:  killParams.Pid,
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

func subscribeReqHF(params interface{}, t *rpc.Transmitter) error {
	subscribeParams := params.(SubscribeParams)

	mask := maskFromTypes(subscribeParams.EventTypes)
	if mask == 0 {
		return rpc.NewArgsError(errors.New("Required at least 1 valid event type"))
	}

	subscriber := Subscriber{
		Id:      t.Channel.Id,
		Mask:    mask,
		Channel: t.Channel.Events,
	}
	// Check whether subscriber should see previous logs or not
	if subscribeParams.After == "" {
		if err := AddSubscriber(subscribeParams.Pid, subscriber); err != nil {
			return asRpcError(err)
		}
	} else {
		after, err := time.Parse(DateTimeFormat, subscribeParams.After)
		if err != nil {
			return rpc.NewArgsError(errors.New("Bad format of 'after', " + err.Error()))
		}
		if err := RestoreSubscriber(subscribeParams.Pid, subscriber, after); err != nil {
			return err
		}
	}
	t.Send(&SubscribeResult{
		Pid:        subscribeParams.Pid,
		EventTypes: subscribeParams.EventTypes,
		Text:       "Successfully subscribed",
	})
	return nil
}

//-- process unsubscribe
type UnsubscribeParams struct {
	Pid uint64 `json:"pid"`
}

func unsubscribeReqHF(params interface{}, t *rpc.Transmitter) error {
	unsubscribeParams := params.(UnsubscribeParams)
	if err := RemoveSubscriber(unsubscribeParams.Pid, t.Channel.Id); err != nil {
		return asRpcError(err)
	}
	t.Send(&ProcessResult{
		Pid:  unsubscribeParams.Pid,
		Text: "Successfully unsubscribed",
	})
	return nil
}

//-- process update subscriber
type UpdateSubscriberParams struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
}

func updateSubscriberReqHF(params interface{}, t *rpc.Transmitter) error {
	updateParams := params.(UpdateSubscriberParams)
	if updateParams.EventTypes == "" {
		return rpc.NewArgsError(errors.New("'eventTypes' required for subscriber update"))
	}
	if err := UpdateSubscriber(updateParams.Pid, t.Channel.Id, maskFromTypes(updateParams.EventTypes)); err != nil {
		return asRpcError(err)
	}
	t.Send(&SubscribeResult{
		Pid:        updateParams.Pid,
		EventTypes: updateParams.EventTypes,
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

func getProcessLogsReqHF(params interface{}, t *rpc.Transmitter) error {
	getLogsParams := params.(GetLogsParams)

	if getLogsParams.Skip < 0 {
		getLogsParams.Skip = 0
	}
	if getLogsParams.Limit < 0 {
		getLogsParams.Limit = 0
	}

	from, err := parseTime(getLogsParams.From, time.Time{})
	if err != nil {
		return rpc.NewArgsError(errors.New("Bad format of 'from', " + err.Error()))
	}

	till, err := parseTime(getLogsParams.Till, time.Now())
	if err != nil {
		return rpc.NewArgsError(errors.New("Bad format of 'till', " + err.Error()))
	}

	logs, err := ReadLogs(getLogsParams.Pid, from, till)
	if err != nil {
		return asRpcError(err)
	}

	limit := DefaultLogsPerPageLimit
	if getLogsParams.Limit != 0 {
		if limit < 1 {
			return rpc.NewArgsError(errors.New("Required 'limit' to be > 0"))
		}
		limit = getLogsParams.Limit
	}

	skip := 0
	if getLogsParams.Skip != 0 {
		if skip < 0 {
			return rpc.NewArgsError(errors.New("Required 'skip' to be >= 0"))
		}
		skip = getLogsParams.Skip
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
	p, err := Get(params.Pid)
	if err != nil {
		return asRpcError(err)
	}
	t.Send(p)
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

func asRpcError(err error) error {
	if npErr, ok := err.(*NoProcessError); ok {
		return rpc.NewError(npErr.error, NoSuchProcessErrorCode)
	} else if naErr, ok := err.(*NotAliveError); ok {
		return rpc.NewError(naErr.error, ProcessNotAliveErrorCode)
	}
	return err
}
