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

package auth

import (
	"sync"
	"time"
)

const (
	// DefaultTokensExpirationTimeoutInMinutes provides default value for expiration timeout of token cache
	// in default implementation of token cache
	DefaultTokensExpirationTimeoutInMinutes = 10
)

// DefaultTokenCache represents default authentication tokens cache.
type DefaultTokenCache struct {
	sync.RWMutex
	tokens        map[string]time.Time
	ticker        *time.Ticker
	expireTimeout time.Duration
}

// NewCache creates TokenCache.
func NewCache(expireDuration time.Duration, period time.Duration) *DefaultTokenCache {
	cache := &DefaultTokenCache{
		tokens:        make(map[string]time.Time),
		expireTimeout: expireDuration,
	}
	if period > 0 {
		go cache.expirePeriodically(period)
	}
	return cache
}

// Put adds token into the cache.
func (cache *DefaultTokenCache) Put(token string) {
	cache.Lock()
	defer cache.Unlock()
	cache.tokens[token] = time.Now().Add(cache.expireTimeout)
}

// Expire removes provided token from the cache.
func (cache *DefaultTokenCache) Expire(token string) {
	cache.Lock()
	defer cache.Unlock()
	delete(cache.tokens, token)
}

// Contains returns true if token is present in the cache and false otherwise.
func (cache *DefaultTokenCache) Contains(token string) bool {
	cache.RLock()
	defer cache.RUnlock()
	_, ok := cache.tokens[token]
	return ok
}

func (cache *DefaultTokenCache) expirePeriodically(period time.Duration) {
	cache.ticker = time.NewTicker(period)
	for range cache.ticker.C {
		cache.expireAllBefore(time.Now())
	}
}

func (cache *DefaultTokenCache) expireAllBefore(expirationPoint time.Time) {
	cache.Lock()
	defer cache.Unlock()
	for token, expTime := range cache.tokens {
		if expTime.Before(expirationPoint) {
			delete(cache.tokens, token)
		}
	}
}
