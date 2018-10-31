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
	"flag"
	"log"
	"net/http"
	"net/url"
	"os"
	"regexp"
	"strconv"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/auth"
	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che-go-jsonrpc/jsonrpcws"
	"github.com/eclipse/che/agents/go-agents/core/process"
	"github.com/eclipse/che/agents/go-agents/core/rest"
	"github.com/eclipse/che/agents/go-agents/core/rest/restutil"
	"github.com/eclipse/che/agents/go-agents/exec-agent/exec"
)

var (
	config = &execAgentConfig{}
)

func init() {
	config.init()
}

func main() {
	flag.Parse()

	log.SetOutput(os.Stdout)

	config.printAll()

	process.SetLogsDir(config.processLogsDir)
	process.SetShellInterpreter(config.processShellInterpreter)

	// remove old logs
	if err := process.WipeLogs(); err != nil {
		log.Fatal(err)
	}

	// start cleaner routine
	if config.processCleanupPeriodInMinutes > 0 {
		if config.processCleanupThresholdInMinutes < 0 {
			log.Fatal("Expected process cleanup threshold to be non negative value")
		}
		cleaner := process.NewCleaner(config.processCleanupPeriodInMinutes, config.processCleanupThresholdInMinutes)
		go cleaner.CleanPeriodically()
	}

	appHTTPRoutes := []rest.RoutesGroup{
		exec.HTTPRoutes,
		{
			Name: "Exec-Agent WebSocket routes",
			Items: []rest.Route{
				{
					Method: "GET",
					Path:   "/connect",
					Name:   "Connect to Exec-Agent(websocket)",
					HandleFunc: func(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
						conn, err := jsonrpcws.Upgrade(w, r)
						if err != nil {
							return err
						}
						tunnel := jsonrpc.NewManagedTunnel(conn)
						tunnel.SayHello()
						return nil
					},
				},
			},
		},
		{
			Name: "Exec-Agent liveness route",
			Items: []rest.Route{
				{
					Method:     "GET",
					Path:       "/liveness",
					Name:       "Check Exec-Agent liveness",
					HandleFunc: restutil.OKRespondingFunc,
				},
			},
		},
	}

	appOpRoutes := []jsonrpc.RoutesGroup{
		exec.RPCRoutes,
	}

	// register routes and http handlers
	r := rest.NewDefaultRouter(config.basePath, appHTTPRoutes)
	rest.PrintRoutes(appHTTPRoutes)
	jsonrpc.RegRoutesGroups(appOpRoutes)
	jsonrpc.PrintRoutes(appOpRoutes)

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
	return auth.NewCachingHandler(h, config.apiEndpoint, droppingRPCChannelsUnauthorizedHandler, cache, ignorePattern)
}

func droppingRPCChannelsUnauthorizedHandler(w http.ResponseWriter, req *http.Request, err error) {
	token := req.URL.Query().Get("token")
	for _, tun := range jsonrpc.GetTunnels() {
		if wsTun, ok := tun.Conn().(*jsonrpcws.NativeConnAdapter); ok {
			if u, err1 := url.ParseRequestURI(wsTun.RequestURI); err1 != nil {
				log.Printf("Couldn't parse the RequestURI '%s' of channel '%s'", wsTun.RequestURI, tun.ID())
			} else if u.Query().Get("token") == token {
				log.Printf("Token for channel '%s' is expired, trying to drop the channel", tun.ID())
				if dropped, ok := jsonrpc.Rm(tun.ID()); ok {
					dropped.Close()
				}
			}
		}
	}
	http.Error(w, err.Error(), http.StatusUnauthorized)
}

type execAgentConfig struct {
	serverAddress string
	basePath      string
	apiEndpoint   string

	authEnabled                      bool
	tokensExpirationTimeoutInMinutes uint

	processShellInterpreter          string
	processLogsDir                   string
	processCleanupThresholdInMinutes int
	processCleanupPeriodInMinutes    int
}

func (cfg *execAgentConfig) init() {
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
	For example for the server address 'localhost:9000', route path '/connect' and
	configured path '/api/' exec-agent server will serve the following route:
	'localhost:9000/api/connect'.
	Regexp syntax is supported`,
	)

	// workspace master server configuration
	flag.StringVar(
		&cfg.apiEndpoint,
		"api-endpoint",
		os.Getenv("CHE_API"),
		`api-endpoint used by exec-agent modules(such as authentication)
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

	// process executor configuration
	flag.StringVar(
		&cfg.processShellInterpreter,
		"cmd",
		process.DefaultShellInterpreter,
		"shell interpreter",
	)
	flag.IntVar(
		&cfg.processCleanupPeriodInMinutes,
		"process-cleanup-period",
		-1,
		"how often processs cleanup job will be executed(in minutes)",
	)
	flag.IntVar(&cfg.processCleanupThresholdInMinutes,
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
		&cfg.processLogsDir,
		"logs-dir",
		curDir,
		"base directory for process logs",
	)
}

func (cfg *execAgentConfig) printAll() {
	log.Println("Exec-agent configuration")
	log.Println("  Server")
	log.Printf("    - Address: %s\n", cfg.serverAddress)
	log.Printf("    - Base path: '%s'\n", cfg.basePath)
	if cfg.authEnabled {
		log.Println("  Authentication")
		log.Printf("    - Enabled: %t\n", cfg.authEnabled)
		log.Printf("    - Tokens expiration timeout: %dm\n", cfg.tokensExpirationTimeoutInMinutes)
		log.Println("  Workspace master server")
		log.Printf("    - API endpoint: %s\n", cfg.apiEndpoint)
	}
	log.Println("  Process executor")
	log.Printf("    - Logs dir: %s\n", cfg.processLogsDir)
	if cfg.processCleanupPeriodInMinutes > 0 {
		log.Printf("    - Cleanup job period: %dm\n", cfg.processCleanupPeriodInMinutes)
		log.Printf("    - Not used & dead processes stay for: %dm\n", cfg.processCleanupThresholdInMinutes)
	}
	log.Println()
}
