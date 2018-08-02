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
package org.eclipse.che.api.installer.server.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.shared.model.Installer;

/** @author Anatoliy Bazko */
@Entity(name = "Inst")
@NamedQueries({
  @NamedQuery(name = "Inst.getAll", query = "SELECT i FROM Inst i"),
  @NamedQuery(name = "Inst.getAllById", query = "SELECT i FROM Inst i WHERE i.id = :id"),
  @NamedQuery(
    name = "Inst.getByKey",
    query = "SELECT i FROM Inst i WHERE i.id = :id AND i.version = :version"
  ),
  @NamedQuery(name = "Inst.getTotalCount", query = "SELECT COUNT(i) FROM Inst i")
})
@Table(name = "installer")
public class InstallerImpl implements Installer {
  @Id
  @GeneratedValue
  @Column(name = "internal_id")
  private Long internalId;

  @Column(name = "id", nullable = false)
  private String id;

  @Column(name = "version", nullable = false)
  private String version;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "dependency", nullable = false)
  @CollectionTable(
    name = "installer_dependencies",
    joinColumns = {@JoinColumn(name = "inst_int_id", referencedColumnName = "internal_id")}
  )
  private List<String> dependencies;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "name")
  @Column(name = "value", nullable = false)
  @CollectionTable(
    name = "installer_properties",
    joinColumns = {@JoinColumn(name = "inst_int_id", referencedColumnName = "internal_id")}
  )
  private Map<String, String> properties;

  @Column(name = "script")
  private String script;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "inst_int_id", referencedColumnName = "internal_id")
  @MapKeyColumn(name = "server_key")
  private Map<String, InstallerServerConfigImpl> servers;

  public InstallerImpl() {}

  public InstallerImpl(
      String id,
      String name,
      String version,
      String description,
      List<String> dependencies,
      Map<String, String> properties,
      String script,
      Map<String, ? extends ServerConfig> servers) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.description = description;
    this.dependencies = dependencies;
    this.properties = properties;
    this.script = script;
    if (servers != null) {
      this.servers =
          servers
              .entrySet()
              .stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, e -> new InstallerServerConfigImpl(e.getValue())));
    }
  }

  public InstallerImpl(Installer installer) {
    this(
        installer.getId(),
        installer.getName(),
        installer.getVersion(),
        installer.getDescription(),
        installer.getDependencies(),
        installer.getProperties(),
        installer.getScript(),
        installer.getServers());
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public InstallerImpl withId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InstallerImpl withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public InstallerImpl withVersion(String version) {
    this.version = version;
    return this;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public InstallerImpl withDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public List<String> getDependencies() {
    if (dependencies == null) {
      dependencies = new ArrayList<>();
    }
    return dependencies;
  }

  public void setDependencies(List<String> dependencies) {
    this.dependencies = dependencies;
  }

  public InstallerImpl withDependencies(List<String> dependencies) {
    this.dependencies = dependencies;
    return this;
  }

  @Override
  public Map<String, String> getProperties() {
    if (properties == null) {
      properties = new HashMap<>();
    }
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public InstallerImpl withProperties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  @Override
  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public InstallerImpl withScript(String script) {
    this.script = script;
    return this;
  }

  @Override
  public Map<String, InstallerServerConfigImpl> getServers() {
    if (servers == null) {
      servers = new HashMap<>();
    }
    return servers;
  }

  public void setServers(Map<String, InstallerServerConfigImpl> servers) {
    this.servers = servers;
  }

  public InstallerImpl withServers(Map<String, InstallerServerConfigImpl> servers) {
    this.servers = servers;
    return this;
  }

  public Long getInternalId() {
    return internalId;
  }

  public void setInternalId(Long internalId) {
    this.internalId = internalId;
  }

  public InstallerImpl withInternalId(Long internalId) {
    this.internalId = internalId;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InstallerImpl)) return false;
    InstallerImpl installer = (InstallerImpl) o;
    return Objects.equals(getId(), installer.getId())
        && Objects.equals(getName(), installer.getName())
        && Objects.equals(getVersion(), installer.getVersion())
        && Objects.equals(getDescription(), installer.getDescription())
        && Objects.equals(getDependencies(), installer.getDependencies())
        && Objects.equals(getProperties(), installer.getProperties())
        && Objects.equals(getScript(), installer.getScript())
        && Objects.equals(getServers(), installer.getServers());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getId(),
        getName(),
        getVersion(),
        getDescription(),
        getDependencies(),
        getProperties(),
        getScript(),
        getServers());
  }

  @Override
  public String toString() {
    return "InstallerImpl{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", version='"
        + version
        + '\''
        + ", description='"
        + description
        + '\''
        + ", dependencies="
        + dependencies
        + ", properties="
        + properties
        + ", servers="
        + servers
        + '}';
  }
}
