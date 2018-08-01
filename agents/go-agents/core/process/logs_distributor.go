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
	"errors"
	"fmt"
	"os"
)

const (
	// DefaultMaxDirsCount defines max number of directories with logs on one level of FS
	DefaultMaxDirsCount = 16
)

// LogsDistributor distributes the logs between different directories
type LogsDistributor interface {

	// The implementor must guarantee that returned file name
	// is always the same for the same pid.
	// Returns an error if it is impossible to create hierarchy of
	// logs file parent folders, otherwise returns file path
	DirForPid(baseDir string, pid uint64) (string, error)
}

// DefaultLogsDistributor is default implementation of LogsDistributor
type DefaultLogsDistributor struct {
	MaxDirsCount uint
}

// NewLogsDistributor creates LogsDistributor instance
func NewLogsDistributor() LogsDistributor {
	return &DefaultLogsDistributor{
		MaxDirsCount: DefaultMaxDirsCount,
	}
}

// DirForPid finds directory that should hold logs data of certain PID
func (ld *DefaultLogsDistributor) DirForPid(baseDir string, pid uint64) (string, error) {
	// directories from 1 to maxDirsCount inclusive
	subDirName := (pid % uint64(ld.MaxDirsCount))

	// {baseLogsDir}/{subDirName}
	pidLogsDir := fmt.Sprintf("%s%c%d", baseDir, os.PathSeparator, subDirName)

	// Create subdirectory
	if info, err := os.Stat(pidLogsDir); os.IsNotExist(err) {
		if err = os.MkdirAll(pidLogsDir, os.ModePerm); err != nil {
			return "", err
		}
	} else if err != nil {
		return "", err
	} else if !info.IsDir() {
		m := fmt.Sprintf("Couldn't create a directory '%s', the name is taken by file", pidLogsDir)
		return "", errors.New(m)
	}
	return pidLogsDir, nil
}
