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
	"encoding/json"
	"io/ioutil"
	"os"
	"strings"
	"testing"
	"time"

	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che-go-jsonrpc/jsonrpctest"
	"github.com/eclipse/che/agents/go-agents/core/process"
)

func TestJSONRPCStartProcess(t *testing.T) {
	channel, connRecorder := startTestChannel()
	defer channel.Close()

	command := &process.Command{Name: "test", CommandLine: "echo test", Type: "test"}
	connRecorder.PushNextReq(StartMethod, command)

	if err := connRecorder.WaitUntil(jsonrpctest.ReqSent(process.DiedEventType)); err != nil {
		t.Fatal(err)
	}

	// first response should be sent
	resp, err := connRecorder.GetResponse(0)
	if err != nil {
		t.Fatal(err)
	}
	startedProcess := &process.MachineProcess{}
	if err := json.Unmarshal(resp.Result, startedProcess); err != nil {
		t.Fatal(err)
	}

	failIfDifferent(t, command.CommandLine, startedProcess.CommandLine, "Command line")
	failIfDifferent(t, command.Name, startedProcess.Name, "Name")
	failIfDifferent(t, command.Type, startedProcess.Type, "Type")
	failIfDifferent(t, true, startedProcess.Alive, "Alive")

	// check sent events
	expMethods := []string{process.StartedEventType, process.StdoutEventType, process.DiedEventType}

	requests, err := connRecorder.GetAllRequests()
	if err != nil {
		t.Fatal(err)
	}

	for i, v := range requests {
		if v.Method != expMethods[i] {
			t.Fatalf("Expected receive event %s but received %s", expMethods[i], v.Method)
		}
	}
}

func TestJSONRPCGetProcess(t *testing.T) {
	channel, rec := startTestChannel()
	defer channel.Close()

	command := process.Command{Name: "test", CommandLine: "echo test", Type: "test"}
	deadProcess := jsonrpcStartAndWaitProcess(t, rec, command)

	// request process
	respIdx := len(rec.GetAll())
	rec.PushNextReq(GetProcessMethod, &GetProcessParams{deadProcess.Pid})
	if err := rec.WaitUntil(jsonrpctest.WriteCalledAtLeast(respIdx + 1)); err != nil {
		t.Fatal(err)
	}

	// get and check response
	gotProcess := process.MachineProcess{}
	err := rec.UnmarshalResponseResult(respIdx, &gotProcess)
	if err != nil {
		t.Fatal(err)
	}

	failIfDifferent(t, deadProcess.Pid, gotProcess.Pid, "Pid")
	failIfDifferent(t, deadProcess.NativePid, gotProcess.NativePid, "Native Pid")
	failIfDifferent(t, deadProcess.Alive, gotProcess.Alive, "Alive")
	failIfDifferent(t, deadProcess.CommandLine, gotProcess.CommandLine, "CommandLine")
	failIfDifferent(t, deadProcess.Name, gotProcess.Name, "Name")
}

func TestJSONRPCGetProcessLogs(t *testing.T) {
	dir, err := ioutil.TempDir(os.TempDir(), "exec-agent-test")
	if err != nil {
		t.Fatal(err)
	}
	process.SetLogsDir(dir)
	defer process.WipeLogs()

	channel, rec := startTestChannel()
	defer channel.Close()

	// start & wait process
	outputLines := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}
	command := process.Command{
		Name:        "test",
		CommandLine: "printf \"" + strings.Join(outputLines, "\n") + "\"",
		Type:        "test",
	}
	deadProcess := jsonrpcStartAndWaitProcess(t, rec, command)

	realLogs, err := process.ReadAllLogs(deadProcess.Pid)
	if err != nil {
		t.Fatal(err)
	}

	cases := []struct {
		expectedLogs []*process.LogMessage
		params       GetLogsParams
	}{
		{
			expectedLogs: realLogs[5:],
			params:       GetLogsParams{Pid: deadProcess.Pid, Limit: 5},
		},
		{
			expectedLogs: realLogs[:5],
			params:       GetLogsParams{Pid: deadProcess.Pid, Skip: 5},
		},
		{
			expectedLogs: realLogs[3:5],
			params:       GetLogsParams{Pid: deadProcess.Pid, Skip: 5, Limit: 2},
		},
		{
			expectedLogs: make([]*process.LogMessage, 0),
			params:       GetLogsParams{Pid: deadProcess.Pid, Skip: 20, Limit: 2},
		},
		{
			expectedLogs: realLogs[9:],
			params:       GetLogsParams{Pid: deadProcess.Pid, Limit: 1},
		},
		{
			expectedLogs: realLogs[6:],
			params: GetLogsParams{
				Pid:  deadProcess.Pid,
				From: realLogs[6].Time.Format(process.DateTimeFormat),
			},
		},
		{
			expectedLogs: realLogs[6:8],
			params: GetLogsParams{
				Pid:  deadProcess.Pid,
				From: realLogs[6].Time.Format(process.DateTimeFormat),
				Till: realLogs[7].Time.Format(process.DateTimeFormat),
			},
		},
	}

	for _, theCase := range cases {
		respIdx := len(rec.GetAll())
		if err := rec.PushNextReq(GetLogsMethod, theCase.params); err != nil {
			t.Fatal(err)
		}

		// wait for the response
		if err := rec.WaitUntil(jsonrpctest.WriteCalledAtLeast(respIdx + 1)); err != nil {
			t.Fatal(err)
		}

		logs := []*process.LogMessage{}
		if err := rec.UnmarshalResponseResult(respIdx, &logs); err != nil {
			t.Fatal(err)
		}
		failIfDifferent(t, len(theCase.expectedLogs), len(logs), "logs len")
		for i := 0; i < len(theCase.expectedLogs); i++ {
			failIfDifferent(t, *theCase.expectedLogs[i], *logs[i], "log messages")
		}
	}
}

func startTestChannel() (*jsonrpc.Tunnel, *jsonrpctest.ConnRecorder) {
	jsonrpc.RegRoutesGroup(RPCRoutes)
	connRecorder := jsonrpctest.NewConnRecorder()
	channel := jsonrpc.NewTunnel(connRecorder, jsonrpc.DefaultRouter)
	connRecorder.CloseAfter(2 * time.Second)
	channel.Go()
	return channel, connRecorder
}

func jsonrpcStartAndWaitProcess(t *testing.T, recorder *jsonrpctest.ConnRecorder, command process.Command) process.MachineProcess {
	recorder.PushNextReq(StartMethod, command)
	if err := recorder.WaitUntil(jsonrpctest.ReqSent(process.DiedEventType)); err != nil {
		t.Fatal(err)
	}
	requests, err := recorder.GetAllRequests()
	if err != nil {
		t.Fatal(err)
	}

	// the last request must be died event
	dieReq := requests[len(requests)-1]
	deadProcess := process.MachineProcess{}
	if err := json.Unmarshal(dieReq.Params, &deadProcess); err != nil {
		t.Fatal(err)
	}

	return deadProcess
}
