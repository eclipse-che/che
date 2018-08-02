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

// websocket/pty proxy server:
// This program wires a websocket to a pty master.
//
// Usage:
// go build -o che-websocket-terminal
// ./che-websocket-terminal -cmd /bin/bash -addr :9000
//
// Copyright 2014 Al Tobey tobert@gmail.com
// MIT License, see the LICENSE file
//
package main

import (
	"flag"
	"log"
	"net/http"
	"os"
	"regexp"
	"strconv"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/activity"
	"github.com/eclipse/che/agents/go-agents/core/auth"
	"github.com/eclipse/che/agents/go-agents/core/rest"
	"github.com/eclipse/che/agents/go-agents/core/rest/restutil"
	"github.com/eclipse/che/agents/go-agents/terminal-agent/term"
)

var (
	config = &terminalAgentConfig{}
)

func init() {
	config.init()
}

func main() {
	flag.Parse()

	log.SetOutput(os.Stdout)

	config.printAll()

	term.Cmd = config.shellInterpreter

	if config.activityTrackingEnabled {
		activity.Tracker = activity.NewTracker(config.workspaceID, config.apiEndpoint)
		go activity.Tracker.StartTracking()
	}

	appHTTPRoutes := []rest.RoutesGroup{
		term.HTTPRoutes,
		{
			Name: "Terminal-Agent liveness route",
			Items: []rest.Route{
				{
					Method:     "GET",
					Path:       "/liveness",
					Name:       "Check Terminal-Agent liveness",
					HandleFunc: restutil.OKRespondingFunc,
				},
			},
		},
	}

	// register routes and http handlers
	r := rest.NewDefaultRouter(config.basePath, appHTTPRoutes)
	rest.PrintRoutes(appHTTPRoutes)

	// do not protect liveness check endpoint
	var handler = wrapWithAuth(r, "/liveness")

	server := &http.Server{
		Handler:      handler,
		Addr:         config.serverAddress,
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}
	log.Fatal(server.ListenAndServe())
}

func wrapWithAuth(h http.Handler, ignoreMapping string) http.Handler {
	// required authentication for all the requests that match mappings, if auth is configured
	if !config.authEnabled {
		return h
	}

	ignorePattern := regexp.MustCompile(ignoreMapping)
	cache := auth.NewCache(time.Minute*time.Duration(config.tokensExpirationTimeoutInMinutes), time.Minute*5)
	return auth.NewCachingHandler(h, config.apiEndpoint, droppingTerminalConnectionsUnauthorizedHandler, cache, ignorePattern)
}

func droppingTerminalConnectionsUnauthorizedHandler(w http.ResponseWriter, req *http.Request, err error) {
	// TODO disconnect all the clients with the same token if authentication returned unauthorized.
}

type terminalAgentConfig struct {
	serverAddress string
	basePath      string
	apiEndpoint   string

	activityTrackingEnabled bool

	shellInterpreter string

	workspaceID                      string
	authEnabled                      bool
	tokensExpirationTimeoutInMinutes uint
}

func (cfg *terminalAgentConfig) init() {
	// server configuration
	flag.StringVar(
		&cfg.serverAddress,
		"addr",
		":9000",
		"IP:PORT or :PORT the address to start the server on",
	)
	flag.StringVar(
		&cfg.basePath,
		"path",
		"",
		`the base path for all the rpc & rest routes, so route paths are treated not
	as 'server_address + route_path' but 'server_address + path + route_path'.
	For example for the server address 'localhost:9000', route path '/pty' and
	configured path '/api/' terminal-agent server will serve the following route:
	'localhost:9000/api/pty'.
	Regexp syntax is supported`,
	)

	// terminal configuration
	flag.StringVar(
		&cfg.shellInterpreter,
		"cmd",
		"/bin/bash",
		"shell interpreter and command to execute on slave side of the pty",
	)
	flag.BoolVar(
		&cfg.activityTrackingEnabled,
		"enable-activity-tracking",
		false,
		"whether workspace master will be notified about workspace activity",
	)

	// workspace master server configuration
	flag.StringVar(
		&cfg.apiEndpoint,
		"api-endpoint",
		os.Getenv("CHE_API"),
		`api-endpoint used by terminal-agent modules(such as activity checker or authentication)
	to request workspace master. By default the value from 'CHE_API' environment variable is used`,
	)

	// auth configuration
	defaultAuthEnabled := false
	authEnabledEnv := os.Getenv("CHE_AUTH_ENABLED")
	b, e := strconv.ParseBool(authEnabledEnv)
	if e == nil {
		defaultAuthEnabled = b
	}
	flag.BoolVar(
		&cfg.authEnabled,
		"enable-auth",
		defaultAuthEnabled,
		"whether authenticate requests on workspace master before allowing them to proceed."+
			"By default the value from 'CHE_AUTH_ENABLED' environment variable is used or `false` if it is missing",
	)
	flag.UintVar(
		&cfg.tokensExpirationTimeoutInMinutes,
		"tokens-expiration-timeout",
		auth.DefaultTokensExpirationTimeoutInMinutes,
		"how much time machine tokens stay in cache(if auth is enabled)",
	)

	cfg.workspaceID = os.Getenv("CHE_WORKSPACE_ID")
}

func (cfg *terminalAgentConfig) printAll() {
	log.Println("Terminal-agent configuration")
	log.Println("  Server")
	log.Printf("    - Address: %s\n", cfg.serverAddress)
	log.Printf("    - Base path: '%s'\n", cfg.basePath)
	log.Println("  Terminal")
	log.Printf("    - Slave command: '%s'\n", term.Cmd)
	log.Printf("    - Activity tracking enabled: %t\n", cfg.activityTrackingEnabled)
	if cfg.authEnabled {
		log.Println("  Authentication")
		log.Printf("    - Enabled: %t\n", cfg.authEnabled)
		log.Printf("    - Tokens expiration timeout: %dm\n", cfg.tokensExpirationTimeoutInMinutes)
	}
	if cfg.authEnabled || cfg.activityTrackingEnabled {
		log.Println("  Workspace master server")
		log.Printf("    - API endpoint: %s\n", cfg.apiEndpoint)
	}
	log.Println()
}
