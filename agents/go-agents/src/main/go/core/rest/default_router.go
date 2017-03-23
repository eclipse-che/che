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
	"net/http"
	"regexp"

	"fmt"

	"github.com/julienschmidt/httprouter"
)

// DefaultRouter is router backed by github.com/julienschmidt/httprouter http router
type DefaultRouter struct {
	router *httprouter.Router
}

type toHandleFunc func(HTTPRouteHandlerFunc) httprouter.Handle

// NewDefaultRouter creates new instance of default implementation of router.
// basePath is path that should be cut from origin HTTP path and then matched to restRoutes.
func NewDefaultRouter(basePath string, restRoutes []RoutesGroup) Router {
	var jrouter = httprouter.New()
	defaultRouter := DefaultRouter{
		router: jrouter,
	}

	if basePath == "" {
		defaultRouter.addRoutes(restRoutes, toHandle)
	} else {
		if reg, err := regexp.Compile(basePath); err == nil {
			defaultRouter.addRoutes(restRoutes, getBasePathChoppingToHandleFunc(reg))
		} else {
			panic(fmt.Errorf("Base path '%s' is not a regexp. Error: %s", basePath, err))
		}
	}

	return &defaultRouter
}

func (router *DefaultRouter) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	router.router.ServeHTTP(w, r)
}

func (router *DefaultRouter) addRoutes(restRoutes []RoutesGroup, f toHandleFunc) {
	for _, routesGroup := range restRoutes {
		for _, route := range routesGroup.Items {
			router.router.Handle(
				route.Method,
				route.Path,
				f(route.HandleFunc),
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

func getBasePathChoppingToHandleFunc(reg *regexp.Regexp) toHandleFunc {
	return func(f HTTPRouteHandlerFunc) httprouter.Handle {
		return func(w http.ResponseWriter, r *http.Request, p httprouter.Params) {
			// if reg is prefix of request path remove the prefix
			if idx := reg.FindStringSubmatchIndex(r.URL.Path); len(idx) != 0 && idx[0] == 0 {
				r.URL.Path = r.URL.Path[idx[1]:]
				r.RequestURI = r.RequestURI[idx[1]:]
			}

			if err := f(w, r, routerParamsAdapter{params: p}); err != nil {
				WriteError(w, err)
			}
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
