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

package exec

import (
	"encoding/json"
	"errors"
	"math"
	"time"

	"github.com/eclipse/che/agents/go-agents/src/main/go/core/process"
	"github.com/eclipse/che/agents/go-agents/src/main/go/core/rpc"
)

// Constants that represent RPC methods identifiers
const (
	StartMethod            = "process.start"
	KillMethod             = "process.kill"
	SubscribeMethod        = "process.subscribe"
	UnsubscribeMethod      = "process.unsubscribe"
	UpdateSubscriberMethod = "process.updateSubscriber"
	GetLogsMethod          = "process.getLogs"
	GetProcessMethod       = "process.getProcess"
	GetProcessesMethod     = "process.getProcesses"
)

// Error codes
const (
	NoSuchProcessErrorCode   = -32000
	ProcessNotAliveErrorCode = -32001
)

// RPCRoutes provides all routes that should be handled by the process API
var RPCRoutes = rpc.RoutesGroup{
	Name: "Process Routes",
	Items: []rpc.Route{
		{
			Method: StartMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := StartParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: startProcessReqHF,
		},
		{
			Method: KillMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := KillParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: killProcessReqHF,
		},
		{
			Method: SubscribeMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := SubscribeParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: subscribeReqHF,
		},
		{
			Method: UnsubscribeMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := UnsubscribeParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: unsubscribeReqHF,
		},
		{
			Method: UpdateSubscriberMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := UpdateSubscriberParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: updateSubscriberReqHF,
		},
		{
			Method: GetLogsMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := GetLogsParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: getProcessLogsReqHF,
		},
		{
			Method: GetProcessMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := GetProcessParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: getProcessReqHF,
		},
		{
			Method: GetProcessesMethod,
			DecoderFunc: func(body []byte) (interface{}, error) {
				b := GetProcessesParams{}
				err := json.Unmarshal(body, &b)
				return b, err
			},
			HandlerFunc: getProcessesReqHF,
		},
	},
}

// ProcessResult represents result of start process call
type ProcessResult struct {
	Pid  uint64 `json:"pid"`
	Text string `json:"text"`
}

// StartParams represents params for start process call
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
			ID:      t.Channel.ID,
			Mask:    parseTypes(startParams.EventTypes),
			Channel: t.Channel.Events,
		}).
		BeforeEventsHook(func(process MachineProcess) {
			t.Send(process)
		}).
		Start()
	return err
}

// KillParams represents params for kill process call
type KillParams struct {
	Pid       uint64 `json:"pid"`
	NativePid uint64 `json:"nativePid"`
}

func killProcessReqHF(params interface{}, t *rpc.Transmitter) error {
	killParams := params.(KillParams)
	if err := Kill(killParams.Pid); err != nil {
		return asRPCError(err)
	}
	t.Send(&ProcessResult{
		Pid:  killParams.Pid,
		Text: "Successfully killed",
	})
	return nil
}

// SubscribeResult represents result of subscribe call
type SubscribeResult struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
	Text       string `json:"text"`
}

// SubscribeParams represents params for subscribe to events call
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
		ID:      t.Channel.ID,
		Mask:    mask,
		Channel: t.Channel.Events,
	}
	// Check whether subscriber should see previous logs or not
	if subscribeParams.After == "" {
		if err := AddSubscriber(subscribeParams.Pid, subscriber); err != nil {
			return asRPCError(err)
		}
	} else {
		after, err := time.Parse(process.DateTimeFormat, subscribeParams.After)
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

// UnsubscribeParams represents params for unsubscribe from events call
type UnsubscribeParams struct {
	Pid uint64 `json:"pid"`
}

func unsubscribeReqHF(params interface{}, t *rpc.Transmitter) error {
	unsubscribeParams := params.(UnsubscribeParams)
	if err := RemoveSubscriber(unsubscribeParams.Pid, t.Channel.ID); err != nil {
		return asRPCError(err)
	}
	t.Send(&ProcessResult{
		Pid:  unsubscribeParams.Pid,
		Text: "Successfully unsubscribed",
	})
	return nil
}

// UpdateSubscriberParams represents params for update subscribtion to events call
type UpdateSubscriberParams struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
}

func updateSubscriberReqHF(params interface{}, t *rpc.Transmitter) error {
	updateParams := params.(UpdateSubscriberParams)
	if updateParams.EventTypes == "" {
		return rpc.NewArgsError(errors.New("'eventTypes' required for subscriber update"))
	}
	if err := UpdateSubscriber(updateParams.Pid, t.Channel.ID, maskFromTypes(updateParams.EventTypes)); err != nil {
		return asRPCError(err)
	}
	t.Send(&SubscribeResult{
		Pid:        updateParams.Pid,
		EventTypes: updateParams.EventTypes,
		Text:       "Subscriber successfully updated",
	})
	return nil
}

// GetLogsParams represents params for get process logs call
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

	from, err := process.ParseTime(getLogsParams.From, time.Time{})
	if err != nil {
		return rpc.NewArgsError(errors.New("Bad format of 'from', " + err.Error()))
	}

	till, err := process.ParseTime(getLogsParams.Till, time.Now())
	if err != nil {
		return rpc.NewArgsError(errors.New("Bad format of 'till', " + err.Error()))
	}

	logs, err := ReadLogs(getLogsParams.Pid, from, till)
	if err != nil {
		return asRPCError(err)
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

// GetProcessParams represents params for get process call
type GetProcessParams struct {
	Pid uint64 `json:"pid"`
}

func getProcessReqHF(body interface{}, t *rpc.Transmitter) error {
	params := body.(GetProcessParams)
	p, err := Get(params.Pid)
	if err != nil {
		return asRPCError(err)
	}
	t.Send(p)
	return nil
}

// GetProcessesParams represents params for get processes call
type GetProcessesParams struct {
	All bool `json:"all"`
}

func getProcessesReqHF(body interface{}, t *rpc.Transmitter) error {
	params := body.(GetProcessesParams)
	t.Send(GetProcesses(params.All))
	return nil
}

func asRPCError(err error) error {
	if npErr, ok := err.(*NoProcessError); ok {
		return rpc.NewError(npErr.error, NoSuchProcessErrorCode)
	} else if naErr, ok := err.(*NotAliveError); ok {
		return rpc.NewError(naErr.error, ProcessNotAliveErrorCode)
	}
	return err
}
