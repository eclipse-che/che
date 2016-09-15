package process

import (
	"flag"
	"log"
	"os"
	"time"
)

var (
	periodInMinutesFlag           int
	cleanupThresholdInMinutesFlag int
)

type Cleaner struct {
	period    time.Duration
	threshold time.Duration
}

func init() {
	flag.IntVar(&periodInMinutesFlag,
		"process-cleanup-period",
		2,
		"How often processs cleanup will happen(in minutes)")
	flag.IntVar(&cleanupThresholdInMinutesFlag, "process-lifetime", 60,
		`How much time will dead and unused process live(in minutes),
		if -1 passed then processes won't be cleaned at all`)
}

func (c *Cleaner) CleanupDeadUnusedProcesses() {
	if c.threshold >= 0 {
		ticker := time.NewTicker(c.period)
		defer ticker.Stop()
		for range ticker.C {
			deadPoint := time.Now().Add(-c.threshold)
			processes.Lock()
			for _, v := range processes.items {
				// TODO v.lastUsed should de done in read lock
				if !v.Alive && v.lastUsed.Before(deadPoint) {
					delete(processes.items, v.Pid)
					if err := os.Remove(v.logfileName); err != nil {
						log.Printf("Couldn't remove process logs file, '%s'", v.logfileName)
					}
				}
			}
			processes.Unlock()
		}
	}
}

func NewCleaner() *Cleaner {
	return &Cleaner{
		period:    time.Duration(periodInMinutesFlag) * time.Minute,
		threshold: time.Duration(cleanupThresholdInMinutesFlag) * time.Minute,
	}
}
