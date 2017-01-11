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
)

type ApiError struct {
	error
	Code int
}

func BadRequest(err error) error {
	return ApiError{err, http.StatusBadRequest}
}

func NotFound(err error) error {
	return ApiError{err, http.StatusNotFound}
}

func Conflict(err error) error {
	return ApiError{err, http.StatusConflict}
}

func Forbidden(err error) error {
	return ApiError{err, http.StatusForbidden}
}

func Unauthorized(err error) error {
	return ApiError{err, http.StatusUnauthorized}
}
