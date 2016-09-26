package process

import (
	"log"
	"os"
	"time"
)

var (
	CleanupPeriodInMinutes    int
	CleanupThresholdInMinutes int
)

type Cleaner struct {
	period    time.Duration
	threshold time.Duration
}

func (c *Cleaner) CleanupPeriodically() {
	if c.threshold >= 0 {
		ticker := time.NewTicker(c.period)
		defer ticker.Stop()
		for range ticker.C {
			deadPoint := time.Now().Add(-c.threshold)
			processes.Lock()
			for _, mp := range processes.items {
				mp.lastUsedLock.RLock()
				if !mp.Alive && mp.lastUsed.Before(deadPoint) {
					delete(processes.items, mp.Pid)
					if err := os.Remove(mp.logfileName); err != nil {
						log.Printf("Couldn't remove process logs file, '%s'", mp.logfileName)
					}
				}
				mp.lastUsedLock.RUnlock()
			}
			processes.Unlock()
		}
	}
}

func NewCleaner() *Cleaner {
	return &Cleaner{
		period:    time.Duration(CleanupPeriodInMinutes) * time.Minute,
		threshold: time.Duration(CleanupThresholdInMinutes) * time.Minute,
	}
}
