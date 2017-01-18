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
type HttpRouteHandlerFunc func(w http.ResponseWriter, r *http.Request, params Params) error

// An interface for getting mapped path parameters by their names
type Params interface {

	// Gets path parameter by it's name e.g.
	// for url template `/process/:id` and actual value `/process/123`
	// this method will return string '123'
	Get(name string) string
}

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

func WriteError(w http.ResponseWriter, err error) {
	if apiErr, ok := err.(ApiError); ok {
		http.Error(w, apiErr.Error(), apiErr.Code)
	} else {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}
