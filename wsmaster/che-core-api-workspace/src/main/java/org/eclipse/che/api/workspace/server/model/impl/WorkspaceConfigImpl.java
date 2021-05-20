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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
import javax.persistence.Transient;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Data object for {@link WorkspaceConfig}.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Entity(name = "WorkspaceConfig")
@Table(name = "workspaceconfig")
public class WorkspaceConfigImpl implements WorkspaceConfig {

  public static WorkspaceConfigImplBuilder builder() {
    return new WorkspaceConfigImplBuilder();
  }

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "defaultenv", nullable = true)
  private String defaultEnv;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "commands_id")
  private List<CommandImpl> commands;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "projects_id")
  private List<ProjectConfigImpl> projects;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "environments_id")
  @MapKeyColumn(name = "environments_key")
  private Map<String, EnvironmentImpl> environments;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "che_workspace_cfg_attributes",
      joinColumns = @JoinColumn(name = "workspace_id"))
  @MapKeyColumn(name = "attributes_key")
  @Column(name = "attributes")
  private Map<String, String> attributes;

  // we do not store converted workspace configs,
  // so it's not needed to store devfile from which this workspace config is generated
  @Transient private DevfileImpl devfile;

  public WorkspaceConfigImpl() {}

  public WorkspaceConfigImpl(
      String name,
      String description,
      String defaultEnv,
      List<? extends Command> commands,
      List<? extends ProjectConfig> projects,
      Map<String, ? extends Environment> environments,
      Map<String, String> attributes) {
    this(name, description, defaultEnv, commands, projects, environments, attributes, null);
  }

  public WorkspaceConfigImpl(
      String name,
      String description,
      String defaultEnv,
      List<? extends Command> commands,
      List<? extends ProjectConfig> projects,
      Map<String, ? extends Environment> environments,
      Map<String, String> attributes,
      Devfile devfile) {
    this.name = name;
    this.defaultEnv = defaultEnv;
    this.description = description;
    if (environments != null) {
      this.environments =
          environments
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, entry -> new EnvironmentImpl(entry.getValue())));
    }
    if (commands != null) {
      this.commands = commands.stream().map(CommandImpl::new).collect(toList());
    }
    if (projects != null) {
      this.projects = projects.stream().map(ProjectConfigImpl::new).collect(toList());
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
    if (devfile != null) {
      this.devfile = new DevfileImpl(devfile);
    }
  }

  public WorkspaceConfigImpl(WorkspaceConfig workspaceConfig) {
    this(
        workspaceConfig.getName(),
        workspaceConfig.getDescription(),
        workspaceConfig.getDefaultEnv(),
        workspaceConfig.getCommands(),
        workspaceConfig.getProjects(),
        workspaceConfig.getEnvironments(),
        workspaceConfig.getAttributes(),
        workspaceConfig.getDevfile());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = requireNonNull(name, "Non-null name required");
  }

  @Override
  @Nullable
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getDefaultEnv() {
    return defaultEnv;
  }

  public void setDefaultEnv(String defaultEnv) {
    this.defaultEnv = defaultEnv;
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
  public List<ProjectConfigImpl> getProjects() {
    if (projects == null) {
      projects = new ArrayList<>();
    }
    return projects;
  }

  public void setProjects(List<ProjectConfigImpl> projects) {
    this.projects = projects;
  }

  @Override
  public Map<String, EnvironmentImpl> getEnvironments() {
    if (environments == null) {
      environments = new HashMap<>();
    }
    return environments;
  }

  public void setEnvironments(Map<String, EnvironmentImpl> environments) {
    this.environments = environments;
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

  public WorkspaceConfigImpl setDevfile(DevfileImpl devfile) {
    this.devfile = devfile;
    return this;
  }

  @Nullable
  @Override
  public DevfileImpl getDevfile() {
    return devfile;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WorkspaceConfigImpl)) {
      return false;
    }
    final WorkspaceConfigImpl that = (WorkspaceConfigImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(defaultEnv, that.defaultEnv)
        && Objects.equals(devfile, that.devfile)
        && getCommands().equals(that.getCommands())
        && getProjects().equals(that.getProjects())
        && getEnvironments().equals(that.getEnvironments())
        && getAttributes().equals(that.getAttributes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(description);
    hash = 31 * hash + Objects.hashCode(defaultEnv);
    hash = 31 * hash + Objects.hashCode(devfile);
    hash = 31 * hash + getCommands().hashCode();
    hash = 31 * hash + getProjects().hashCode();
    hash = 31 * hash + getEnvironments().hashCode();
    hash = 31 * hash + getAttributes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "WorkspaceConfigImpl{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", defaultEnv='"
        + defaultEnv
        + '\''
        + ", commands="
        + commands
        + ", projects="
        + projects
        + ", environments="
        + environments
        + ", attributes="
        + attributes
        + ", devfile="
        + devfile
        + '}';
  }

  /**
   * Helps to build complex {@link WorkspaceConfigImpl users workspace instance}.
   *
   * @see WorkspaceConfigImpl#builder()
   */
  public static class WorkspaceConfigImplBuilder {

    private String name;
    private String defaultEnvName;
    private String description;
    private List<? extends Command> commands;
    private List<? extends ProjectConfig> projects;
    private Map<String, ? extends Environment> environments;
    private Map<String, String> attributes;

    private WorkspaceConfigImplBuilder() {}

    public WorkspaceConfigImpl build() {
      return new WorkspaceConfigImpl(
          name, description, defaultEnvName, commands, projects, environments, attributes);
    }

    public WorkspaceConfigImplBuilder fromConfig(WorkspaceConfig workspaceConfig) {
      this.name = workspaceConfig.getName();
      this.description = workspaceConfig.getDescription();
      this.defaultEnvName = workspaceConfig.getDefaultEnv();
      this.projects = workspaceConfig.getProjects();
      this.commands = workspaceConfig.getCommands();
      this.environments = workspaceConfig.getEnvironments();
      return this;
    }

    public WorkspaceConfigImplBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public WorkspaceConfigImplBuilder setDefaultEnv(String defaultEnvName) {
      this.defaultEnvName = defaultEnvName;
      return this;
    }

    public WorkspaceConfigImplBuilder setCommands(List<? extends Command> commands) {
      this.commands = commands;
      return this;
    }

    public WorkspaceConfigImplBuilder setProjects(List<? extends ProjectConfig> projects) {
      this.projects = projects;
      return this;
    }

    public WorkspaceConfigImplBuilder setEnvironments(
        Map<String, ? extends Environment> environments) {
      this.environments = environments;
      return this;
    }

    public WorkspaceConfigImplBuilder setDescription(String description) {
      this.description = description;
      return this;
    }

    public WorkspaceConfigImplBuilder setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
      return this;
    }
  }
}
