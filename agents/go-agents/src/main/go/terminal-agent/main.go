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
	"time"

	"github.com/eclipse/che/agents/go-agents/src/main/go/core/activity"
	"github.com/eclipse/che/agents/go-agents/src/main/go/core/auth"
	"github.com/eclipse/che/agents/go-agents/src/main/go/core/rest"
	"github.com/eclipse/che/agents/go-agents/src/main/go/terminal-agent/term"
)

var (
	serverAddress string
	basePath      string
	apiEndpoint   string

	workspaceID                      string
	authEnabled                      bool
	tokensExpirationTimeoutInMinutes uint
)

func init() {
	// server configuration
	flag.StringVar(
		&serverAddress,
		"addr",
		":9000",
		"IP:PORT or :PORT the address to start the server on",
	)
	flag.StringVar(
		&basePath,
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
		&term.Cmd,
		"cmd",
		"/bin/bash",
		"shell interpreter and command to execute on slave side of the pty",
	)
	flag.BoolVar(
		&activity.ActivityTrackingEnabled,
		"enable-activity-tracking",
		false,
		"whether workspace master will be notified about workspace activity",
	)

	// workspace master server configuration
	flag.StringVar(
		&apiEndpoint,
		"api-endpoint",
		os.Getenv("CHE_API"),
		`api-endpoint used by terminal-agent modules(such as activity checker or authentication)
	to request workspace master. By default the value from 'CHE_API' environment variable is used`,
	)

	// auth configuration
	flag.BoolVar(
		&authEnabled,
		"enable-auth",
		false,
		"whether authenicate requests on workspace master before allowing them to proceed",
	)
	flag.UintVar(
		&tokensExpirationTimeoutInMinutes,
		"tokens-expiration-timeout",
		auth.DefaultTokensExpirationTimeoutInMinutes,
		"how much time machine tokens stay in cache(if auth is enabled)",
	)

	workspaceID = os.Getenv("CHE_WORKSPACE_ID")
}

func main() {
	flag.Parse()

	log.SetOutput(os.Stdout)

	printConfiguration()

	if activity.ActivityTrackingEnabled {
		activity.Tracker = activity.NewTracker(workspaceID, apiEndpoint)
		go activity.Tracker.StartTracking()
	}

	appHTTPRoutes := []rest.RoutesGroup{
		term.HTTPRoutes,
	}

	// register routes and http handlers
	r := rest.NewDefaultRouter(basePath, appHTTPRoutes)
	rest.PrintRoutes(appHTTPRoutes)

	var handler = getHandler(r)
	http.Handle("/", handler)

	server := &http.Server{
		Handler:      handler,
		Addr:         serverAddress,
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}
	log.Fatal(server.ListenAndServe())
}

func getHandler(h http.Handler) http.Handler {
	// required authentication for all the requests, if it is configured
	if authEnabled {
		cache := auth.NewCache(time.Minute*time.Duration(tokensExpirationTimeoutInMinutes), time.Minute*5)
		return auth.NewCachingHandler(h, apiEndpoint, droppingTerminalConnectionsUnauthorizedHandler, cache)
	}

	return h
}

func droppingTerminalConnectionsUnauthorizedHandler(w http.ResponseWriter, req *http.Request, err error) {
	// TODO disconnect all the clients with the same token if authentication returned unauthorized.
}

func printConfiguration() {
	log.Println("Terminal-agent configuration")
	log.Println("  Server")
	log.Printf("    - Address: %s\n", serverAddress)
	log.Printf("    - Base path: '%s'\n", basePath)
	log.Println("  Terminal")
	log.Printf("    - Slave command: '%s'\n", term.Cmd)
	log.Printf("    - Activity tracking enabled: %t\n", activity.ActivityTrackingEnabled)
	if authEnabled {
		log.Println("  Authentication")
		log.Printf("    - Enabled: %t\n", authEnabled)
		log.Printf("    - Tokens expiration timeout: %dm\n", tokensExpirationTimeoutInMinutes)
	}
	if authEnabled || activity.ActivityTrackingEnabled {
		log.Println("  Workspace master server")
		log.Printf("    - API endpoint: %s\n", apiEndpoint)
	}
	log.Println()
}
