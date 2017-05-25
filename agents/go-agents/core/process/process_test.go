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
	"log"
	"math/rand"
	"os"
	"strings"
	"testing"
	"time"

	"fmt"
	"github.com/eclipse/che/agents/go-agents/core/process"
	"github.com/eclipse/che/agents/go-agents/core/rpc"
)

const (
	testCmd = "printf \"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\""
)

var alphabet = []byte("abcdefgh123456789")

func TestOneLineOutput(t *testing.T) {
	defer wipeLogs()
	// create and start a process
	p := startAndWaitTestProcessWritingLogsToTmpDir("echo test", t)

	logs, _ := process.ReadAllLogs(p.Pid)

	if len(logs) != 1 {
		t.Fatalf("Expected logs size to be 1, but got %d", len(logs))
	}

	if logs[0].Text != "test" {
		t.Fatalf("Expected to get 'test' output but got %s", logs[0].Text)
	}
}

func TestEmptyLinesOutput(t *testing.T) {
	p := startAndWaitTestProcessWritingLogsToTmpDir("printf \"\n\n\n\n\n\"", t)
	defer process.WipeLogs()

	logs, _ := process.ReadAllLogs(p.Pid)

	if len(logs) != 5 {
		t.Fatalf("Expected logs to be 5 sized, but the size is '%d'", len(logs))
	}

	for _, value := range logs {
		if value.Text != "" {
			t.Fatal("Expected all the logs to be empty files")
		}
	}
}

func TestAddSubscriber(t *testing.T) {
	outputLines := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}

	// create and start a process
	pb := process.NewBuilder().
		CmdName("test").
		CmdType("test").
		CmdLine("printf \"" + strings.Join(outputLines, "\n") + "\"")

	// add a new subscriber
	eventsChan := make(chan *rpc.Event)
	pb.FirstSubscriber(process.Subscriber{
		ID:      "test",
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
	beforeStart := time.Now()
	p := startAndWaitTestProcessWritingLogsToTmpDir("echo test", t)
	defer process.WipeLogs()

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

	_ = process.RestoreSubscriber(p.Pid, process.Subscriber{
		ID:      "test",
		Mask:    process.DefaultMask,
		Channel: channel,
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
	if p.Alive {
		t.Fatal("Process should not be alive")
	}
}

func TestItIsNotPossibleToAddSubscriberToDeadProcess(t *testing.T) {
	p := startAndWaitTestProcess(testCmd, t)
	if err := process.AddSubscriber(p.Pid, process.Subscriber{}); err == nil {
		t.Fatal("Should not be able to add subscriber")
	}
}

func TestReadProcessLogs(t *testing.T) {
	p := startAndWaitTestProcessWritingLogsToTmpDir(testCmd, t)
	defer wipeLogs()
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

func TestLogsAreNotWrittenIfLogsDirIsNotSet(t *testing.T) {
	p := doStartAndWaitTestProcess(testCmd, "", t)

	_, err := process.ReadAllLogs(p.Pid)
	if err == nil {
		t.Fatal("Error must be returned in the case when the process doesn't write logs")
	}

	expected := fmt.Sprintf("Logs file for process '%d' is missing", p.Pid)
	if err.Error() != expected {
		t.Fatalf("Expected to get '%s' error but got '%s'", err.Error(), expected)
	}
}

func startAndWaitTestProcess(cmd string, t *testing.T) process.MachineProcess {
	return doStartAndWaitTestProcess(cmd, "", t);
}

func startAndWaitTestProcessWritingLogsToTmpDir(cmd string, t *testing.T) process.MachineProcess {
	return doStartAndWaitTestProcess(cmd, tmpFile(), t);
}

func doStartAndWaitTestProcess(cmd string, logsDir string, t *testing.T) process.MachineProcess {
	process.SetLogsDir(logsDir)
	p, err := process.NewBuilder().
		CmdName("test").
		CmdType("test").
		CmdLine(cmd).
		Start()
	if err != nil {
		t.Fatal(err)
	}
	waitProcessDied(p, t)

	// Check process state after it is finished
	result, err := process.Get(p.Pid)
	if err != nil {
		t.Fatal(err)
	}
	return result
}

func waitProcessDiedOrFailIfTimeoutReached(mp process.MachineProcess, timeout time.Duration) error {
	events := make(chan *rpc.Event)
	subscriber := process.Subscriber{
		ID:      "test",
		Mask:    process.DefaultMask,
		Channel: events,
	}
	if err := process.RestoreSubscriber(mp.Pid, subscriber, time.Now()); err != nil {
		return err
	}

	// wait process.DiedEventType
	processDied := make(chan bool)
	go func() {
		for {
			event, ok := <-events
			if !ok {
				return
			}
			if event.EventType == process.DiedEventType {
				processDied <- true
				return
			}
		}
	}()

	// wait either process died or timeout reached
	select {
	case <-processDied:
		return nil
	case <-time.After(timeout):
		close(processDied)
		return fmt.Errorf(
			"Process pid='%d' cmd='%s' didn't publish '%s' event before timeout was reached",
			mp.Pid,
			mp.CommandLine,
			process.DiedEventType,
		)
	}
}

func waitProcessDied(mp process.MachineProcess, t *testing.T) {
	if err := waitProcessDiedOrFailIfTimeoutReached(mp, time.Second*5); err != nil {
		t.Fatal(err)
	}
}

func tmpFile() string {
	return os.TempDir() + string(os.PathSeparator) + randomName(10)
}

func wipeLogs() {
	if err := process.WipeLogs(); err != nil {
		log.Printf("Could not wipe process logs dir. %s", err.Error())
	}
}

func randomName(length int) string {
	rand.Seed(time.Now().UnixNano())
	bytes := make([]byte, length)
	for i := 0; i < length; i++ {
		bytes[i] = alphabet[rand.Intn(len(alphabet))]
	}
	return string(bytes)
}
