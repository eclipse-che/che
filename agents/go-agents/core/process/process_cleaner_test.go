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
	"sync"
	"testing"
	"time"
)

func TestCleanWithZeroThreshold(t *testing.T) {
	p1 := &MachineProcess{Pid: 1, mutex: &sync.RWMutex{}, Alive: false, deathTime: time.Now().Add(-time.Hour)}
	p2 := &MachineProcess{Pid: 2, mutex: &sync.RWMutex{}, Alive: false, deathTime: time.Now()}
	p3 := &MachineProcess{Pid: 3, mutex: &sync.RWMutex{}, Alive: true}

	processes.Lock()
	processes.items[1] = p1
	processes.items[2] = p2
	processes.items[3] = p3
	processes.Unlock()

	NewCleaner(0, 0).CleanOnce()

	processMustNotExist(p1.Pid, t)
	processMustNotExist(p2.Pid, t)
	processMustExist(p3.Pid, t)
}

func TestCleansOnlyExpiredProcesses(t *testing.T) {
	p1 := &MachineProcess{Pid: 1, Alive: false, mutex: &sync.RWMutex{}, deathTime: time.Now().Add(-time.Hour)}
	p2 := &MachineProcess{Pid: 2, Alive: false, mutex: &sync.RWMutex{}, deathTime: time.Now().Add(-time.Minute * 45)}
	p3 := &MachineProcess{Pid: 3, Alive: false, mutex: &sync.RWMutex{}, deathTime: time.Now().Add(-time.Minute * 15)}
	p4 := &MachineProcess{Pid: 4, Alive: true, mutex: &sync.RWMutex{}}

	processes.Lock()
	processes.items[1] = p1
	processes.items[2] = p2
	processes.items[3] = p3
	processes.items[4] = p4
	processes.Unlock()

	// cleanup immediately
	(&Cleaner{CleanupPeriod: 0, CleanupThreshold: time.Minute * 30}).CleanOnce()

	processMustNotExist(p1.Pid, t)
	processMustNotExist(p2.Pid, t)
	processMustExist(p3.Pid, t)
	processMustExist(p4.Pid, t)
}

func processMustNotExist(pid uint64, t *testing.T) {
	_, err := Get(pid)
	if err == nil {
		t.Fatalf("Process with id '%d' must not exist", pid)
	}
	if _, ok := err.(*NoProcessError); !ok {
		t.Fatalf("The error must be of type NoProcessError, error message: %s", err.Error())
	}
}

func processMustExist(pid uint64, t *testing.T) {
	if _, err := Get(pid); err != nil {
		t.Fatalf("Process with pid '%d' must exist, but error occurred '%s'", pid, err.Error())
	}
}
