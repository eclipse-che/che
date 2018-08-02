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
	"testing"
	"time"
)

func TestTokenCache(t *testing.T) {
	cache := &DefaultTokenCache{
		tokens:        make(map[string]time.Time),
		expireTimeout: 0,
	}

	token := "my-token"

	cache.Put(token)
	if !cache.Contains(token) {
		t.Fatalf("Cache must contain token %s", token)
	}

	cache.Expire(token)
	if cache.Contains(token) {
		t.Fatalf("Cache must not contain token %s", token)
	}
}

func TestExpiresTokensCreatedBeforeGivenPointOfTime(t *testing.T) {
	cache := &DefaultTokenCache{
		tokens:        make(map[string]time.Time),
		expireTimeout: 0,
	}

	cache.Put("token1")
	afterToken1Put := time.Now()
	cache.Put("token2")

	cache.expireAllBefore(afterToken1Put)

	if cache.Contains("token1") {
		t.Fatal("Cache must not contain token1")
	}
	if !cache.Contains("token2") {
		t.Fatal("Cache must contain token2")
	}
}
