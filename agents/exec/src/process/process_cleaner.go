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
	"log"
	"os"
	"time"
)

type Cleaner struct {
	CleanupPeriod    time.Duration
	CleanupThreshold time.Duration
}

func NewCleaner(period int, threshold int) *Cleaner {
	return &Cleaner{
		time.Duration(period) * time.Minute,
		time.Duration(threshold) * time.Minute,
	}
}

func (c *Cleaner) CleanPeriodically() {
	ticker := time.NewTicker(c.CleanupPeriod)
	defer ticker.Stop()
	for range ticker.C {
		c.CleanOnce()
	}
}

func (pc *Cleaner) CleanOnce() {
	deadPoint := time.Now().Add(-pc.CleanupThreshold)
	processes.Lock()
	for _, mp := range processes.items {
		mp.lastUsedLock.RLock()
		if !mp.Alive && mp.lastUsed.Before(deadPoint) {
			delete(processes.items, mp.Pid)
			if err := os.Remove(mp.logfileName); err != nil {
				if !os.IsNotExist(err) {
					log.Printf("Couldn't remove process logs file, '%s'", mp.logfileName)
				}
			}
		}
		mp.lastUsedLock.RUnlock()
	}
	processes.Unlock()
}
