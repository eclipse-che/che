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

// Package auth provides simple way of authentication of http requests on workspace master
package auth

import (
	"errors"
	"fmt"
	"net/http"

	"github.com/eclipse/che/agents/go-agents/src/main/go/core/rest"
)

// TokenCache represents authentication tokens cache
type TokenCache interface {
	Put(token string)           // Put adds token into the cache.
	Expire(token string)        // Expire removes provided token from the cache.
	Contains(token string) bool // Contains returns true if token is present in the cache and false otherwise.
}

// Handler is HTTP handler that authenticates all the http calls on workspace master.
// Checks on workspace master if provided by request token is valid and calls ServerHTTP on delegate.
// Otherwise if UnauthorizedHandler is configured calls ServerHTTP on it.
// If it is not configured returns 401 with appropriate error message.
type Handler struct {
	// Handler that handles request when authentication is passed
	Delegate http.Handler
	// Endpoint of workspace master where http requests should be authenticated
	APIEndpoint string
	// Optional cache of tokens that passed authentication on workspace master
	Cache TokenCache
	// Optional handler that handles request when authentication failed
	UnauthorizedHandler http.HandlerFunc
}

func (handler Handler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	token := req.URL.Query().Get("token")
	if handler.Cache != nil {
		if handler.Cache.Contains(token) {
			handler.Delegate.ServeHTTP(w, req)
		}
	} else if err := authenticateOnMaster(handler.APIEndpoint, token); err == nil {
		if handler.Cache != nil {
			handler.Cache.Put(token)
		}
		handler.Delegate.ServeHTTP(w, req)
	} else if handler.UnauthorizedHandler != nil {
		handler.UnauthorizedHandler(w, req)
	} else {
		http.Error(w, err.Error(), http.StatusUnauthorized)
	}
}

func authenticateOnMaster(apiEndpoint string, tokenParam string) error {
	if tokenParam == "" {
		return rest.Unauthorized(errors.New("Authentication failed: missing 'token' query parameter"))
	}
	req, err := http.NewRequest("GET", apiEndpoint+"/machine/token/user/"+tokenParam, nil)
	if err != nil {
		return rest.Unauthorized(err)
	}
	req.Header.Add("Authorization", tokenParam)
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return rest.Unauthorized(err)
	}
	if resp.StatusCode != 200 {
		return rest.Unauthorized(fmt.Errorf("Authentication failed, token: %s is invalid", tokenParam))
	}
	return nil
}
