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
	"errors"
	"os"
)

const SignatureKeyEnv = "CHE_MACHINE_AUTH_SIGNATURE__PUBLIC__KEY"

// SignKeyProvider provides signature key
type SignKeyProvider interface {
	GetKey() (interface{}, error)
}

// EnvSignKeyProvider provides public key retrieved from environment
type EnvSignKeyProvider struct {
	PublicKey interface{}
}

func (enk EnvSignKeyProvider) GetKey() (interface{}, error) {
	if enk.PublicKey == nil {
		// Key may contain symbols that are unacceptable for environment variables values, so it must be encoded in base64
		bytes, err := base64.StdEncoding.DecodeString(os.Getenv(SignatureKeyEnv))
		if err != nil {
			return nil, errors.New("Failed to encode public key cause: " + err.Error())
		}
		key, err := x509.ParsePKIXPublicKey(bytes)
		if err != nil {
			return nil, errors.New("Failed to parse public key cause: " + err.Error())
		}
		enk.PublicKey = key
	}
	return enk.PublicKey, nil
}
