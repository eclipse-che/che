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

package term

import (
	"log"
	"net/http"
	"os"
	"time"
)

// time in seconds to wait, after last sent activity request, before next requests can be sent
const threshold int64 = 60

var (
	ActivityTrackingEnabled = false
	Activity                = &WorkspaceActivity{}
	workspaceId             = os.Getenv("CHE_WORKSPACE_ID")
	ApiEndpoint             string
)

type WorkspaceActivity struct {
	active         bool
	lastUpdateTime int64
}

func (wa *WorkspaceActivity) Notify() {
	t := time.Now().Unix()
	if t < (wa.lastUpdateTime + threshold) {
		wa.active = true
	} else {
		go makeActivityRequest()
		wa.lastUpdateTime = t
	}
}

func (wa *WorkspaceActivity) StartTracking() {
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
	req, _ := http.NewRequest(http.MethodPut, ApiEndpoint+"/activity/"+workspaceId, nil)
	client := &http.Client{}
	_, err := client.Do(req)

	if err != nil {
		log.Printf("Failed to notify user activity in terminal: %s\n", err)
	}
}
