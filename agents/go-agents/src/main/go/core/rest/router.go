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
	"log"
	"net/http"

	"github.com/julienschmidt/httprouter"
)

// Router provides http requests routing capabilities
type Router interface {
	http.Handler
}

type routerImpl struct {
	router *httprouter.Router
}

func (router *routerImpl) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	router.router.ServeHTTP(w, r)
}

// NewRouter creates new instance of router
func NewRouter(restRoutes []RoutesGroup) Router {
	var r = httprouter.New()

	addRoutes(restRoutes, r)

	return &routerImpl{
		router: r,
	}
}

// PrintRoutes prints description of routes in provided slice of groups
func PrintRoutes(restRoutes []RoutesGroup) {
	log.Print("⇩ Registered HTTPRoutes:\n\n")
	for _, routesGroup := range restRoutes {
		log.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			log.Printf("✓ %s\n", &route)
		}
		log.Println()
	}
}

func addRoutes(restRoutes []RoutesGroup, r *httprouter.Router) {
	for _, routesGroup := range restRoutes {
		for _, route := range routesGroup.Items {
			r.Handle(
				route.Method,
				route.Path,
				toHandle(route.HandleFunc),
			)
		}
	}
}

func toHandle(f HTTPRouteHandlerFunc) httprouter.Handle {
	return func(w http.ResponseWriter, r *http.Request, p httprouter.Params) {
		if err := f(w, r, routerParamsAdapter{params: p}); err != nil {
			WriteError(w, err)
		}
	}
}

// Implementation of route.Params
type routerParamsAdapter struct {
	params httprouter.Params
}

func (pa routerParamsAdapter) Get(param string) string {
	return pa.params.ByName(param)
}
