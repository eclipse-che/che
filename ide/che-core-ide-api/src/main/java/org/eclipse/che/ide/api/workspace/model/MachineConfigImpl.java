/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.commons.annotation.Nullable;

public class MachineConfigImpl implements MachineConfig {

  private List<String> installers;
  private Map<String, String> env;
  private Map<String, String> attributes;
  private Map<String, ServerConfigImpl> servers;
  private Map<String, VolumeImpl> volumes;

  public MachineConfigImpl(
      List<String> installers,
      Map<String, ? extends ServerConfig> servers,
      Map<String, String> env,
      Map<String, String> attributes,
      Map<String, ? extends Volume> volumes) {
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
    if (env != null) {
      this.env = new HashMap<>(env);
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
    if (volumes != null) {
      this.volumes =
          volumes
              .entrySet()
              .stream()
              .collect(
                  Collectors.toMap(Map.Entry::getKey, entry -> new VolumeImpl(entry.getValue())));
    }
  }

  public MachineConfigImpl(MachineConfig machine) {
    this(
        machine.getInstallers(),
        machine.getServers(),
        machine.getEnv(),
        machine.getAttributes(),
        machine.getVolumes());
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
  public Map<String, String> getEnv() {
    if (env == null) {
      env = new HashMap<>();
    }
    return env;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  @Override
  public Map<String, VolumeImpl> getVolumes() {
    if (volumes == null) {
      volumes = new HashMap<>();
    }
    return volumes;
  }

  /**
   * @param name
   * @return volume by name or null if no such volume defined
   */
  @Nullable
  public VolumeImpl getVolume(String name) {
    return volumes.get(name);
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
        && getEnv().equals(that.getEnv())
        && getAttributes().equals(that.getAttributes())
        && getServers().equals(that.getServers())
        && getVolumes().equals(that.getVolumes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + getInstallers().hashCode();
    hash = 31 * hash + getEnv().hashCode();
    hash = 31 * hash + getAttributes().hashCode();
    hash = 31 * hash + getServers().hashCode();
    hash = 31 * hash + getVolumes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "MachineConfigImpl{"
        + "installers="
        + installers
        + ", env="
        + env
        + ", attributes="
        + attributes
        + ", servers="
        + servers
        + ", volumes="
        + volumes
        + '}';
  }
}
