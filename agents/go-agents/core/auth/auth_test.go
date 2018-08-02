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
	"crypto/x509"
	"encoding/base64"
	"strings"
	"testing"
)

const (
	Token                    = "eyJraW5kIjoibWFjaGluZV90b2tlbiIsImFsZyI6IlJTNTEyIn0.eyJ3c2lkIjoid29ya3NwYWNla3JoOTl4amVuZWs3amN5ZSIsInVpZCI6InRlc3RfdXNlcjMxIiwidW5hbWUiOiJ0ZXN0X3VzZXIiLCJqdGkiOiI4MzEyLTIxMy0xd2UzMSJ9.TX3ZyWgIyFmOPe8yHQWGXNOwmuAxp1lIZEdzXAmhf6nnUKLHV5QLV2qNI1iuDta0fLXtB_Wwf6Mpqkx8J1ighNhgUfbro7OujIlAezx3sEBHI-Ntzb1JHsbr_xKxrzhEVT7NuvkJuB1RlFEpZmRt0Sf86UOoKZSsYypuCOA1vZU"
	EncodedPublicKey         = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC85cPsQ8FLvvI6NY5DGiq2YIdAlq2MkNcGgrQggQqTwTyQ7MgPYO7o4BYc5BjOljQ93g/vzyN9Ku9YahHjQTT9X3IdZSk7+UINvKF/3gPjprHYQ2t1PswNNCViqwEN1nktvbugJjI3FYnr5eiBxkV+7wwggxEILGfKL67I+HnA0QIDAQAB"
	TestWorkspaceId          = "workspacekrh99xjenek7jcye"
	TokenWithDifferentSigAlg = "eyJraW5kIjoibWFjaGluZV90b2tlbiIsImFsZyI6IkVTNTEyIn0.eyJ3c2lkIjoid29ya3NwYWNla3JoOTl4amVuZWs3amN5ZSIsInVpZCI6InRlc3RfdXNlcjMxIiwidW5hbWUiOiJ0ZXN0X3VzZXIiLCJqdGkiOiI4MzEyLTIxMy0xd2UzMSJ9.ATHOy78MXfi9e2MzS_GENXwI7wddmdeEzcMKQfKCgw78n-_YoI0vWowNS-AOMiJKeBtNlgTMRnsY_H1lsK8QxRZMOFg-KaRwAQkZTJBB1D107KPNbI-FYBORhMu0PNV2-j9_DwIUiv_lek9bmE02ibY28VzPOhl-V9iTYWecsgzAsC0ACzyRbZaZIkRShlN7"
)

type ConstSignKeyProvider struct{}

func (enk ConstSignKeyProvider) GetKey() (interface{}, error) {
	bytes, _ := base64.StdEncoding.DecodeString(EncodedPublicKey)
	key, _ := x509.ParsePKIXPublicKey(bytes)
	return key, nil
}

func TestAuthenticateWithValidRSAToken(t *testing.T) {
	WorkspaceId = TestWorkspaceId
	KeyProvider = &ConstSignKeyProvider{}
	if err := authenticate(Token); err != nil {
		t.Fatal(err)
	}
}

func TestAuthenticationFailedWhenNoWorkspaceIdInEnv(t *testing.T) {
	WorkspaceId = ""
	KeyProvider = &ConstSignKeyProvider{}
	err := authenticate(Token)
	if err == nil {
		t.Fatal("Expecting error when no workspace id in an environment")
	}
	expectedError := "Authentication failed, due to kind: 'machine_token' or workspace id: '' is wrong"
	if err.Error() != expectedError {
		t.Fatalf("Expecting error message: '%v'", expectedError)
	}
}

func TestAuthenticationFailedWhenFailedToGetKey(t *testing.T) {
	WorkspaceId = TestWorkspaceId
	KeyProvider = &EnvSignKeyProvider{}
	err := authenticate(Token)
	if err == nil {
		t.Fatal("Expecting error when no signature key in environment")
	}
	errorPrefix := "Authentication failed. Failed to parse public key cause:"
	if !strings.HasPrefix(err.Error(), errorPrefix) {
		t.Fatalf("Expecting error prefix: '%v'", errorPrefix)
	}
}

func TestAuthenticationFailedWhenEmptyTokenProvided(t *testing.T) {
	WorkspaceId = TestWorkspaceId
	KeyProvider = &ConstSignKeyProvider{}
	err := authenticate("")
	if err == nil {
		t.Fatal("Expecting error when no empty token provided")
	}
	expectedError := "Authentication failed because: missing authentication token in 'Authorization' header or 'token' query param"
	if err.Error() != expectedError {
		t.Fatalf("Expecting error message: '%v'", expectedError)
	}
}

func TestAuthenticationFailedWhenTokenWithInvalidSignatureProvided(t *testing.T) {
	WorkspaceId = TestWorkspaceId
	KeyProvider = &ConstSignKeyProvider{}
	err := authenticate("invalid_token")
	if err == nil {
		t.Fatal("Expecting error when no signature key in environment")
	}
	errorPrefix := "Authentication failed."
	if !strings.HasPrefix(err.Error(), errorPrefix) {
		t.Fatalf("Expecting error prefix: '%v'", errorPrefix)
	}
}

func TestAuthenticationFailedWhenTokenSignedWithDifferentAlgorithmProvided(t *testing.T) {
	WorkspaceId = TestWorkspaceId
	KeyProvider = &ConstSignKeyProvider{}
	err := authenticate(TokenWithDifferentSigAlg)
	if err == nil {
		t.Fatal("Expecting error when token with different signature algorithm provided")
	}
	expectedError := "Authentication failed. token with unsupported signing method 'ES512' provided"
	if err.Error() != expectedError {
		t.Fatalf("Expecting error message: '%v'", expectedError)
	}
}
