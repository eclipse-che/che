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
	"bufio"
	"io"
	"log"
	"sync"
	"time"
)

type acceptLine func(line string)

// LogsPumper client consumes a message read by pumper
type LogsConsumer interface {

	// called on each line pumped from process stdout
	OnStdout(line string, time time.Time)

	// called on each line pumped from process stderr
	OnStderr(line string, time time.Time)

	// called when pumping is finished either by normal return or by error
	Close()
}

// Pumps lines from the stdout and stderr
type LogsPumper struct {
	stdout    io.Reader
	stderr    io.Reader
	clients   []LogsConsumer
	waitGroup sync.WaitGroup
}

func NewPumper(stdout io.Reader, stderr io.Reader) *LogsPumper {
	return &LogsPumper{
		stdout: stdout,
		stderr: stderr,
	}
}

func (pumper *LogsPumper) AddConsumer(consumer LogsConsumer) {
	pumper.clients = append(pumper.clients, consumer)
}

// Start 'pumping' logs from the stdout and stderr
// The method execution is synchronous and waits for
// both stderr and stdout to complete closing all the clients after
func (pumper *LogsPumper) Pump() {
	pumper.waitGroup.Add(2)

	// reading from stdout & stderr
	go pump(pumper.stdout, pumper.notifyStdout, &pumper.waitGroup)
	go pump(pumper.stderr, pumper.notifyStderr, &pumper.waitGroup)

	// cleanup after pumping is complete
	pumper.waitGroup.Wait()
	pumper.notifyClose()
}

func pump(r io.Reader, lineConsumer acceptLine, wg *sync.WaitGroup) {
	defer wg.Done()
	br := bufio.NewReader(r)
	for {
		line, err := br.ReadBytes('\n')

		if err != nil {
			if err != io.EOF {
				log.Println("Error pumping: " + err.Error())
			} else if len(line) != 0 {
				lineConsumer(string(line))
			}
			return
		}

		lineConsumer(string(line[:len(line)-1]))
	}
}

func (pumper *LogsPumper) notifyStdout(line string) {
	t := time.Now()
	for _, client := range pumper.clients {
		client.OnStdout(line, t)
	}
}

func (pumper *LogsPumper) notifyStderr(line string) {
	t := time.Now()
	for _, client := range pumper.clients {
		client.OnStderr(line, t)
	}
}

func (pumper *LogsPumper) notifyClose() {
	for _, client := range pumper.clients {
		client.Close()
	}
}
