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

package exec

import (
	"errors"
	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che/agents/go-agents/core/process"
	"strconv"
	"strings"
)

const (
	// DefaultLogsPerPageLimit is default limit of logs per page on process output fetching
	DefaultLogsPerPageLimit = 50
)

func maskFromTypes(types string) uint64 {
	var mask uint64
	for _, t := range strings.Split(types, ",") {
		switch strings.ToLower(strings.TrimSpace(t)) {
		case "stderr":
			mask |= process.StderrBit
		case "stdout":
			mask |= process.StdoutBit
		case "process_status":
			mask |= process.StatusBit
		}
	}
	return mask
}

func parseTypes(types string) uint64 {
	var mask uint64 = process.DefaultMask
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
func checkCommand(command *process.Command) error {
	if command.Name == "" {
		return errors.New("Command name required")
	}
	if command.CommandLine == "" {
		return errors.New("Command line required")
	}
	return nil
}

type rpcProcessEventConsumer struct {
	tunnel *jsonrpc.Tunnel
}

// TODO rework process to use events.Bus
func (rpcConsumer *rpcProcessEventConsumer) Accept(e process.Event) {
	if err := rpcConsumer.tunnel.Notify(e.Type(), e); err != nil {
		// process lib will remove each subscriber which panics
		panic(err)
	}
}
