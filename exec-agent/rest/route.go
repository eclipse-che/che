package rest

import (
	"fmt"
	"net/http"
	"strings"
)

const (
	maxNameLen   = 40
	maxMethodLen = len("DELETE")
)

// Handler for http routes
// vars variable contain only path parameters if any specified for given route
type HttpRouteHandlerFunc func(w http.ResponseWriter, r *http.Request) error

// Describes route for http requests
type Route struct {

	// Http method e.g. 'GET'
	Method string

	// The name of the http route, used in logs
	// this name is unique for all the application http routes
	// example: 'StartProcess'
	Name string

	// The path of the http route which this route is mapped to
	// example: '/process'
	Path string

	// The function used for handling http request
	HandleFunc HttpRouteHandlerFunc
}

// Named group of http routes, those groups
// should be defined by separate apis, and then combined together
type RoutesGroup struct {

	// The name of this group e.g.: 'ProcessRoutes'
	Name string

	// The http routes of this group
	Items []Route
}

func (r *Route) String() string {
	name := r.Name + " " + strings.Repeat(".", maxNameLen-len(r.Name))
	method := r.Method + strings.Repeat(" ", maxMethodLen-len(r.Method))
	return fmt.Sprintf("%s %s %s", name, method, r.Path)
}

func ToHttpHandlerFunc(routeHF HttpRouteHandlerFunc) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		// Delegate call to the defined handler func
		err := routeHF(w, r)
		// Consider all the errors different from ApiError as server error
		if err != nil {

			// Figure out whether error is api error
			apiErr, ok := err.(ApiError)
			if !ok {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			// If it is then respond with an appropriate error code
			http.Error(w, apiErr.Error(), apiErr.Code)
		}
	}
}
