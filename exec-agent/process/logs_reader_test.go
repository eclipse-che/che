package process_test

import (
	"github.com/evoevodin/machine-agent/process"
	"os"
	"testing"
	"time"
)

func TestReadLogs(t *testing.T) {
	filename := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer os.Remove(filename)

	fl, err := process.NewLogger(filename)
	if err != nil {
		t.Fatal(err)
	}

	// Write something to the log
	now := time.Now()
	fl.OnStdout("line1", now.Add(time.Second))
	fl.OnStdout("line2", now.Add(time.Second*2))
	fl.OnStdout("line3", now.Add(time.Second*3))
	fl.OnStderr("line4", now.Add(time.Second*4))
	fl.OnStderr("line5", now.Add(time.Second*5))
	fl.Close()

	// Read logs [2, 4]
	logs, err :=
		process.NewLogsReader(filename).
			From(now.Add(time.Second * 2)).
			Till(now.Add(time.Second * 4)).
			ReadLogs()
	if err != nil {
		t.Fatal(err)
	}

	// Check everything is okay
	expected := []process.LogMessage{
		{Kind: process.StdoutKind, Time: now.Add(time.Second * 2), Text: "line2"},
		{Kind: process.StdoutKind, Time: now.Add(time.Second * 3), Text: "line3"},
		{Kind: process.StderrKind, Time: now.Add(time.Second * 4), Text: "line4"},
	}
	for i := 0; i < len(logs); i++ {
		if *logs[i] != expected[i] {
			t.Fatalf("Expected: '%v' Found '%v'", expected[i], *logs[i])
		}
	}
}
