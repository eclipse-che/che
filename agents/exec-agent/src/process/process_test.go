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

package process_test

import (
	"github.com/eclipse/che/agents/exec-agent/process"
	"github.com/eclipse/che/agents/exec-agent/rpc"
	"os"
	"strings"
	"testing"
	"time"
)

const (
	testCmd = "printf \"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\""
)

func TestOneLineOutput(t *testing.T) {
	defer cleanupLogsDir()
	// create and start a process
	p := startAndWaitTestProcess("echo test", t)

	logs, _ := process.ReadAllLogs(p.Pid)

	if len(logs) != 1 {
		t.Fatalf("Expected logs size to be 1, but got %d", len(logs))
	}

	if logs[0].Text != "test" {
		t.Fatalf("Expected to get 'test' output but got %s", logs[0].Text)
	}
}

func TestEmptyLinesOutput(t *testing.T) {
	defer cleanupLogsDir()
	p := startAndWaitTestProcess("printf \"\n\n\n\n\n\"", t)

	logs, _ := process.ReadAllLogs(p.Pid)

	if len(logs) != 5 {
		t.Fatal("Expected logs to be 4 sized")
	}

	for _, value := range logs {
		if value.Text != "" {
			t.Fatal("Expected all the logs to be empty files")
		}
	}
}

func TestAddSubscriber(t *testing.T) {
	process.LogsDir = TmpFile()
	defer cleanupLogsDir()

	outputLines := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}

	// create and start a process
	pb := process.NewBuilder().
		CmdName("test").
		CmdType("test").
		CmdLine("printf \"" + strings.Join(outputLines, "\n") + "\"")

	// add a new subscriber
	eventsChan := make(chan *rpc.Event)
	pb.FirstSubscriber(process.Subscriber{
		Id:      "test",
		Mask:    process.DefaultMask,
		Channel: eventsChan,
	})

	// start a new process
	if _, err := pb.Start(); err != nil {
		t.Fatal(err)
	}

	// read all the process output events
	done := make(chan bool)
	var received []string
	go func() {
		event := <-eventsChan
		for event.EventType != process.DiedEventType {
			if event.EventType == process.StdoutEventType {
				out := event.Body.(*process.ProcessOutputEventBody)
				received = append(received, out.Text)
			}
			event = <-eventsChan
		}
		done <- true
	}()

	// wait until process is done
	<-done

	if len(outputLines) != len(received) {
		t.Fatalf("Expected the same size but got %d != %d", len(outputLines), len(received))
	}

	for idx, value := range outputLines {
		if value != received[idx] {
			t.Fatalf("Expected %s but got %s", value, received[idx])
		}
	}
}

func TestRestoreSubscriberForDeadProcess(t *testing.T) {
	process.LogsDir = TmpFile()
	defer cleanupLogsDir()
	beforeStart := time.Now()
	p := startAndWaitTestProcess("echo test", t)

	// Read all the data from channel
	channel := make(chan *rpc.Event)
	done := make(chan bool)
	var received []*rpc.Event
	go func() {
		statusReceived := false
		timeoutReached := false
		for !statusReceived && !timeoutReached {
			select {
			case v := <-channel:
				received = append(received, v)
				if v.EventType == process.DiedEventType {
					statusReceived = true
				}
			case <-time.After(time.Second):
				timeoutReached = true
			}
		}
		done <- true
	}()

	process.RestoreSubscriber(p.Pid, process.Subscriber{
		"test",
		process.DefaultMask,
		channel,
	}, beforeStart)

	<-done

	if len(received) != 2 {
		t.Fatalf("Expected to recieve 2 events but got %d", len(received))
	}
	e1Type := received[0].EventType
	e1Text := received[0].Body.(*process.ProcessOutputEventBody).Text
	if received[0].EventType != process.StdoutEventType || e1Text != "test" {
		t.Fatalf("Expected to receieve output event with text 'test', but got '%s' event with text %s",
			e1Type,
			e1Text)
	}
	if received[1].EventType != process.DiedEventType {
		t.Fatal("Expected to get 'process_died' event")
	}
}

func TestMachineProcessIsNotAliveAfterItIsDead(t *testing.T) {
	p := startAndWaitTestProcess(testCmd, t)
	defer cleanupLogsDir()
	if p.Alive {
		t.Fatal("Process should not be alive")
	}
}

func TestItIsNotPossibleToAddSubscriberToDeadProcess(t *testing.T) {
	p := startAndWaitTestProcess(testCmd, t)
	defer cleanupLogsDir()
	if err := process.AddSubscriber(p.Pid, process.Subscriber{}); err == nil {
		t.Fatal("Should not be able to add subscriber")
	}
}

func TestReadProcessLogs(t *testing.T) {
	p := startAndWaitTestProcess(testCmd, t)
	defer cleanupLogsDir()
	logs, err := process.ReadLogs(p.Pid, time.Time{}, time.Now())
	if err != nil {
		t.Fatal(err)
	}
	expected := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}

	for idx := range expected {
		if process.StdoutKind != logs[idx].Kind {
			t.Fatalf("Expected log message kind to be '%s', while got '%s'", process.StdoutKind, logs[idx].Kind)
		}
		if expected[idx] != logs[idx].Text {
			t.Fatalf("Expected log message to be '%s', but got '%s'", expected[idx], logs[idx].Text)
		}
	}
}

func startAndWaitTestProcess(cmd string, t *testing.T) process.MachineProcess {
	process.LogsDir = TmpFile()
	events := make(chan *rpc.Event)
	done := make(chan bool)

	// Create and start process
	pb := process.NewBuilder().
		CmdName("test").
		CmdType("test").
		CmdLine(cmd).
		FirstSubscriber(process.Subscriber{
			Id:      "test",
			Mask:    process.DefaultMask,
			Channel: events,
		})

	go func() {
		statusReceived := false
		timeoutReached := false
		for !statusReceived && !timeoutReached {
			select {
			case event := <-events:
				if event.EventType == process.DiedEventType {
					statusReceived = true
				}
			case <-time.After(time.Second):
				timeoutReached = true
			}
		}
		done <- true
	}()

	p, err := pb.Start()
	if err != nil {
		t.Fatal(err)
	}

	// Wait until process is finished or timeout is reached
	if ok := <-done; !ok {
		t.Fatalf("Expected to receive %s process event", process.DiedEventType)
	}

	// Check process state after it is finished
	result, err := process.Get(p.Pid)
	if err != nil {
		t.Fatal(err)
	}
	return result
}

func TmpFile() string {
	return os.TempDir() + string(os.PathSeparator) + randomName(10)
}

func cleanupLogsDir() {
	os.RemoveAll(process.LogsDir)
}
