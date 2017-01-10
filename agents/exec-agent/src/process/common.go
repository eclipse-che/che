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
	"errors"
	"strconv"
	"strings"
	"time"
)

const (
	DefaultLogsPerPageLimit = 50
)

func maskFromTypes(types string) uint64 {
	var mask uint64
	for _, t := range strings.Split(types, ",") {
		switch strings.ToLower(strings.TrimSpace(t)) {
		case "stderr":
			mask |= StderrBit
		case "stdout":
			mask |= StdoutBit
		case "process_status":
			mask |= ProcessStatusBit
		}
	}
	return mask
}

func parseTypes(types string) uint64 {
	var mask uint64 = DefaultMask
	if types != "" {
		mask = maskFromTypes(types)
	}
	return mask
}

// Checks whether pid is valid and converts it to the uint64
func parsePid(strPid string) (uint64, error) {
	intPid, err := strconv.Atoi(strPid)
	if err != nil {
		return 0, errors.New("Pid value must be unsigned integer")
	}
	if intPid <= 0 {
		return 0, errors.New("Pid value must be unsigned integer")
	}
	return uint64(intPid), nil
}

// Checks whether command is valid
func checkCommand(command *Command) error {
	if command.Name == "" {
		return errors.New("Command name required")
	}
	if command.CommandLine == "" {
		return errors.New("Command line required")
	}
	return nil
}

// If time string is empty, then default time is returned
// If time string is invalid, then appropriate error is returned
// If time string is valid then parsed time is returned
func parseTime(timeStr string, defTime time.Time) (time.Time, error) {
	if timeStr == "" {
		return defTime, nil
	}
	return time.Parse(DateTimeFormat, timeStr)
}
