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
