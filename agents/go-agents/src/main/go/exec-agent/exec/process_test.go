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

package exec_test

import (
	"math/rand"
	"os"
	"strings"
	"testing"
	"time"

	"github.com/eclipse/che/agents/go-agents/src/main/go/core/process"
	"github.com/eclipse/che/agents/go-agents/src/main/go/core/rpc"
	"github.com/eclipse/che/agents/go-agents/src/main/go/exec-agent/exec"
)

const (
	testCmd = "printf \"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\""
)

var alphabet = []byte("abcdefgh123456789")

func TestOneLineOutput(t *testing.T) {
	defer cleanupLogsDir()
	// create and start a process
	p := startAndWaitTestProcess("echo test", t)

	logs, _ := exec.ReadAllLogs(p.Pid)

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

	logs, _ := exec.ReadAllLogs(p.Pid)

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
	exec.LogsDir = TmpFile()
	defer cleanupLogsDir()

	outputLines := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}

	// create and start a process
	pb := exec.NewBuilder().
		CmdName("test").
		CmdType("test").
		CmdLine("printf \"" + strings.Join(outputLines, "\n") + "\"")

	// add a new subscriber
	eventsChan := make(chan *rpc.Event)
	pb.FirstSubscriber(exec.Subscriber{
		ID:      "test",
		Mask:    exec.DefaultMask,
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
		for event.EventType != exec.DiedEventType {
			if event.EventType == exec.StdoutEventType {
				out := event.Body.(*exec.ProcessOutputEventBody)
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
	exec.LogsDir = TmpFile()
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
				if v.EventType == exec.DiedEventType {
					statusReceived = true
				}
			case <-time.After(time.Second):
				timeoutReached = true
			}
		}
		done <- true
	}()

	exec.RestoreSubscriber(p.Pid, exec.Subscriber{
		"test",
		exec.DefaultMask,
		channel,
	}, beforeStart)

	<-done

	if len(received) != 2 {
		t.Fatalf("Expected to recieve 2 events but got %d", len(received))
	}
	e1Type := received[0].EventType
	e1Text := received[0].Body.(*exec.ProcessOutputEventBody).Text
	if received[0].EventType != exec.StdoutEventType || e1Text != "test" {
		t.Fatalf("Expected to receieve output event with text 'test', but got '%s' event with text %s",
			e1Type,
			e1Text)
	}
	if received[1].EventType != exec.DiedEventType {
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
	if err := exec.AddSubscriber(p.Pid, exec.Subscriber{}); err == nil {
		t.Fatal("Should not be able to add subscriber")
	}
}

func TestReadProcessLogs(t *testing.T) {
	p := startAndWaitTestProcess(testCmd, t)
	defer cleanupLogsDir()
	logs, err := exec.ReadLogs(p.Pid, time.Time{}, time.Now())
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

func startAndWaitTestProcess(cmd string, t *testing.T) exec.MachineProcess {
	exec.LogsDir = TmpFile()
	events := make(chan *rpc.Event)
	done := make(chan bool)

	// Create and start process
	pb := exec.NewBuilder().
		CmdName("test").
		CmdType("test").
		CmdLine(cmd).
		FirstSubscriber(exec.Subscriber{
			ID:      "test",
			Mask:    exec.DefaultMask,
			Channel: events,
		})

	go func() {
		statusReceived := false
		timeoutReached := false
		for !statusReceived && !timeoutReached {
			select {
			case event := <-events:
				if event.EventType == exec.DiedEventType {
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
		t.Fatalf("Expected to receive %s process event", exec.DiedEventType)
	}

	// Check process state after it is finished
	result, err := exec.Get(p.Pid)
	if err != nil {
		t.Fatal(err)
	}
	return result
}

func TmpFile() string {
	return os.TempDir() + string(os.PathSeparator) + randomName(10)
}

func cleanupLogsDir() {
	os.RemoveAll(exec.LogsDir)
}

func randomName(length int) string {
	rand.Seed(time.Now().UnixNano())
	bytes := make([]byte, length)
	for i := 0; i < length; i++ {
		bytes[i] = alphabet[rand.Intn(len(alphabet))]
	}
	return string(bytes)
}
