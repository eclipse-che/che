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
	"flag"
	"log"
	"net/http"
	"net/url"
	"os"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/auth"
	"github.com/eclipse/che/agents/go-agents/core/rest"
	"github.com/eclipse/che/agents/go-agents/core/rpc"
	"github.com/eclipse/che/agents/go-agents/exec-agent/exec"
)

var (
	serverAddress string
	basePath      string
	apiEndpoint   string

	authEnabled                      bool
	tokensExpirationTimeoutInMinutes uint

	processCleanupThresholdInMinutes int
	processCleanupPeriodInMinutes    int
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
	For example for the server address 'localhost:9000', route path '/connect' and
	configured path '/api/' exec-agent server will serve the following route:
	'localhost:9000/api/connect'.
	Regexp syntax is supported`,
	)

	// workspace master server configuration
	flag.StringVar(
		&apiEndpoint,
		"api-endpoint",
		os.Getenv("CHE_API"),
		`api-endpoint used by exec-agent modules(such as authentication)
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

	// process executor configuration
	flag.StringVar(
		&exec.ShellInterpreter,
		"cmd",
		"/bin/bash",
		"shell interpreter",
	)
	flag.IntVar(
		&processCleanupPeriodInMinutes,
		"process-cleanup-period",
		-1,
		"how often processs cleanup job will be executed(in minutes)",
	)
	flag.IntVar(&processCleanupThresholdInMinutes,
		"process-cleanup-threshold",
		-1,
		`how much time will dead and unused process stay(in minutes),
	if -1 passed then processes won't be cleaned at all. Please note that the time
	of real cleanup is between configured threshold and threshold + process-cleanup-period.`,
	)
	curDir, err := os.Getwd()
	if err != nil {
		log.Fatal(err)
	}
	curDir += string(os.PathSeparator) + "logs"
	flag.StringVar(
		&exec.LogsDir,
		"logs-dir",
		curDir,
		"base directory for process logs",
	)
}

func main() {
	flag.Parse()

	log.SetOutput(os.Stdout)

	printConfiguration()

	// remove old logs
	if err := os.RemoveAll(exec.LogsDir); err != nil {
		log.Fatal(err)
	}

	// start cleaner routine
	if processCleanupPeriodInMinutes > 0 {
		if processCleanupThresholdInMinutes < 0 {
			log.Fatal("Expected process cleanup threshold to be non negative value")
		}
		cleaner := exec.NewCleaner(processCleanupPeriodInMinutes, processCleanupThresholdInMinutes)
		go cleaner.CleanPeriodically()
	}

	appHTTPRoutes := []rest.RoutesGroup{
		exec.HTTPRoutes,
		rpc.HTTPRoutes,
	}

	appOpRoutes := []rpc.RoutesGroup{
		exec.RPCRoutes,
	}

	// register routes and http handlers
	r := rest.NewDefaultRouter(basePath, appHTTPRoutes)
	rest.PrintRoutes(appHTTPRoutes)
	rpc.RegisterRoutes(appOpRoutes)
	rpc.PrintRoutes(appOpRoutes)

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
		return auth.NewCachingHandler(h, apiEndpoint, droppingRPCChannelsUnauthorizedHandler, cache)
	}

	return h
}

func droppingRPCChannelsUnauthorizedHandler(w http.ResponseWriter, req *http.Request, err error) {
	token := req.URL.Query().Get("token")
	for _, c := range rpc.GetChannels() {
		if u, err1 := url.ParseRequestURI(c.RequestURI); err1 != nil {
			log.Printf("Couldn't parse the RequestURI '%s' of channel '%s'", c.RequestURI, c.ID)
		} else if u.Query().Get("token") == token {
			log.Printf("Token for channel '%s' is expired, trying to drop the channel", c.ID)
			rpc.DropChannel(c.ID)
		}
	}
	http.Error(w, err.Error(), http.StatusUnauthorized)
}

func printConfiguration() {
	log.Println("Exec-agent configuration")
	log.Println("  Server")
	log.Printf("    - Address: %s\n", serverAddress)
	log.Printf("    - Base path: '%s'\n", basePath)
	if authEnabled {
		log.Println("  Authentication")
		log.Printf("    - Enabled: %t\n", authEnabled)
		log.Printf("    - Tokens expiration timeout: %dm\n", tokensExpirationTimeoutInMinutes)
	}
	log.Println("  Process executor")
	log.Printf("    - Logs dir: %s\n", exec.LogsDir)
	if processCleanupPeriodInMinutes > 0 {
		log.Printf("    - Cleanup job period: %dm\n", processCleanupPeriodInMinutes)
		log.Printf("    - Not used & dead processes stay for: %dm\n", processCleanupThresholdInMinutes)
	}
	if authEnabled {
		log.Println("  Workspace master server")
		log.Printf("    - API endpoint: %s\n", apiEndpoint)
	}
	log.Println()
}
