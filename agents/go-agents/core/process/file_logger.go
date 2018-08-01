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
	"bytes"
	"encoding/json"
	"log"
	"os"
	"sync"
	"time"
)

const (
	flushThreshold = 8192
)

// FileLogger provides possibility to write process logs into a file
type FileLogger struct {
	sync.RWMutex
	filename string
	buffer   *bytes.Buffer
	encoder  *json.Encoder
}

// NewLogger creates FileLogger instance by provided log file path
func NewLogger(filename string) (*FileLogger, error) {
	fl := &FileLogger{filename: filename}
	fl.buffer = &bytes.Buffer{}
	fl.encoder = json.NewEncoder(fl.buffer)

	// Trying to create logs file
	file, err := os.Create(filename)
	if err != nil {
		return nil, err
	}
	defer closeFile(file)

	return fl, nil
}

// Flush writes buffered data into file
func (fl *FileLogger) Flush() {
	fl.Lock()
	fl.doFlush()
	fl.Unlock()
}

// OnStdout writes log message and marks it as stdout message
func (fl *FileLogger) OnStdout(line string, time time.Time) {
	fl.writeLine(&LogMessage{StdoutKind, time, line})
}

// OnStderr writes log message and marks it as stdout message
func (fl *FileLogger) OnStderr(line string, time time.Time) {
	fl.writeLine(&LogMessage{StderrKind, time, line})
}

// Close writes buffered data into file and closes it
func (fl *FileLogger) Close() {
	fl.Flush()
}

func (fl *FileLogger) writeLine(message *LogMessage) {
	fl.Lock()
	err := fl.encoder.Encode(message)
	if err != nil {
		log.Printf("Error appears on writing data to logs buffer. %s \n", err.Error())
	}
	if flushThreshold < fl.buffer.Len() {
		fl.doFlush()
	}
	fl.Unlock()
}

func (fl *FileLogger) doFlush() {
	if fl.buffer.Len() > 0 {
		f, err := os.OpenFile(fl.filename, os.O_WRONLY|os.O_APPEND|os.O_CREATE, 0666)
		if err != nil {
			log.Printf("Couldn't open file '%s' for flushing the buffer. %s \n", fl.filename, err.Error())
		} else {
			defer closeFile(f)
			_, err = fl.buffer.WriteTo(f)
			if err != nil {
				log.Printf("Error appears on flushing data to file '%s'. %s \n", fl.filename, err.Error())
			}
		}
	}
}

func closeFile(file *os.File) {
	if err := file.Close(); err != nil {
		log.Printf("Can't close file %s. Error: %s", file.Name(), err)
	}
}
