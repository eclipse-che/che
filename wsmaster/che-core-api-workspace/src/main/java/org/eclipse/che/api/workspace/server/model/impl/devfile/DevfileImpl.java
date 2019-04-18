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
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.Project;

/** @author Sergii Leshchenko */
@Entity(name = "Devfile")
@Table(name = "devfile")
public class DevfileImpl implements Devfile {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "spec_version", nullable = false)
  private String specVersion;

  @Column(name = "name", nullable = false)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "devfile_id")
  private List<ProjectImpl> projects;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "devfile_id")
  private List<ComponentImpl> components;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "devfile_id")
  private List<CommandImpl> commands;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "devfile_attributes", joinColumns = @JoinColumn(name = "devfile_id"))
  @MapKeyColumn(name = "name")
  @Column(name = "value", columnDefinition = "TEXT")
  private Map<String, String> attributes;

  public DevfileImpl() {}

  public DevfileImpl(
      String specVersion,
      String name,
      List<? extends Project> projects,
      List<? extends Component> components,
      List<? extends Command> commands,
      Map<String, String> attributes) {
    this.specVersion = specVersion;
    this.name = name;
    if (projects != null) {
      this.projects = projects.stream().map(ProjectImpl::new).collect(toCollection(ArrayList::new));
    }
    if (components != null) {
      this.components =
          components.stream().map(ComponentImpl::new).collect(toCollection(ArrayList::new));
    }
    if (commands != null) {
      this.commands = commands.stream().map(CommandImpl::new).collect(toCollection(ArrayList::new));
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
  }

  public DevfileImpl(Devfile devfile) {
    this(
        devfile.getSpecVersion(),
        devfile.getName(),
        devfile.getProjects(),
        devfile.getComponents(),
        devfile.getCommands(),
        devfile.getAttributes());
  }

  @Override
  public String getSpecVersion() {
    return specVersion;
  }

  public void setSpecVersion(String specVersion) {
    this.specVersion = specVersion;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public List<ProjectImpl> getProjects() {
    if (projects == null) {
      projects = new ArrayList<>();
    }
    return projects;
  }

  public void setProjects(List<ProjectImpl> projects) {
    this.projects = projects;
  }

  @Override
  public List<ComponentImpl> getComponents() {
    if (components == null) {
      components = new ArrayList<>();
    }
    return components;
  }

  public void setComponents(List<ComponentImpl> components) {
    this.components = components;
  }

  @Override
  public List<CommandImpl> getCommands() {
    if (commands == null) {
      commands = new ArrayList<>();
    }
    return commands;
  }

  public void setCommands(List<CommandImpl> commands) {
    this.commands = commands;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DevfileImpl)) {
      return false;
    }
    DevfileImpl devfile = (DevfileImpl) o;
    return Objects.equals(id, devfile.id)
        && Objects.equals(specVersion, devfile.specVersion)
        && Objects.equals(name, devfile.name)
        && Objects.equals(getProjects(), devfile.getProjects())
        && Objects.equals(getComponents(), devfile.getComponents())
        && Objects.equals(getCommands(), devfile.getCommands())
        && Objects.equals(getAttributes(), devfile.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, specVersion, name, getProjects(), getComponents(), getCommands(), getAttributes());
  }

  @Override
  public String toString() {
    return "DevfileImpl{"
        + "id='"
        + id
        + '\''
        + ", specVersion='"
        + specVersion
        + '\''
        + ", name='"
        + name
        + '\''
        + ", projects="
        + projects
        + ", components="
        + components
        + ", commands="
        + commands
        + ", attributes="
        + attributes
        + '}';
  }
}
