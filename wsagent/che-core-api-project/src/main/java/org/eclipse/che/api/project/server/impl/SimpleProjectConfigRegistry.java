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
package org.eclipse.che.api.project.server.impl;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeResolver;

@Singleton
public class SimpleProjectConfigRegistry
    implements org.eclipse.che.api.project.server.api.ProjectConfigRegistry {

  private final Map<String, RegisteredProject> projects = new ConcurrentHashMap<>();

  private final ProjectTypeRegistry projectTypeRegistry;
  private final ProjectTypeResolver projectTypeResolver;

  @Inject
  public SimpleProjectConfigRegistry(
      ProjectTypeRegistry projectTypeRegistry, ProjectTypeResolver projectTypeResolver) {
    this.projectTypeRegistry = projectTypeRegistry;
    this.projectTypeResolver = projectTypeResolver;
  }

  @Override
  public Set<RegisteredProject> getAll() {
    return ImmutableSet.copyOf(projects.values());
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {
    Set<RegisteredProject> children =
        projects
            .entrySet()
            .stream()
            .filter(it -> it.getKey().startsWith(wsPath))
            .map(Entry::getValue)
            .collect(toSet());
    return ImmutableSet.copyOf(children);
  }

  @Override
  public Optional<RegisteredProject> get(String wsPath) {
    return Optional.ofNullable(projects.get(wsPath));
  }

  @Override
  public RegisteredProject getOrNull(String wsPath) {
    return projects.get(wsPath);
  }

  @Override
  public RegisteredProject put(ProjectConfig config, boolean updated, boolean detected)
      throws ServerException {
    String wsPath = config.getPath();
    RegisteredProject project =
        new RegisteredProject(
            wsPath, config, updated, detected, projectTypeResolver, projectTypeRegistry);
    projects.put(wsPath, project);
    return project;
  }

  @Override
  public RegisteredProject put(String wsPath, boolean updated, boolean detected)
      throws ServerException {
    RegisteredProject project =
        new RegisteredProject(
            wsPath, null, updated, detected, projectTypeResolver, projectTypeRegistry);
    projects.put(wsPath, project);
    return project;
  }

  @Override
  public Optional<RegisteredProject> remove(String wsPath) {
    return Optional.ofNullable(projects.remove(wsPath));
  }

  @Override
  public RegisteredProject removeOrNull(String wsPath) {
    return projects.remove(wsPath);
  }

  @Override
  public boolean isRegistered(String path) {
    return projects.containsKey(path);
  }
}
