package process_test

import (
	"github.com/eclipse/che/exec-agent/op"
	"github.com/eclipse/che/exec-agent/process"
	"testing"
	"time"
	"os"
)

func TestMachineProcessIsNotAliveAfterItIsDead(t *testing.T) {
	p := startAndWaitTestProcess(t)
	defer os.RemoveAll(process.LogsDir)
	if p.Alive {
		t.Fatal("Process should not be alive")
	}
}

func TestItIsNotPossibleToAddSubscriberToDeadProcess(t *testing.T) {
	p := startAndWaitTestProcess(t)
	defer os.RemoveAll(process.LogsDir)
	if err := p.AddSubscriber(&process.Subscriber{}); err == nil {
		t.Fatal("Should not be able to add subscriber")
	}
}

func TestReadProcessLogs(t *testing.T) {
	p := startAndWaitTestProcess(t)
	defer os.RemoveAll(process.LogsDir)
	logs, err := p.ReadLogs(time.Time{}, time.Now())
	if err != nil {
		t.Fatal(err)
	}
	expected := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}

	for idx := range logs {
		if process.StdoutKind != logs[idx].Kind {
			t.Fatalf("Expected log message kind to be '%s', while got '%s'", process.StdoutKind, logs[idx].Kind)
		}
		if expected[idx] != logs[idx].Text {
			t.Fatalf("Expected log message to be '%s', but got '%s'", expected[idx], logs[idx].Text)
		}
	}
}

func startAndWaitTestProcess(t *testing.T) *process.MachineProcess {
	process.LogsDir = os.TempDir() + string(os.PathSeparator) + randomName(10)
	events := make(chan *op.Event)
	done := make(chan bool)

	// Create and start process
	p := process.NewProcess(process.Command{
		Name:        "test",
		CommandLine: "printf \"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\"",
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
				if event.EventType == process.ProcessDiedEventType {
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
		t.Fatalf("Expected to receive %s process event", process.ProcessDiedEventType)
	}

	// Check process state after it is finished
	result, ok := process.Get(p.Pid)
	if !ok {
		t.Fatal("Expected process to exist")
	}
	return result
}
