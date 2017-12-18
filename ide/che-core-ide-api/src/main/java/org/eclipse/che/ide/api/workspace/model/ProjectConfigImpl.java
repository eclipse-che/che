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

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.project.ProjectProblem;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;

/** Data object for {@link ProjectConfig}. */
public class ProjectConfigImpl implements ProjectConfig {

  private String path;
  private String name;
  private String type;
  private String description;
  private SourceStorageImpl source;
  private List<String> mixins;
  private Map<String, List<String>> attributes;
  private List<? extends ProjectProblem> problems;

  public ProjectConfigImpl(ProjectConfig projectConfig) {
    name = projectConfig.getName();
    path = projectConfig.getPath();
    description = projectConfig.getDescription();
    type = projectConfig.getType();
    mixins = new ArrayList<>(projectConfig.getMixins());
    problems = new ArrayList<>(projectConfig.getProblems());
    attributes =
        projectConfig
            .getAttributes()
            .entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));

    SourceStorage sourceStorage = projectConfig.getSource();
    if (sourceStorage != null) {
      source =
          new SourceStorageImpl(
              sourceStorage.getType(), sourceStorage.getLocation(), sourceStorage.getParameters());
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public List<String> getMixins() {
    if (mixins == null) {
      mixins = new ArrayList<>();
    }
    return mixins;
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  @Override
  public SourceStorageImpl getSource() {
    return source;
  }

  @Override
  public List<? extends ProjectProblem> getProblems() {
    if (problems == null) {
      problems = new ArrayList<>();
    }
    return problems;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProjectConfigImpl)) {
      return false;
    }
    final ProjectConfigImpl that = (ProjectConfigImpl) obj;
    return Objects.equals(path, that.path)
        && Objects.equals(name, that.name)
        && Objects.equals(type, that.type)
        && Objects.equals(description, that.description)
        && Objects.equals(source, that.source)
        && getMixins().equals(that.getMixins())
        && getAttributes().equals(that.getAttributes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(path);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(type);
    hash = 31 * hash + Objects.hashCode(description);
    hash = 31 * hash + Objects.hashCode(source);
    hash = 31 * hash + getMixins().hashCode();
    hash = 31 * hash + getAttributes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "ProjectConfigImpl{"
        + "path='"
        + path
        + '\''
        + ", name='"
        + name
        + '\''
        + ", type='"
        + type
        + '\''
        + ", description='"
        + description
        + '\''
        + ", source="
        + source
        + ", mixins="
        + mixins
        + ", attributes="
        + attributes
        + ", problems="
        + problems
        + '}';
  }
}
