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

type FileLogger struct {
	sync.RWMutex
	filename string
	buffer   *bytes.Buffer
	encoder  *json.Encoder
}

func NewLogger(filename string) (*FileLogger, error) {
	fl := &FileLogger{filename: filename}
	fl.buffer = &bytes.Buffer{}
	fl.encoder = json.NewEncoder(fl.buffer)

	// Trying to create logs file
	file, err := os.Create(filename)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	return fl, nil
}

func (fl *FileLogger) Flush() {
	fl.Lock()
	fl.doFlush()
	fl.Unlock()
}

func (fl *FileLogger) OnStdout(line string, time time.Time) {
	fl.writeLine(&LogMessage{StdoutKind, time, line})
}

func (fl *FileLogger) OnStderr(line string, time time.Time) {
	fl.writeLine(&LogMessage{StderrKind, time, line})
}

func (fl *FileLogger) Close() {
	fl.Flush()
}

func (fl *FileLogger) writeLine(message *LogMessage) {
	fl.Lock()
	fl.encoder.Encode(message)
	if flushThreshold < fl.buffer.Len() {
		fl.doFlush()
	}
	fl.Unlock()
}

func (fl *FileLogger) doFlush() {
	f, err := os.OpenFile(fl.filename, os.O_WRONLY|os.O_APPEND|os.O_CREATE, 0666)
	if err != nil {
		log.Printf("Couldn't open file '%s' for flushing the buffer. %s \n", fl.filename, err.Error())
	} else {
		defer f.Close()
		fl.buffer.WriteTo(f)
	}
}
