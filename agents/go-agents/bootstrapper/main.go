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

package main

import (
	"log"
	"os"

	"github.com/eclipse/che/agents/go-agents/bootstrapper/booter"
	"github.com/eclipse/che/agents/go-agents/bootstrapper/cfg"
	"github.com/eclipse/che/agents/go-agents/core/jsonrpc"
	"github.com/eclipse/che/agents/go-agents/core/jsonrpc/jsonrpcws"
)

func main() {
	log.SetOutput(os.Stdout)

	cfg.Parse()
	cfg.Print()

	booter.Init(
		cfg.RuntimeID,
		cfg.MachineName,
		cfg.InstallerTimeoutSec,
		cfg.CheckServersPeriodSec,
	)
	booter.AddAll(cfg.ReadInstallersConfig())

	// push statuses
	statusTun := connect(cfg.PushStatusesEndpoint)
	booter.PushStatuses(statusTun)

	// push logs
	if len(cfg.PushLogsEndpoint) != 0 {
		if cfg.PushLogsEndpoint == cfg.PushStatusesEndpoint {
			booter.PushLogs(statusTun)
		} else {
			booter.PushLogs(connect(cfg.PushLogsEndpoint))
		}
	}

	if err := booter.Start(); err != nil {
		log.Fatal(err)
	}
}

func connect(endpoint string) *jsonrpc.Tunnel {
	conn, err := jsonrpcws.Dial(endpoint)
	if err != nil {
		log.Fatalf("Couldn't connect to endpoint '%s', due to error '%s'", cfg.PushStatusesEndpoint, err)
	}
	tunnel := jsonrpc.NewManagedTunnel(conn)
	tunnel.Go()
	return tunnel
}
