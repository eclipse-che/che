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

package process

import (
	"bufio"
	"encoding/json"
	"io"
	"os"
	"time"
)

// LogsReader allows to read logs from file written by DefaultFileLogger
type LogsReader struct {
	filename string
	readFrom *time.Time
	readTill *time.Time
}

// NewLogsReader creates new LogsReader instance
func NewLogsReader(filename string) *LogsReader {
	return &LogsReader{filename: filename}
}

// From skips all the logs before the given time.
// If the log message appeared at the given time, it won't be skipped.
func (lr *LogsReader) From(time time.Time) *LogsReader {
	lr.readFrom = &time
	return lr
}

// Till reads logs which appeared before and right at a given time
func (lr *LogsReader) Till(time time.Time) *LogsReader {
	lr.readTill = &time
	return lr
}

// ReadLogs reads logs between [from, till] inclusive.
// Returns an error if logs file is missing, or
// decoding of file content failed.
// If no logs matched time frame, an empty slice will be returned.
func (lr *LogsReader) ReadLogs() ([]*LogMessage, error) {
	// Open logs file for reading logs
	logsFile, err := os.Open(lr.filename)
	if err != nil {
		return nil, err
	}
	defer closeFile(logsFile)

	from := time.Time{}
	if lr.readFrom != nil {
		from = *lr.readFrom
	}
	till := time.Now()
	if lr.readTill != nil {
		till = *lr.readTill
	}

	// Read logs
	logs := []*LogMessage{}
	decoder := json.NewDecoder(bufio.NewReader(logsFile))
	for {
		message := &LogMessage{}
		err = decoder.Decode(message)
		if err != nil {
			if err == io.EOF {
				break
			}
			return nil, err
		}
		if message.Time.Before(from) {
			continue
		}
		if message.Time.After(till) {
			break
		}
		logs = append(logs, message)
	}
	return logs, nil
}
