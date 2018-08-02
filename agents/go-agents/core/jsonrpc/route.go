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

package jsonrpc

import (
	"encoding/json"
	"log"
	"sync"
)

// DefaultRouter is a default package router.
// It might be used as tunnels request handler.
var DefaultRouter = NewRouter()

// RegRoute registers route using package DefaultRouter.
func RegRoute(r Route) { DefaultRouter.Register(r) }

// RegRoutesGroup registers routes group using package DefaultRouter.
func RegRoutesGroup(rg RoutesGroup) { DefaultRouter.RegisterGroup(rg) }

// RegRoutesGroups registers routes groups using package DefaultRouter.
func RegRoutesGroups(rgs []RoutesGroup) { DefaultRouter.RegisterGroups(rgs) }

// DecodeFunc used to decode route params for forth handling by HandleFunc.
type DecodeFunc func(body []byte) (interface{}, error)

// HandleFunc used to handle route request.
type HandleFunc func(tun *Tunnel, params interface{}, t RespTransmitter)

// Route defines named operation and its handler.
type Route struct {

	// Method is the operation name like defined by Request.Method.
	Method string

	// Decode used for decoding raw request parameters
	// into the certain object. If decoding is okay, then
	// decoded value will be passed to the Handle
	// of this request route, so it is up to the route
	// - to define type safe couple of Decode & Handle.
	Decode DecodeFunc

	// Handle handler for decoded request parameters.
	// If handler function can't perform the operation then
	// handler function should either return an error, or
	// send it directly within transmitter#SendError func.
	// Params is a value returned from the Decode.
	Handle HandleFunc
}

// FactoryDec uses given function to get an instance of object
// and then unmarshal params json into that object.
// The result of this function can be used as Route.Decode function.
func FactoryDec(create func() interface{}) DecodeFunc {
	return func(body []byte) (interface{}, error) {
		v := create()
		if err := json.Unmarshal(body, &v); err != nil {
			return nil, err
		}
		return v, nil
	}
}

// HandleRet converts handle function without transmitter to the HandleFunc.
// Returned values will be sent with transmitter.
func HandleRet(f func(tun *Tunnel, params interface{}) (interface{}, error)) HandleFunc {
	return func(tun *Tunnel, params interface{}, t RespTransmitter) {
		if res, err := f(tun, params); err == nil {
			t.Send(res)
		} else {
			t.SendError(asJSONRPCErr(err))
		}
	}
}

// Unmarshal unpacks raw request params using route.Decode.
func (r Route) Unmarshal(params []byte) (interface{}, error) {
	if r.Decode == nil {
		return nil, nil
	}
	return r.Decode(params)
}

// Call handles request using Route.Handle.
func (r Route) Call(tun *Tunnel, params interface{}, rt RespTransmitter) { r.Handle(tun, params, rt) }

// RoutesGroup is named group of rpc routes.
type RoutesGroup struct {

	// Name is the name of this group.
	Name string

	// Items routes.
	Items []Route
}

// Router is a simple request dispatcher.
type Router struct {
	mutex  sync.RWMutex
	routes map[string]Route
}

// NewRouter returns a new router.
func NewRouter() *Router {
	return &Router{routes: make(map[string]Route)}
}

// Register registers a new route in this router.
func (r *Router) Register(route Route) {
	r.mutex.Lock()
	defer r.mutex.Unlock()
	r.routes[route.Method] = route
}

// RegisterGroup registers a whole routes group.
func (r *Router) RegisterGroup(group RoutesGroup) {
	for _, route := range group.Items {
		r.Register(route)
	}
}

// RegisterGroups registers
func (r *Router) RegisterGroups(groups []RoutesGroup) {
	for _, group := range groups {
		r.RegisterGroup(group)
	}
}

// FindHandler finds a route for a given method.
func (r *Router) FindHandler(method string) (MethodHandler, bool) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()
	route, ok := r.routes[method]
	return route, ok
}

// PrintRoutes prints provided rpc routes by group.
func PrintRoutes(rg []RoutesGroup) {
	log.Print("⇩ Registered RPCRoutes:\n\n")
	for _, group := range rg {
		log.Printf("%s:\n", group.Name)
		for _, route := range group.Items {
			log.Printf("✓ %s\n", route.Method)
		}
	}
}

func asJSONRPCErr(err error) *Error {
	if rpcerr, ok := err.(*Error); ok {
		return rpcerr
	}
	return NewError(InternalErrorCode, err)
}
