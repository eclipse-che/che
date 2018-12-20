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

package main

import (
	"log"
	"os"

	"crypto/tls"
	"crypto/x509"
	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che-go-jsonrpc/jsonrpcws"
	"github.com/eclipse/che/agents/go-agents/bootstrapper/booter"
	"github.com/eclipse/che/agents/go-agents/bootstrapper/cfg"
	"github.com/eclipse/che/agents/go-agents/core/process"
	"io/ioutil"
)

func main() {
	log.SetOutput(os.Stdout)

	cfg.Parse()
	cfg.Print()

	if cfg.SelfSignedCertificateFilePath != "" {
		configureCertPool(cfg.SelfSignedCertificateFilePath)
	}

	process.SetShellInterpreter("/bin/sh")

	booter.Init(
		cfg.RuntimeID,
		cfg.MachineName,
		cfg.InstallerTimeoutSec,
		cfg.CheckServersPeriodSec,
		cfg.LogsEndpointReconnectPeriodSec,
	)
	booter.AddAll(cfg.ReadInstallersConfig())

	// push statuses
	statusTun := connectOrFail(cfg.PushStatusesEndpoint, cfg.Token)
	booter.PushStatuses(statusTun)

	// push logs
	if len(cfg.PushLogsEndpoint) != 0 {
		connector := &wsDialConnector{
			endpoint: cfg.PushLogsEndpoint,
			token:    cfg.Token,
		}
		if cfg.PushLogsEndpoint == cfg.PushStatusesEndpoint {
			booter.PushLogs(statusTun, connector)
		} else {
			booter.PushLogs(connectOrFail(cfg.PushLogsEndpoint, cfg.Token), connector)
		}
	}

	if err := booter.Start(); err != nil {
		log.Fatal(err)
	}
}

func configureCertPool(customCertificateFilePath string) {
	// Get the SystemCertPool, continue with an empty pool on error
	rootCAs, _ := x509.SystemCertPool()
	if rootCAs == nil {
		rootCAs = x509.NewCertPool()
	}

	// Read in the cert file
	certs, err := ioutil.ReadFile(customCertificateFilePath)
	if err != nil {
		log.Fatalf("Failed to read custom certificate %q. Error: %v", customCertificateFilePath, err)
	}

	// Append our cert to the system pool
	if ok := rootCAs.AppendCertsFromPEM(certs); !ok {
		log.Fatalf("Failed to append %q to RootCAs: %v", customCertificateFilePath, err)
	}

	// Trust the augmented cert pool in our client
	jsonrpcws.DefaultDialer.TLSClientConfig = &tls.Config{
		RootCAs: rootCAs,
	}
}

func connectOrFail(endpoint string, token string) *jsonrpc.Tunnel {
	tunnel, err := connect(endpoint, token)
	if err != nil {
		log.Fatalf("Couldn't connect to endpoint '%s', due to error '%s'", endpoint, err)
	}
	return tunnel
}

func connect(endpoint string, token string) (*jsonrpc.Tunnel, error) {
	conn, err := jsonrpcws.Dial(endpoint, token)
	if err != nil {
		return nil, err
	}
	return jsonrpc.NewManagedTunnel(conn), nil
}

type wsDialConnector struct {
	endpoint string
	token    string
}

func (c *wsDialConnector) Connect() (*jsonrpc.Tunnel, error) { return connect(c.endpoint, c.token) }
