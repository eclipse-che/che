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

package rest

import (
	"log"
	"net/http"
)

// Router provides http requests routing capabilities
type Router interface {
	http.Handler
}

// PrintRoutes prints description of routes in provided slice of groups
func PrintRoutes(restRoutes []RoutesGroup) {
	log.Print("⇩ Registered HTTPRoutes:\n\n")
	for _, routesGroup := range restRoutes {
		log.Printf("%s:\n", routesGroup.Name)
		for _, route := range routesGroup.Items {
			log.Printf("✓ %s\n", &route)
		}
		log.Println()
	}
}
