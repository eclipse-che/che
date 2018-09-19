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

package activity

import (
	"log"
	"net/http"
	"time"
)

// time in seconds to wait, after last sent activity request, before next requests can be sent
const threshold int64 = 60

var (
	// Tracker provides workspace activity notification client
	Tracker WorkspaceActivityTracker = &NoOpActivityTracker{}
)

// WorkspaceActivityTracker provides workspace activity notification API
type WorkspaceActivityTracker interface {
	Notify()
	StartTracking()
}

// Default impl of WorkspaceActivityTracker
type tracker struct {
	WorkspaceActivityTracker

	active         bool
	lastUpdateTime int64
	activityAPI    string
}

// NewTracker creates default implementation of activity tracker
func NewTracker(wsID string, apiEndpoint string) WorkspaceActivityTracker {
	return &tracker{
		activityAPI: apiEndpoint + "/activity/" + wsID,
	}
}

// Notify ensures that workspace master knows about recent activity of a workspace
func (tr *tracker) Notify() {
	t := time.Now().Unix()
	if t < (tr.lastUpdateTime + threshold) {
		tr.active = true
	} else {
		go tr.makeActivityRequest()
		tr.lastUpdateTime = t
	}
}

// StartTracking runs scheduler that continiously notifies workspace master
// if workspace activity was submitted with function Notify.
// Since it is synchronious function it should be started at separate thread.
func (tr *tracker) StartTracking() {
	ticker := time.NewTicker(time.Minute)
	defer ticker.Stop()
	for range ticker.C {
		if tr.active {
			go tr.makeActivityRequest()
			tr.active = false
		}
	}
}

func (tr *tracker) makeActivityRequest() {
	req, err := http.NewRequest(http.MethodPut, tr.activityAPI, nil)
	if err != nil {
		panic(err)
	}
	client := &http.Client{}
	_, err = client.Do(req)

	if err != nil {
		log.Printf("Failed to notify user activity: %s\n", err)
	}
}
