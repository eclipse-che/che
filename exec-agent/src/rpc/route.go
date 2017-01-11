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

package rpc

import (
	"log"
	"sync"
)

var (
	routes = &routesMap{items: make(map[string]Route)}
)

// Describes route for rpc requests
type Route struct {

	// The operation name like defined by Request.Method
	Method string

	// The decoder used for decoding raw request parameters
	// into the certain object. If decoding is okay, then
	// decoded value will be passed to the HandlerFunc
	// of this request route, so it is up to the route
	// - to define type safe couple of DecoderFunc & HandlerFunc.
	DecoderFunc func(body []byte) (interface{}, error)

	// Defines handler for decoded request parameters.
	// If handler function can't perform the operation then
	// handler function should either return an error, or
	// send it directly within transmitter#SendError func.
	// Params is a value returned from the DecoderFunc.
	// If an error is returned from this function and the type
	// of the error is different from rpc.Error, it will be
	// published as internal rpc error(-32603).
	HandlerFunc func(params interface{}, t *Transmitter) error
}

// Named group of rpc routes
type RoutesGroup struct {
	// The name of this group e.g.: 'ProcessRpcRoutes'
	Name string

	// Rpc routes of this group
	Items []Route
}

// Defines lockable map for storing operation routes
type routesMap struct {
	sync.RWMutex
	items map[string]Route
}

// Gets route by the operation name
func (routes *routesMap) get(method string) (Route, bool) {
	routes.RLock()
	defer routes.RUnlock()
	item, ok := routes.items[method]
	return item, ok
}

// Returns true if route is added and false if route for such method
// already present(won't override it).
func (or *routesMap) add(r Route) bool {
	routes.Lock()
	defer routes.Unlock()
	_, ok := routes.items[r.Method]
	if ok {
		return false
	}
	routes.items[r.Method] = r
	return true
}

// Adds a new route, panics if such route already exists.
func RegisterRoute(route Route) {
	if !routes.add(route) {
		log.Fatalf("Couldn't register a new route, route for the operation '%s' already exists", route.Method)
	}
}
