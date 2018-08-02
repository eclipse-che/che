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

package rest

import (
	"net/http"
	"regexp"

	"fmt"

	"github.com/julienschmidt/httprouter"
)

// DefaultRouter is router backed by github.com/julienschmidt/httprouter http router
type DefaultRouter struct {
	router *httprouter.Router
}

type basePathChoppingRouter struct {
	DefaultRouter
	reg *regexp.Regexp
}

// NewDefaultRouter creates new instance of default implementation of router.
// basePath is path that should be cut from origin HTTP path and then matched to restRoutes.
func NewDefaultRouter(basePath string, restRoutes []RoutesGroup) Router {
	var jrouter = httprouter.New()

	// base path chopping is not needed
	if basePath == "" {
		defaultRouter := &DefaultRouter{
			router: jrouter,
		}
		defaultRouter.addRoutes(restRoutes)
		return defaultRouter
	}

	// base path chopping is needed
	reg, err := regexp.Compile(basePath)
	if err != nil {
		panic(fmt.Errorf("Base path '%s' is not a regexp. Error: %s", basePath, err))
	}
	defaultRouter := &basePathChoppingRouter{
		DefaultRouter: DefaultRouter{router: jrouter},
		reg:           reg,
	}
	defaultRouter.addRoutes(restRoutes)
	return defaultRouter
}

func (router *DefaultRouter) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	router.router.ServeHTTP(w, r)
}

func (router *DefaultRouter) addRoutes(restRoutes []RoutesGroup) {
	for _, routesGroup := range restRoutes {
		for _, route := range routesGroup.Items {
			router.router.Handle(
				route.Method,
				route.Path,
				toHandle(route.HandleFunc),
			)
		}
	}
}

func (router *basePathChoppingRouter) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	// if reg is prefix of request path remove the prefix
	if idx := router.reg.FindStringSubmatchIndex(r.URL.Path); len(idx) != 0 && idx[0] == 0 {
		r.URL.Path = r.URL.Path[idx[1]:]
		r.RequestURI = r.RequestURI[idx[1]:]
	}
	router.router.ServeHTTP(w, r)
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
