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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;

public class MachineConfigImpl implements MachineConfig {

  private List<String> installers;
  private Map<String, String> attributes;
  private Map<String, ServerConfigImpl> servers;

  public MachineConfigImpl(
      List<String> installers,
      Map<String, ? extends ServerConfig> servers,
      Map<String, String> attributes) {
    if (installers != null) {
      this.installers = new ArrayList<>(installers);
    }
    if (servers != null) {
      this.servers =
          servers
              .entrySet()
              .stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, entry -> new ServerConfigImpl(entry.getValue())));
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
  }

  public MachineConfigImpl(MachineConfig machine) {
    this(machine.getInstallers(), machine.getServers(), machine.getAttributes());
  }

  @Override
  public List<String> getInstallers() {
    if (installers == null) {
      installers = new ArrayList<>();
    }
    return installers;
  }

  @Override
  public Map<String, ServerConfigImpl> getServers() {
    if (servers == null) {
      servers = new HashMap<>();
    }
    return servers;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MachineConfigImpl)) {
      return false;
    }
    final MachineConfigImpl that = (MachineConfigImpl) obj;
    return getInstallers().equals(that.getInstallers())
        && getAttributes().equals(that.getAttributes())
        && getServers().equals(that.getServers());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + getInstallers().hashCode();
    hash = 31 * hash + getAttributes().hashCode();
    hash = 31 * hash + getServers().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "MachineConfigImpl{"
        + "installers="
        + installers
        + ", attributes="
        + attributes
        + ", servers="
        + servers
        + '}';
  }
}
