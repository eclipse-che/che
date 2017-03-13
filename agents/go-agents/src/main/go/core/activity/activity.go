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

package activity

import (
	"log"
	"net/http"
	"os"
	"time"
)

// time in seconds to wait, after last sent activity request, before next requests can be sent
const threshold int64 = 60

var (
	// ActivityTrackingEnabled defines whether activity tracking should be used
	ActivityTrackingEnabled = false
	// Tracker provides activity notification API
	Tracker     = &WorkspaceActivityTracker{}
	workspaceID = os.Getenv("CHE_WORKSPACE_ID")
	// APIEndpoint points to url of workspace master server
	APIEndpoint string
)

// WorkspaceActivityTracker provides workspace activity notification API
type WorkspaceActivityTracker struct {
	active         bool
	lastUpdateTime int64
}

// Notify ensures that workspace master knows about recent activity of a workspace
func (wa *WorkspaceActivityTracker) Notify() {
	t := time.Now().Unix()
	if t < (wa.lastUpdateTime + threshold) {
		wa.active = true
	} else {
		go makeActivityRequest()
		wa.lastUpdateTime = t
	}
}

// StartTracking runs scheduler that continiously notifies workspace master
// if workspace activity was submitted with function Notify.
// Since it is synchronious function it should be started at separate thread.
func (wa *WorkspaceActivityTracker) StartTracking() {
	ticker := time.NewTicker(time.Minute)
	defer ticker.Stop()
	for range ticker.C {
		if wa.active {
			go makeActivityRequest()
			wa.active = false
		}
	}
}

func makeActivityRequest() {
	req, err := http.NewRequest(http.MethodPut, APIEndpoint+"/activity/"+workspaceID, nil)
	if err != nil {
		panic(err)
	}
	client := &http.Client{}
	_, err = client.Do(req)

	if err != nil {
		log.Printf("Failed to notify user activity: %s\n", err)
	}
}
