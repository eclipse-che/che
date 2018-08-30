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

package exec

import (
	"errors"
	"math"
	"time"

	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che/agents/go-agents/core/process"
)

// Constants that represent RPC methods identifiers.
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

// Error codes.
const (
	ProcessAPIErrorCode      = 100
	NoSuchProcessErrorCode   = 101
	ProcessNotAliveErrorCode = 102
)

// RPCRoutes defines process jsonrpc routes.
var RPCRoutes = jsonrpc.RoutesGroup{
	Name: "Process Routes",
	Items: []jsonrpc.Route{
		{
			Method: StartMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &StartParams{} }),
			Handle: jsonrpcStartProcess,
		},
		{
			Method: KillMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &KillParams{} }),
			Handle: jsonrpc.HandleRet(jsonrpcKillProcess),
		},
		{
			Method: SubscribeMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &SubscribeParams{} }),
			Handle: jsonrpc.HandleRet(jsonrpcSubscribe),
		},
		{
			Method: UnsubscribeMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &UnsubscribeParams{} }),
			Handle: jsonrpcUnsubscribe,
		},
		{
			Method: UpdateSubscriberMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &UpdateSubscriberParams{} }),
			Handle: jsonrpc.HandleRet(jsonrpcUpdateSubscriber),
		},
		{
			Method: GetLogsMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &GetLogsParams{} }),
			Handle: jsonrpc.HandleRet(jsonrpcGetProcessLogs),
		},
		{
			Method: GetProcessMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &GetProcessParams{} }),
			Handle: jsonrpcGetProcess,
		},
		{
			Method: GetProcessesMethod,
			Decode: jsonrpc.FactoryDec(func() interface{} { return &GetProcessesParams{} }),
			Handle: jsonrpcGetProcesses,
		},
	},
}

// ProcessResult result of operation performed on process.
type ProcessResult struct {
	Pid  uint64 `json:"pid"`
	Text string `json:"text"`
}

// StartParams represents params for start process call.
type StartParams struct {
	Name        string `json:"name"`
	CommandLine string `json:"commandLine"`
	Type        string `json:"type"`
	EventTypes  string `json:"eventTypes"`
}

func jsonrpcStartProcess(tun *jsonrpc.Tunnel, params interface{}, t jsonrpc.RespTransmitter) {
	startParams := params.(*StartParams)
	command := process.Command{
		Name:        startParams.Name,
		CommandLine: startParams.CommandLine,
		Type:        startParams.Type,
	}
	if err := checkCommand(&command); err != nil {
		t.SendError(jsonrpc.NewArgsError(err))
	} else {
		pb := process.NewBuilder()
		pb.Cmd(command)
		pb.Subscribe(tun.ID(), parseTypes(startParams.EventTypes), &rpcProcessEventConsumer{tun})
		pb.BeforeEventsHook(func(process process.MachineProcess) {
			t.Send(process)
		})
		if _, err := pb.Start(); err != nil {
			t.SendError(asRPCError(err))
		}
	}
}

// KillParams represents params for kill process call.
type KillParams struct {
	Pid       uint64 `json:"pid"`
	NativePid uint64 `json:"nativePid"`
}

func jsonrpcKillProcess(_ *jsonrpc.Tunnel, params interface{}) (interface{}, error) {
	killParams := params.(*KillParams)
	if err := process.Kill(killParams.Pid); err != nil {
		return nil, asRPCError(err)
	}
	return &ProcessResult{Pid: killParams.Pid, Text: "Successfully killed"}, nil
}

// SubscribeResult represents result of subscribe call.
type SubscribeResult struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
	Text       string `json:"text"`
}

// SubscribeParams represents params for subscribe to events call.
type SubscribeParams struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
	After      string `json:"after"`
}

func jsonrpcSubscribe(tun *jsonrpc.Tunnel, params interface{}) (interface{}, error) {
	subscribeParams := params.(*SubscribeParams)

	mask := maskFromTypes(subscribeParams.EventTypes)
	if mask == 0 {
		return nil, jsonrpc.NewArgsError(errors.New("Required at least 1 valid event type"))
	}

	subscriber := process.Subscriber{
		ID:       tun.ID(),
		Mask:     mask,
		Consumer: &rpcProcessEventConsumer{tun},
	}
	// Check whether subscriber should see previous logs or not
	if subscribeParams.After == "" {
		if err := process.AddSubscriber(subscribeParams.Pid, subscriber); err != nil {
			return nil, asRPCError(err)
		}
	} else {
		after, err := time.Parse(process.DateTimeFormat, subscribeParams.After)
		if err != nil {
			return nil, jsonrpc.NewArgsError(errors.New("Bad format of 'after', " + err.Error()))
		}
		if err := process.RestoreSubscriber(subscribeParams.Pid, subscriber, after); err != nil {
			return nil, err
		}
	}
	return &SubscribeResult{
		Pid:        subscribeParams.Pid,
		EventTypes: subscribeParams.EventTypes,
		Text:       "Successfully subscribed",
	}, nil
}

