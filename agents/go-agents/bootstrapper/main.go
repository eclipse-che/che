//
// Copyright (c) 2012-2017 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
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
		cfg.LogsEndpointReconnectPeriodSec,
	)
	booter.AddAll(cfg.ReadInstallersConfig())

	// push statuses
	statusTun := connectOrFail(cfg.PushStatusesEndpoint)
	booter.PushStatuses(statusTun)

	// push logs
	if len(cfg.PushLogsEndpoint) != 0 {
		connector := &wsDialConnector{cfg.PushLogsEndpoint}
		if cfg.PushLogsEndpoint == cfg.PushStatusesEndpoint {
			booter.PushLogs(statusTun, connector)
		} else {
			booter.PushLogs(connectOrFail(cfg.PushLogsEndpoint), connector)
		}
	}

	if err := booter.Start(); err != nil {
		log.Fatal(err)
	}
}

func connectOrFail(endpoint string) *jsonrpc.Tunnel {
	tunnel, err := connect(endpoint)
	if err != nil {
		log.Fatalf("Couldn't connect to endpoint '%s', due to error '%s'", endpoint, err)
	}
	return tunnel
}

func connect(endpoint string) (*jsonrpc.Tunnel, error) {
	conn, err := jsonrpcws.Dial(endpoint)
	if err != nil {
		return nil, err
	}
	return jsonrpc.NewManagedTunnel(conn), nil
}

type wsDialConnector struct{ endpoint string }

func (c *wsDialConnector) Connect() (*jsonrpc.Tunnel, error) { return connect(c.endpoint) }
