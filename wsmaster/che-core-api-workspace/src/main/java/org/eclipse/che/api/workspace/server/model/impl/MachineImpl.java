/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;

/**
 * Data object for {@link Machine}.
 *
 * @author Alexander Garagatyi
 */
public class MachineImpl implements Machine {

  private Map<String, String> attributes;
  private Map<String, ServerImpl> servers;
  private MachineStatus status;

  public MachineImpl(Machine machineRuntime) {
    this(machineRuntime.getAttributes(), machineRuntime.getServers(), machineRuntime.getStatus());
  }

  public MachineImpl(
      Map<String, String> attributes, Map<String, ? extends Server> servers, MachineStatus status) {
    this(servers);
    this.attributes = new HashMap<>(attributes);
    this.status = status;
  }

  public MachineImpl(Map<String, ? extends Server> servers) {
    if (servers != null) {
      this.servers =
          servers
              .entrySet()
              .stream()
              .collect(
                  HashMap::new,
                  (map, entry) -> map.put(entry.getKey(), new ServerImpl(entry.getValue())),
                  HashMap::putAll);
    }
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  @Override
  public Map<String, ServerImpl> getServers() {
    if (servers == null) {
      servers = new HashMap<>();
    }
    return servers;
  }

  @Override
  public MachineStatus getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MachineImpl)) {
      return false;
    }
    MachineImpl machine = (MachineImpl) o;
    return Objects.equals(getAttributes(), machine.getAttributes())
        && Objects.equals(getServers(), machine.getServers())
        && getStatus() == machine.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAttributes(), getServers(), getStatus());
  }

  @Override
  public String toString() {
    return "MachineImpl{"
        + "attributes="
        + attributes
        + ", servers="
        + servers
        + ", status="
        + status
        + '}';
  }
}
