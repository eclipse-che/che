package main

import (
	"flag"
	"fmt"
	"github.com/eclipse/che/exec-agent/op"
	"github.com/eclipse/che/exec-agent/process"
	"github.com/eclipse/che/exec-agent/rest"
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
		op.HttpRoutes,
		term.HttpRoutes,
	}

	AppOpRoutes = []op.RoutesGroup{
		process.OpRoutes,
	}

	serverAddress string
	staticFlag    string
)

func init() {
	flag.StringVar(&serverAddress, "addr", ":9000", "IP:PORT or :PORT the address to start the server on")
	flag.StringVar(&staticFlag, "static", "./static/", "path to static content")
}

func main() {
	flag.Parse()

	// cleanup logs dir
	os.RemoveAll(process.LogsDir)

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

	fmt.Print("\n⇩ Registered OperationRoutes:\n\n")
	for _, routesGroup := range AppOpRoutes {
		fmt.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			fmt.Printf("✓ %s\n", route.Operation)
			op.RegisterRoute(route)
		}
	}

	go process.NewCleaner().CleanupDeadUnusedProcesses()
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
