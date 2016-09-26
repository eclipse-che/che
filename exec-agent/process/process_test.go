package process_test

import (
	"github.com/eclipse/che/exec-agent/process"
	"github.com/eclipse/che/exec-agent/rpc"
	"os"
	"strings"
	"testing"
	"time"
)

const (
	TestCmd = "printf \"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\""
)

func TestOneLineOutput(t *testing.T) {
	defer cleanupLogsDir()
	// create and start a process
	p := startAndWaitTestProcess("echo test", t)

	logs, _ := p.ReadAllLogs()

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

	logs, _ := p.ReadAllLogs()

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
	p := process.NewProcess(process.Command{
		Name:        "test",
		CommandLine: "printf \"" + strings.Join(outputLines, "\n") + "\"",
		Type:        "test",
	})

	// add a new subscriber
	eventsChan := make(chan *rpc.Event)
	p.AddSubscriber(&process.Subscriber{
		Id:      "test",
		Mask:    process.DefaultMask,
		Channel: eventsChan,
	})

	// start a new process
	if err := p.Start(); err != nil {
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

func TestMachineProcessIsNotAliveAfterItIsDead(t *testing.T) {
	p := startAndWaitTestProcess(TestCmd, t)
	defer cleanupLogsDir()
	if p.Alive {
		t.Fatal("Process should not be alive")
	}
}

func TestItIsNotPossibleToAddSubscriberToDeadProcess(t *testing.T) {
	p := startAndWaitTestProcess(TestCmd, t)
	defer cleanupLogsDir()
	if err := p.AddSubscriber(&process.Subscriber{}); err == nil {
		t.Fatal("Should not be able to add subscriber")
	}
}

func TestReadProcessLogs(t *testing.T) {
	p := startAndWaitTestProcess(TestCmd, t)
	defer cleanupLogsDir()
	logs, err := p.ReadLogs(time.Time{}, time.Now())
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

func startAndWaitTestProcess(cmd string, t *testing.T) *process.MachineProcess {
	process.LogsDir = TmpFile()
	events := make(chan *rpc.Event)
	done := make(chan bool)

	// Create and start process
	p := process.NewProcess(process.Command{
		Name:        "test",
		CommandLine: cmd,
		Type:        "test",
	})

	p.AddSubscriber(&process.Subscriber{
		Id:      "test",
		Mask:    process.DefaultMask,
		Channel: events,
	})

	go func() {
		for {
			select {
			case event := <-events:
				if event.EventType == process.DiedEventType {
					done <- true
					break
				}
			case <-time.After(2 * time.Second):
				done <- false
				break
			}
		}
	}()

	if err := p.Start(); err != nil {
		t.Fatal(err)
	}

	// Wait until process is finished or timeout is reached
	if ok := <-done; !ok {
		t.Fatalf("Expected to receive %s process event", process.DiedEventType)
	}

	// Check process state after it is finished
	result, ok := process.Get(p.Pid)
	if !ok {
		t.Fatal("Expected process to exist")
	}
	return result
}

func TmpFile() string {
	return os.TempDir() + string(os.PathSeparator) + randomName(10)
}

func cleanupLogsDir() {
	os.RemoveAll(process.LogsDir)
}
