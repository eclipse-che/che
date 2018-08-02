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

package booter

// Installer defines installation configuration which usually
// brings something new to the machine e.g. starts server or installs tools like 'curl'.
type Installer struct {

	// ID the identifier of the installer e.g. 'org.eclipse.che.exec'.
	ID string `json:"id"`

	// Description the description of the installer.
	Description string `json:"description"`

	// Version the version of the installer.
	Version string `json:"version"`

	// Script is content of installation script.
	Script string `json:"script"`

	// Servers map of ref -> server.
	Servers map[string]Server `json:"servers"`
}

// HasServers returns true if installer has at least one server.
func (installer Installer) HasServers() bool { return len(installer.Servers) != 0 }

// Server represents set of configuration that can be
type Server struct {

	// Server port in form 'port/protocol' or 'port' if protocol is tcp.
	Port string `json:"port"`

	// Protocol the protocol for configuring preview url of the server e.g. 'http'.
	Protocol string `json:"protocol"`
}