// UnsubscribeParams represents params for unsubscribe from events call.
type UnsubscribeParams struct {
	Pid uint64 `json:"pid"`
}

func jsonrpcUnsubscribe(tun *jsonrpc.Tunnel, params interface{}, t jsonrpc.RespTransmitter) {
	unsubscribeParams := params.(*UnsubscribeParams)
	if err := process.RemoveSubscriber(unsubscribeParams.Pid, tun.ID()); err != nil {
		t.SendError(asRPCError(err))
	} else {
		t.Send(&ProcessResult{Pid: unsubscribeParams.Pid, Text: "Successfully unsubscribed"})
	}
}

// UpdateSubscriberParams represents params for update subscribtion to events call.
type UpdateSubscriberParams struct {
	Pid        uint64 `json:"pid"`
	EventTypes string `json:"eventTypes"`
}

func jsonrpcUpdateSubscriber(tun *jsonrpc.Tunnel, params interface{}) (interface{}, error) {
	updateParams := params.(*UpdateSubscriberParams)
	if updateParams.EventTypes == "" {
		return nil, jsonrpc.NewArgsError(errors.New("'eventTypes' required for subscriber update"))
	}
	if err := process.UpdateSubscriber(updateParams.Pid, tun.ID(), maskFromTypes(updateParams.EventTypes)); err != nil {
		return nil, asRPCError(err)
	}
	return &SubscribeResult{
		Pid:        updateParams.Pid,
		EventTypes: updateParams.EventTypes,
		Text:       "Subscriber successfully updated",
	}, nil
}

// GetLogsParams represents params for get process logs call.
type GetLogsParams struct {
	Pid   uint64 `json:"pid"`
	From  string `json:"from"`
	Till  string `json:"till"`
	Limit int    `json:"limit"`
	Skip  int    `json:"skip"`
}

func jsonrpcGetProcessLogs(_ *jsonrpc.Tunnel, params interface{}) (interface{}, error) {
	getLogsParams := params.(*GetLogsParams)

	if getLogsParams.Skip < 0 {
		getLogsParams.Skip = 0
	}
	if getLogsParams.Limit < 0 {
		getLogsParams.Limit = 0
	}

	from, err := process.ParseTime(getLogsParams.From, time.Time{})
	if err != nil {
		return nil, jsonrpc.NewArgsError(errors.New("Bad format of 'from', " + err.Error()))
	}

	till, err := process.ParseTime(getLogsParams.Till, time.Now())
	if err != nil {
		return nil, jsonrpc.NewArgsError(errors.New("Bad format of 'till', " + err.Error()))
	}

	logs, err := process.ReadLogs(getLogsParams.Pid, from, till)
	if err != nil {
		return nil, asRPCError(err)
	}

	limit := DefaultLogsPerPageLimit
	if getLogsParams.Limit != 0 {
		if getLogsParams.Limit < 1 {
			return nil, jsonrpc.NewArgsError(errors.New("Required 'limit' to be > 0"))
		}
		limit = getLogsParams.Limit
	}

	skip := 0
	if getLogsParams.Skip != 0 {
		if getLogsParams.Skip < 0 {
			return nil, jsonrpc.NewArgsError(errors.New("Required 'skip' to be >= 0"))
		}
		skip = getLogsParams.Skip
	}

	logsLen := len(logs)
	fromIdx := int(math.Max(float64(logsLen-limit-skip), 0))
	toIdx := logsLen - int(math.Min(float64(skip), float64(logsLen)))

	return logs[fromIdx:toIdx], nil
}

// GetProcessParams represents params for get process call.
type GetProcessParams struct {
	Pid uint64 `json:"pid"`
}

func jsonrpcGetProcess(_ *jsonrpc.Tunnel, body interface{}, t jsonrpc.RespTransmitter) {
	params := body.(*GetProcessParams)
	p, err := process.Get(params.Pid)
	if err != nil {
		t.SendError(asRPCError(err))
	} else {
		t.Send(p)
	}
}

// GetProcessesParams represents params for get processes call.
type GetProcessesParams struct {
	All bool `json:"all"`
}

func jsonrpcGetProcesses(_ *jsonrpc.Tunnel, body interface{}, t jsonrpc.RespTransmitter) {
	params := body.(*GetProcessesParams)
	t.Send(process.GetProcesses(params.All))
}

func asRPCError(err error) *jsonrpc.Error {
	if npErr, ok := err.(*process.NoProcessError); ok {
		return jsonrpc.NewError(NoSuchProcessErrorCode, npErr)
	} else if naErr, ok := err.(*process.NotAliveError); ok {
		return jsonrpc.NewError(ProcessNotAliveErrorCode, naErr)
	}
	return jsonrpc.NewError(ProcessAPIErrorCode, err)
}
