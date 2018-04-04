//
// Copyright (c) 2012-2018 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

package jsonrpctest

import (
	"time"

	"github.com/eclipse/che/agents/go-agents/core/jsonrpc"
)

// NewTmpTunnel creates a new running jsonrpc.Tunnel based on test connection
// and test request dispatcher which will be automatically closed after specified timeout.
func NewTmpTunnel(t time.Duration) (*jsonrpc.Tunnel, *ConnRecorder, *ReqRecorder) {
	connRecorder := NewConnRecorder()
	connRecorder.CloseAfter(t)
	reqRecorder := NewReqRecorder()
	reqRecorder.CloseAfter(t)
	tunnel := jsonrpc.NewTunnel(connRecorder, reqRecorder)
	tunnel.Go()
	return tunnel, connRecorder, reqRecorder
}
