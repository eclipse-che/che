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

package restutil

import (
	"encoding/json"
	"net/http"
	"strconv"
)

// Writes body as json to the response writer
func WriteJson(w http.ResponseWriter, body interface{}) error {
	w.Header().Set("Content-Type", "application/json")
	return json.NewEncoder(w).Encode(body)
}

// Reads json body from the request
func ReadJson(r *http.Request, v interface{}) error {
	return json.NewDecoder(r.Body).Decode(v)
}

func IntQueryParam(r *http.Request, name string, defaultValue int) int {
	qp := r.URL.Query().Get(name)
	if qp == "" {
		return defaultValue
	} else {
		v, err := strconv.Atoi(qp)
		if err != nil {
			return defaultValue
		}
		if v < 0 {
			return defaultValue
		}
		return v
	}
}
