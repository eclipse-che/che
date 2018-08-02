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

package restutil

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/eclipse/che/agents/go-agents/core/rest"
)

// WriteJSON writes body as json to the response writer
func WriteJSON(w http.ResponseWriter, body interface{}) error {
	w.Header().Set("Content-Type", "application/json")
	return json.NewEncoder(w).Encode(body)
}

// ReadJSON reads JSON body from the request
func ReadJSON(r *http.Request, v interface{}) error {
	return json.NewDecoder(r.Body).Decode(v)
}

// IntQueryParam converts value of query parameter into int.
// If query parameter is not found or can't be converted into int defaultValue is returned.
func IntQueryParam(r *http.Request, name string, defaultValue int) int {
	qp := r.URL.Query().Get(name)
	if qp == "" {
		return defaultValue
	}
	v, err := strconv.Atoi(qp)
	if err != nil {
		return defaultValue
	}
	if v < 0 {
		return defaultValue
	}
	return v
}

// OKRespondingFunc responds 200 to the requests for a server liveness checks
func OKRespondingFunc(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	w.WriteHeader(http.StatusOK)
	return nil
}
