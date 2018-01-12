/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.workspace.model;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandImpl;

/** Data object for {@link WorkspaceConfig}. */
public class WorkspaceConfigImpl implements WorkspaceConfig {

  private String name;
  private String description;
  private String defaultEnv;
  private List<CommandImpl> commands;
  private List<ProjectConfigImpl> projects;
  private Map<String, EnvironmentImpl> environments;

  public WorkspaceConfigImpl(
      String name,
      String description,
      String defaultEnv,
      List<? extends Command> commands,
      List<? extends ProjectConfig> projects,
      Map<String, ? extends Environment> environments) {
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
  }

  public WorkspaceConfigImpl(WorkspaceConfig workspaceConfig) {
    this(
        workspaceConfig.getName(),
        workspaceConfig.getDescription(),
        workspaceConfig.getDefaultEnv(),
        workspaceConfig.getCommands(),
        workspaceConfig.getProjects(),
        workspaceConfig.getEnvironments());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  @Nullable
  public String getDescription() {
    return description;
  }

  @Override
  public String getDefaultEnv() {
    return defaultEnv;
  }

  @Override
  public List<CommandImpl> getCommands() {
    if (commands == null) {
      commands = new ArrayList<>();
    }
    return commands;
  }

  @Override
  public List<ProjectConfigImpl> getProjects() {
    if (projects == null) {
      projects = new ArrayList<>();
    }
    return projects;
  }

  @Override
  public Map<String, EnvironmentImpl> getEnvironments() {
    if (environments == null) {
      return new HashMap<>();
    }
    return environments;
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
    return Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(defaultEnv, that.defaultEnv)
        && getCommands().equals(that.getCommands())
        && getProjects().equals(that.getProjects())
        && getEnvironments().equals(that.getEnvironments());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(description);
    hash = 31 * hash + Objects.hashCode(defaultEnv);
    hash = 31 * hash + getCommands().hashCode();
    hash = 31 * hash + getProjects().hashCode();
    hash = 31 * hash + getEnvironments().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "WorkspaceConfigImpl{"
        + "name='"
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
        + '}';
  }
}
