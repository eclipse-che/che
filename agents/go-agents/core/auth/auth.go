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

// Package auth provides simple way of authentication of http requests on workspace master
package auth

import (
	"errors"
	"net/http"
	"regexp"

	"fmt"
	"github.com/dgrijalva/jwt-go"
	"os"
	"strings"
)

const (
	TokenKind      = "machine_token"
	WorkspaceIdEnv = "CHE_WORKSPACE_ID"
	BearerPrefix   = "bearer "
)

var (
	WorkspaceId                 = os.Getenv(WorkspaceIdEnv)
	KeyProvider SignKeyProvider = &EnvSignKeyProvider{}
)

// TokenCache represents authentication tokens cache
type TokenCache interface {
	// Put adds token into the cache.
	Put(token string)
	// Expire removes provided token from the cache.
	Expire(token string)
	// Contains returns true if token is present in the cache and false otherwise.
	Contains(token string) bool
}

// UnauthorizedHandler handles request when authentication failed
type UnauthorizedHandler func(w http.ResponseWriter, r *http.Request, err error)

type handler struct {
	delegate            http.Handler
	apiEndpoint         string
	unauthorizedHandler UnauthorizedHandler
	ignoreMapping       *regexp.Regexp
}

type cachingHandler struct {
	delegate            http.Handler
	apiEndpoint         string
	cache               TokenCache
	unauthorizedHandler UnauthorizedHandler
	ignoreMapping       *regexp.Regexp
}

type JWTClaims struct {
	*jwt.StandardClaims
	UserId      string `json:"uid,omitempty"`
	UserName    string `json:"uname,omitempty"`
	WorkspaceId string `json:"wsid,omitempty"`
}

// NewHandler creates HTTP handler that authenticates http calls that don't match provided non authenticated path pattern on workspace master.
// Checks on workspace master if provided by request token is valid and calls ServerHTTP on delegate.
// Otherwise if UnauthorizedHandler is configured calls ServerHTTP on it.
// If it is not configured returns 401 with appropriate error message.
func NewHandler(delegate http.Handler, apiEndpoint string, unauthorizedHandler UnauthorizedHandler, ignoreMapping *regexp.Regexp) http.Handler {
	if unauthorizedHandler == nil {
		unauthorizedHandler = defaultUnauthorizedHandler
	}
	return &handler{
		delegate:            delegate,
		apiEndpoint:         apiEndpoint,
		unauthorizedHandler: unauthorizedHandler,
		ignoreMapping:       ignoreMapping,
	}
}

// NewCachingHandler creates HTTP handler that authenticates http calls that don't match provided non authenticated path pattern on workspace master.
// Checks on workspace master if provided by request token is valid and calls ServerHTTP on delegate.
// Otherwise if UnauthorizedHandler is configured calls ServerHTTP on it.
// If it is not configured returns 401 with appropriate error message.
// This implementation caches the results of authentication to speedup request handling.
func NewCachingHandler(delegate http.Handler, apiEndpoint string, unauthorizedHandler UnauthorizedHandler, cache TokenCache, ignoreMapping *regexp.Regexp) http.Handler {
	if cache == nil {
		panic("TokenCache argument of NewCachingHandler required")
	}
	if unauthorizedHandler == nil {
		unauthorizedHandler = defaultUnauthorizedHandler
	}
	return &cachingHandler{
		delegate:            delegate,
		apiEndpoint:         apiEndpoint,
		cache:               cache,
		unauthorizedHandler: unauthorizedHandler,
		ignoreMapping:       ignoreMapping,
	}
}

func (handler handler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	// check whether to protect this URL
	if handler.ignoreMapping.MatchString(req.URL.Path) {
		handler.delegate.ServeHTTP(w, req)
		return
	}
	token := req.URL.Query().Get("token")
	if token == "" {
		header := req.Header.Get("Authorization")
		if header != "" && strings.HasPrefix(strings.ToLower(header), BearerPrefix) {
			token = header[len(BearerPrefix):]
		}
	}
	if err := authenticate(token); err == nil {
		handler.delegate.ServeHTTP(w, req)
	} else {
		handler.unauthorizedHandler(w, req, err)
	}
}

func (handler cachingHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	// check whether to protect this URL
	if handler.ignoreMapping.MatchString(req.URL.Path) {
		handler.delegate.ServeHTTP(w, req)
		return
	}
	token := req.URL.Query().Get("token")
	if token == "" {
		header := req.Header.Get("Authorization")
		if header != "" && strings.HasPrefix(strings.ToLower(header), BearerPrefix) {
			token = header[len(BearerPrefix):]
		}
	}
	if handler.cache.Contains(token) {
		handler.delegate.ServeHTTP(w, req)
	} else if err := authenticate(token); err == nil {
		handler.cache.Put(token)
		handler.delegate.ServeHTTP(w, req)
	} else {
		handler.unauthorizedHandler(w, req, err)
	}
}

func authenticate(token string) error {
	if token == "" {
		return errors.New("Authentication failed because: missing authentication token in 'Authorization' header or 'token' query param")
	}

	claims := &JWTClaims{}
	jwt, err := jwt.ParseWithClaims(token, claims, rsaKeyFunc)
	if err != nil {
		return errors.New("Authentication failed. " + err.Error())
	}

	kind := jwt.Header["kind"].(string)
	if TokenKind != kind || WorkspaceId != claims.WorkspaceId {
		// signature is ok, but the token kind or workspace identifier is invalid
		return fmt.Errorf("Authentication failed, due to kind: '%v' or workspace id: '%v' is wrong", kind, WorkspaceId)
	}
	return nil
}

// Supplies RSA public key for verification of given token,
// when token 'alg' header is different or any problem occurs while retrieving key then error will be returned
func rsaKeyFunc(token *jwt.Token) (interface{}, error) {
	if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
		return nil, fmt.Errorf("token with unsupported signing method '%v' provided", token.Header["alg"])
	}
	key, err := KeyProvider.GetKey()
	if err != nil {
		return nil, err
	}
	return key, nil
}

func defaultUnauthorizedHandler(w http.ResponseWriter, r *http.Request, err error) {
	http.Error(w, err.Error(), http.StatusUnauthorized)
}
