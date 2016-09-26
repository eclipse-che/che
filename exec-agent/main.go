package main

import (
	"flag"
	"fmt"
	"github.com/eclipse/che/exec-agent/auth"
	"github.com/eclipse/che/exec-agent/process"
	"github.com/eclipse/che/exec-agent/rest"
	"github.com/eclipse/che/exec-agent/rpc"
	"github.com/eclipse/che/exec-agent/term"
	"github.com/gorilla/mux"
	"log"
	"net/http"
	"os"
	"time"
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
	staticFlag    string
)

func init() {
	// Server configuration
	flag.StringVar(&serverAddress, "addr", ":9000", "IP:PORT or :PORT the address to start the server on")
	flag.StringVar(&staticFlag, "static", "./static/", "path to static content")

	// Auth configuration
	flag.BoolVar(&auth.Enabled, "enable-auth", false, "Whether authenticate on workspace master or not")
	flag.StringVar(&auth.ApiEndpoint,
		"auth-api-endpoint",
		os.Getenv("CHE_API_ENDPOINT"),
		"Auth api-endpoint, by default 'CHEAPI-ENDPOINT' environment variable is used for this")

	// Process configuration
	flag.IntVar(&process.CleanupPeriodInMinutes, "process-cleanup-period", 2, "How often processs cleanup will happen(in minutes)")
	flag.IntVar(&process.CleanupThresholdInMinutes,
		"process-lifetime",
		60,
		"How much time will dead and unused process live(in minutes), if -1 passed then processes won't be cleaned at all")
	curDir, _ := os.Getwd()
	curDir += string(os.PathSeparator) + "logs"
	flag.StringVar(&process.LogsDir, "logs-dir", curDir, "Base directory for process logs")
}

func main() {
	flag.Parse()

	// cleanup logs dir
	if err := os.RemoveAll(process.LogsDir); err != nil {
		log.Fatal(err)
	}

	router := mux.NewRouter().StrictSlash(true)
	fmt.Print("⇩ Registered HttpRoutes:\n\n")
	for _, routesGroup := range AppHttpRoutes {
		fmt.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			fmt.Printf("✓ %s\n", &route)
			router.
				Methods(route.Method).
				Path(route.Path).
				Name(route.Name).
				HandlerFunc(rest.ToHttpHandlerFunc(route.HandleFunc))
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

	go process.NewCleaner().CleanupPeriodically()
	if term.ActivityTrackingEnabled {
		go term.Activity.StartTracking()
	}

	router.PathPrefix("/").Handler(http.FileServer(http.Dir(staticFlag)))
	http.Handle("/", router)
	server := &http.Server{
		Handler:      router,
		Addr:         serverAddress,
		WriteTimeout: 10 * time.Second,
		ReadTimeout:  10 * time.Second,
	}
	log.Fatal(server.ListenAndServe())
}
