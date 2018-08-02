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

package process_test

import (
	"os"
	"testing"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/process"
)

func TestReadLogs(t *testing.T) {
	filename := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer removeFile(filename)

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
		failIfDifferent(t, *logs[i], expected[i])
	}
}

func failIfDifferent(t *testing.T, expected process.LogMessage, actual process.LogMessage) {
	if expected.Kind != actual.Kind || expected.Text != actual.Text || expected.Time.Unix() != actual.Time.Unix() {
		t.Fatalf("Expected: '%v' Found '%v'", expected, actual)
	}
}
