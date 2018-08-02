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
	"log"
	"os"
	"time"
)

// Cleaner cleanups processes that died
type Cleaner struct {
	CleanupPeriod    time.Duration
	CleanupThreshold time.Duration
}

// NewCleaner create new instance of Cleaner
func NewCleaner(period int, threshold int) *Cleaner {
	return &Cleaner{
		time.Duration(period) * time.Minute,
		time.Duration(threshold) * time.Minute,
	}
}

// CleanPeriodically schedules cleanups of processes that exited
// more than CleanupThreshold time before cleanup.
// This function is synchronous.
func (pc *Cleaner) CleanPeriodically() {
	ticker := time.NewTicker(pc.CleanupPeriod)
	defer ticker.Stop()
	for range ticker.C {
		pc.CleanOnce()
	}
}

// CleanOnce cleanups processes that died before Time.Now() minus CleanupThreshold.
//
//  process1.deathTime = 2
//  process2.deathTime = 5
//
//                    death bound           now
//                         v                 v
// timeline -> 1 --- 2 --- 3 --- 4 --- 5 --- 6 --- 7 ->
//                   ^                 ^
//                process1          process2
//
// the method execution will remove the process1.
func (pc *Cleaner) CleanOnce() {
	deathBound := time.Now().Add(-pc.CleanupThreshold)
	processes.Lock()
	for _, mp := range processes.items {
		mp.mutex.RLock()
		if !mp.Alive && mp.deathTime.Before(deathBound) {
			delete(processes.items, mp.Pid)
			if err := os.Remove(mp.logfileName); err != nil {
				if !os.IsNotExist(err) {
					log.Printf("Couldn't remove process logs file, '%s'", mp.logfileName)
				}
			}
		}
		mp.mutex.RUnlock()
	}
	processes.Unlock()
}
