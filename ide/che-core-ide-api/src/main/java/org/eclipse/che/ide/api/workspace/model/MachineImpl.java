/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.workspace.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;

/** Data object for {@link Machine}. */
public class MachineImpl implements Machine {

  private String name;
  private Map<String, String> attributes;
  private Map<String, ServerImpl> servers;

  public MachineImpl(
      String name, Map<String, String> attributes, Map<String, ? extends Server> servers) {
    this.name = name;
    this.attributes = new HashMap<>(attributes);
    if (servers != null) {
      this.servers =
          servers
              .entrySet()
              .stream()
              .collect(
                  HashMap::new,
                  (map, entry) ->
                      map.put(entry.getKey(), new ServerImpl(entry.getKey(), entry.getValue())),
                  HashMap::putAll);
    }
  }

  public MachineImpl(String name, Machine machine) {
    this(name, machine.getAttributes(), machine.getServers());
  }

  public String getName() {
    return name;
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

  public Optional<ServerImpl> getServerByName(String name) {
    return Optional.ofNullable(getServers().get(name));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MachineImpl)) return false;
    MachineImpl that = (MachineImpl) o;
    return Objects.equals(getAttributes(), that.getAttributes())
        && Objects.equals(getServers(), that.getServers());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAttributes(), getServers());
  }

  @Override
  public String toString() {
    return "MachineImpl{" + "attributes=" + attributes + ", servers=" + servers + '}';
  }
}
