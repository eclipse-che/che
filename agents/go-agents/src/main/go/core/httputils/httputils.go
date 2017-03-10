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

package httputils

import (
	"net/http"
	"regexp"
)

// BasePathChopper cuts base path of a request
type BasePathChopper struct {
	Pattern  *regexp.Regexp
	Delegate http.Handler
}

func (c BasePathChopper) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	// if Pattern is prefix of request path remove the prefix
	if idx := c.Pattern.FindStringSubmatchIndex(r.URL.Path); len(idx) != 0 && idx[0] == 0 {
		r.URL.Path = r.URL.Path[idx[1]:]
		r.RequestURI = r.RequestURI[idx[1]:]
	}
	c.Delegate.ServeHTTP(w, r)
}
