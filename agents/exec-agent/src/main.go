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
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"
	"time"

	"regexp"

	"github.com/eclipse/che/agents/exec-agent/auth"
	"github.com/eclipse/che/agents/exec-agent/process"
	"github.com/eclipse/che/agents/exec-agent/rest"
	"github.com/eclipse/che/agents/exec-agent/rpc"
	"github.com/eclipse/che/agents/exec-agent/term"
	"github.com/julienschmidt/httprouter"
)

var (
	AppHttpRoutes = []rest.RoutesGroup{
		process.HttpRoutes,
		rpc.HttpRoutes,
		term.HttpRoutes,
	}

	AppOpRoutes = []rpc.RoutesGroup{
		process.RpcRoutes,
	}

	serverAddress string
	staticDir     string
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
		&staticDir,
		"static",
		"./static/",
		"path to the directory where static content is located",
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

	// terminal configuration
	flag.StringVar(
		&term.Cmd,
		"cmd",
		"/bin/bash",
		"shell interpreter and command to execute on slave side of the pty",
	)
	process.ShellInterpreter = term.Cmd

	// workspace master server configuration
	flag.StringVar(
		&apiEndpoint,
		"api-endpoint",
		os.Getenv("CHE_API"),
		`api-endpoint used by exec-agent modules(such as activity checker or authentication)
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

	// terminal configuration
	flag.BoolVar(
		&term.ActivityTrackingEnabled,
		"enable-activity-tracking",
		false,
		"whether workspace master will be notified about terminal activity",
	)

	// process executor configuration
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
	curDir, _ := os.Getwd()
	curDir += string(os.PathSeparator) + "logs"
	flag.StringVar(
		&process.LogsDir,
		"logs-dir",
		curDir,
		"base directory for process logs",
	)
}

func main() {
	flag.Parse()

	log.SetOutput(os.Stdout)

	// print configuration
	fmt.Println("Exec-agent configuration")
	fmt.Println("  Server")
	fmt.Printf("    - Address: %s\n", serverAddress)
	fmt.Printf("    - Static content: %s\n", staticDir)
	fmt.Printf("    - Base path: '%s'\n", basePath)
	fmt.Println("  Terminal")
	fmt.Printf("    - Slave command: '%s'\n", term.Cmd)
	fmt.Printf("    - Activity tracking enabled: %t\n", term.ActivityTrackingEnabled)
	if authEnabled {
		fmt.Println("  Authentication")
		fmt.Printf("    - Enabled: %t\n", authEnabled)
		fmt.Printf("    - Tokens expiration timeout: %dm\n", tokensExpirationTimeoutInMinutes)
	}
	fmt.Println("  Process executor")
	fmt.Printf("    - Logs dir: %s\n", process.LogsDir)
	if processCleanupPeriodInMinutes > 0 {
		fmt.Printf("    - Cleanup job period: %dm\n", processCleanupPeriodInMinutes)
		fmt.Printf("    - Not used & dead processes stay for: %dm\n", processCleanupThresholdInMinutes)
	}
	if authEnabled || term.ActivityTrackingEnabled {
		fmt.Println("  Workspace master server")
		fmt.Printf("    - API endpoint: %s\n", apiEndpoint)
	}
	fmt.Println()

	term.ApiEndpoint = apiEndpoint

	// process configuration
	if err := os.RemoveAll(process.LogsDir); err != nil {
		log.Fatal(err)
	}

	if processCleanupPeriodInMinutes > 0 {
		if processCleanupThresholdInMinutes < 0 {
			log.Fatal("Expected process cleanup threshold to be non negative value")
		}
		cleaner := process.NewCleaner(processCleanupPeriodInMinutes, processCleanupThresholdInMinutes)
		cleaner.CleanPeriodically()
	}

	// terminal configuration
	if term.ActivityTrackingEnabled {
		go term.Activity.StartTracking()
	}

	// register routes and http handlers
	router := httprouter.New()
	router.NotFound = http.FileServer(http.Dir(staticDir))

	fmt.Print("⇩ Registered HttpRoutes:\n\n")
	for _, routesGroup := range AppHttpRoutes {
		fmt.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			router.Handle(
				route.Method,
				route.Path,
				toHandle(route.HandleFunc),
			)
			fmt.Printf("✓ %s\n", &route)
		}
		fmt.Println()
	}

	fmt.Print("\n⇩ Registered RpcRoutes:\n\n")
	for _, routesGroup := range AppOpRoutes {
		fmt.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			fmt.Printf("✓ %s\n", route.Method)
			rpc.RegisterRoute(route)
		}
	}

	var handler http.Handler = router

	// required authentication for all the requests, if it is configured
	if authEnabled {
		cache := auth.NewCache(time.Minute*time.Duration(tokensExpirationTimeoutInMinutes), time.Minute*5)

		handler = auth.Handler{
			Delegate:    handler,
			ApiEndpoint: apiEndpoint,
			Cache:       cache,
			UnauthorizedHandler: func(w http.ResponseWriter, req *http.Request) {
				dropChannelsWithExpiredToken(req.URL.Query().Get("token"))
				http.Error(w, "Unauthorized", http.StatusUnauthorized)
			},
		}
	}

	// cut base path on requests, if it is configured
	if basePath != "" {
		if rx, err := regexp.Compile(basePath); err == nil {
			handler = basePathChopper{rx, handler}
		} else {
			log.Fatal(err)
		}
	}

	http.Handle("/", handler)

	server := &http.Server{
		Handler:      handler,
		Addr:         serverAddress,
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}
	log.Fatal(server.ListenAndServe())
}

func dropChannelsWithExpiredToken(token string) {
	for _, c := range rpc.GetChannels() {
		u, err := url.ParseRequestURI(c.RequestURI)
		if err != nil {
			log.Printf("Couldn't parse the RequestURI '%s' of channel '%s'", c.RequestURI, c.Id)
		} else if u.Query().Get("token") == token {
			log.Printf("Token for channel '%s' is expired, trying to drop the channel", c.Id)
			rpc.DropChannel(c.Id)
		}
	}
}

type routerParamsAdapter struct {
	params httprouter.Params
}

func (pa routerParamsAdapter) Get(param string) string {
	return pa.params.ByName(param)
}

func toHandle(f rest.HttpRouteHandlerFunc) httprouter.Handle {
	return func(w http.ResponseWriter, r *http.Request, p httprouter.Params) {
		if err := f(w, r, routerParamsAdapter{params: p}); err != nil {
			rest.WriteError(w, err)
		}
	}
}

type basePathChopper struct {
	pattern  *regexp.Regexp
	delegate http.Handler
}

func (c basePathChopper) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	// if request path starts with given base path
	if idx := c.pattern.FindStringSubmatchIndex(r.URL.Path); len(idx) != 0 && idx[0] == 0 {
		r.URL.Path = r.URL.Path[idx[1]:]
		r.RequestURI = r.RequestURI[idx[1]:]
	}
	c.delegate.ServeHTTP(w, r)
}
