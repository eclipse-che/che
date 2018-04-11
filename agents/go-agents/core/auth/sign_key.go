//
// Copyright (c) 2012-2018 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

package auth

import (
	"encoding/base64"
	"os"
	"errors"
	"crypto/x509"
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
