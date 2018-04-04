//
// Copyright (c) 2012-2018 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

// Package process provides common utilities for native process execution
package process

import (
	"time"
)

// LogKind represents kind of source of log line - stdout or stderr.
type LogKind int

const (
	// StdoutKind match logs produced by stdout of a process.
	StdoutKind LogKind = iota
	// StderrKind match logs produced by stderr of a process.
	StderrKind
)

// DateTimeFormat is used for serialization/deserialization of dates in agents.
// Single date format keeps API consistent
var DateTimeFormat = time.RFC3339Nano

// LogMessage represents single log entry with timestamp and source of log.
type LogMessage struct {
	Kind LogKind   `json:"kind"`
	Time time.Time `json:"time"`
	Text string    `json:"text"`
}

// ParseTime parses string into Time.
// If time string is empty, then time provided as an argument is returned.
// If time string is invalid, then appropriate error is returned.
// If time string is valid then parsed time is returned.
func ParseTime(timeStr string, defTime time.Time) (time.Time, error) {
	if timeStr == "" {
		return defTime, nil
	}
	return time.Parse(DateTimeFormat, timeStr)
}

func (k LogKind) String() string {
	if k == StdoutKind {
		return "STDOUT"
	}
	return "STDERR"
}
