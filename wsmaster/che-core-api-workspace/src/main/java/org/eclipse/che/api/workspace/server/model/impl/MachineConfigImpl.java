/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
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
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.config.Volume;

/** @author Alexander Garagatyi */
@Entity(name = "ExternalMachine")
@Table(name = "externalmachine")
public class MachineConfigImpl implements MachineConfig {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "externalmachine_attributes",
      joinColumns = @JoinColumn(name = "externalmachine_id"))
  @MapKeyColumn(name = "attributes_key")
  @Column(name = "attributes")
  private Map<String, String> attributes;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "externalmachine_env",
      joinColumns = @JoinColumn(name = "externalmachine_id"))
  @MapKeyColumn(name = "env_key")
  @Column(name = "env_value")
  private Map<String, String> env;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "servers_id")
  @MapKeyColumn(name = "servers_key")
  private Map<String, ServerConfigImpl> servers;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "machine_id")
  @MapKeyColumn(name = "name")
  private Map<String, VolumeImpl> volumes;

  public MachineConfigImpl() {}

  public MachineConfigImpl(
      Map<String, ? extends ServerConfig> servers,
      Map<String, String> env,
      Map<String, String> attributes,
      Map<String, ? extends Volume> volumes) {
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
    this(machine.getServers(), machine.getEnv(), machine.getAttributes(), machine.getVolumes());
  }

  @Override
  public Map<String, ServerConfigImpl> getServers() {
    if (servers == null) {
      servers = new HashMap<>();
    }
    return servers;
  }

  public void setServers(Map<String, ServerConfigImpl> servers) {
    this.servers = servers;
  }

  public MachineConfigImpl withServers(Map<String, ServerConfigImpl> servers) {
    this.servers = servers;
    return this;
  }

  @Override
  public Map<String, String> getEnv() {
    if (env == null) {
      env = new HashMap<>();
    }
    return env;
  }

  public void setEnv(Map<String, String> env) {
    this.env = env;
  }

  public MachineConfigImpl withEnv(Map<String, String> env) {
    this.env = env;
    return this;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public MachineConfigImpl withAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  @Override
  public Map<String, VolumeImpl> getVolumes() {
    if (volumes == null) {
      volumes = new HashMap<>();
    }
    return volumes;
  }

  public void setVolumes(Map<String, VolumeImpl> volumes) {
    this.volumes = volumes;
  }

  public MachineConfigImpl withVolumes(Map<String, VolumeImpl> volumes) {
    this.volumes = volumes;
    return this;
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
    return Objects.equals(id, that.id)
        && getEnv().equals(that.getEnv())
        && getAttributes().equals(that.getAttributes())
        && getServers().equals(that.getServers())
        && getVolumes().equals(that.getVolumes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + getEnv().hashCode();
    hash = 31 * hash + getAttributes().hashCode();
    hash = 31 * hash + getServers().hashCode();
    hash = 31 * hash + getVolumes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "MachineConfigImpl{"
        + "id="
        + id
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
