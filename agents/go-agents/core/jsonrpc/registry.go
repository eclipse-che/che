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

package jsonrpc

import "sync"

// DefaultRegistry is package registry, which is used by NewManagedTunnel.
var DefaultRegistry = NewRegistry()

// Save saves a given tunnel is the default registry.
func Save(tun *Tunnel) {
	DefaultRegistry.Save(tun)
}

// Rm removes a given tunnel from the default registry.
func Rm(id string) (*Tunnel, bool) {
	return DefaultRegistry.Rm(id)
}

// GetTunnels gets tunnels managed by default registry.
func GetTunnels() []*Tunnel {
	return DefaultRegistry.GetTunnels()
}

// Get gets a single tunnel managed by default registry.
func Get(id string) (*Tunnel, bool) {
	return DefaultRegistry.Get(id)
}

// TunnelRegistry is a simple storage for tunnels.
type TunnelRegistry struct {
	sync.RWMutex
	tunnels map[string]*Tunnel
}

// NewRegistry creates a new registry.
func NewRegistry() *TunnelRegistry {
	return &TunnelRegistry{tunnels: make(map[string]*Tunnel)}
}

// Save saves a tunnel with a given id in this registry, overrides existing one.
func (reg *TunnelRegistry) Save(tunnel *Tunnel) {
	reg.Lock()
	defer reg.Unlock()
	reg.tunnels[tunnel.id] = tunnel
}

// Rm removes tunnel with given id from the registry.
func (reg *TunnelRegistry) Rm(id string) (*Tunnel, bool) {
	reg.Lock()
	defer reg.Unlock()
	tun, ok := reg.tunnels[id]
	if ok {
		delete(reg.tunnels, id)
	}
	return tun, ok
}

// GetTunnels returns all the tunnels which the registry keeps.
func (reg *TunnelRegistry) GetTunnels() []*Tunnel {
	reg.RLock()
	defer reg.RUnlock()
	tunnels := make([]*Tunnel, 0)
	for _, v := range reg.tunnels {
		tunnels = append(tunnels, v)
	}
	return tunnels
}

// Get returns tunnel with a given id.
func (reg *TunnelRegistry) Get(id string) (*Tunnel, bool) {
	reg.RLock()
	defer reg.RUnlock()
	tun, ok := reg.tunnels[id]
	return tun, ok
}
