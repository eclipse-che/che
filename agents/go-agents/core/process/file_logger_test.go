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
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"testing"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/process"
)

func TestFileLoggerCreatesFileWhenFileDoesNotExist(t *testing.T) {
	filename := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer removeFile(filename)

	if _, err := os.Stat(filename); err == nil {
		t.Fatalf("File '%s' already exists", filename)
	}

	if _, err := process.NewLogger(filename); err != nil {
		t.Fatal(err)
	}

	if _, err := os.Stat(filename); os.IsNotExist(err) {
		t.Fatalf("Expected file '%s' was created, but it wasn't", filename)
	}
}

func TestFileLoggerTruncatesFileIfFileExistsOnCreate(t *testing.T) {
	filename := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer removeFile(filename)

	if _, err := os.Create(filename); err != nil {
		t.Fatal(err)
	}
	if err := ioutil.WriteFile(filename, []byte("file-content"), 0666); err != nil {
		t.Fatal(err)
	}

	if _, err := process.NewLogger(filename); err != nil {
		t.Fatal(err)
	}

	content, err := ioutil.ReadFile(filename)
	if err != nil {
		t.Fatal(err)
	}
	if len(content) != 0 {
		t.Errorf("Expected file '%s' content is empty", filename)
	}
}

func TestLogsAreFlushedOnClose(t *testing.T) {
	filename := os.TempDir() + string(os.PathSeparator) + randomName(10)
	defer removeFile(filename)

	fl, err := process.NewLogger(filename)
	if err != nil {
		t.Fatal(err)
	}

	// Write something to the log
	now := time.Now()
	fl.OnStdout("stdout", now)
	fl.OnStderr("stderr", now)
	fl.Close()

	// Read file content
	f, err := os.Open(filename)
	if err != nil {
		t.Fatal(err)
	}

	// Read log messages
	stdout := process.LogMessage{}
	stderr := process.LogMessage{}
	decoder := json.NewDecoder(f)
	if err := decoder.Decode(&stdout); err != nil {
		t.Fatal(err)
	}
	if err := decoder.Decode(&stderr); err != nil {
		t.Fatal(err)
	}

	// Check logs are okay
	expectedStdout := process.LogMessage{
		Kind: process.StdoutKind,
		Time: now,
		Text: "stdout",
	}
	expectedStderr := process.LogMessage{
		Kind: process.StderrKind,
		Time: now,
		Text: "stderr",
	}
	failIfDifferent(t, expectedStdout, stdout)
	failIfDifferent(t, expectedStderr, stderr)
}

func removeFile(path string) {
	if err := os.Remove(path); err != nil {
		log.Printf("Can't remove file %s. Error: %s", path, err)
	}
}
