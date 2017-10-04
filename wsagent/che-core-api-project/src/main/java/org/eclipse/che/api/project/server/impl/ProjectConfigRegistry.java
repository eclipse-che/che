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

@Singleton
public class ProjectConfigRegistry {

  private final Map<String, RegisteredProject> projects = new ConcurrentHashMap<>();

  private final RegisteredProjectFactory registeredProjectFactory;

  @Inject
  public ProjectConfigRegistry(RegisteredProjectFactory registeredProjectFactory) {
    this.registeredProjectFactory = registeredProjectFactory;
  }

  public Set<RegisteredProject> getAll() {
    return ImmutableSet.copyOf(projects.values());
  }

  public Set<RegisteredProject> getAll(String wsPath) {
    Set<RegisteredProject> children =
        projects
            .entrySet()
            .stream()
            .filter(it -> it.getKey().startsWith(wsPath))
            .filter(it -> !it.getKey().equals(wsPath))
            .map(Entry::getValue)
            .collect(toSet());
    return ImmutableSet.copyOf(children);
  }

  public Optional<RegisteredProject> get(String wsPath) {
    return Optional.ofNullable(projects.get(wsPath));
  }

  public RegisteredProject getOrNull(String wsPath) {
    return projects.get(wsPath);
  }

  public RegisteredProject put(ProjectConfig config, boolean updated, boolean detected)
      throws ServerException {
    String wsPath = config.getPath();
    RegisteredProject project = registeredProjectFactory.create(wsPath, config, updated, detected);
    projects.put(wsPath, project);
    return project;
  }

  public RegisteredProject put(String wsPath, boolean updated, boolean detected)
      throws ServerException {
    RegisteredProject project = registeredProjectFactory.create(wsPath, null, updated, detected);
    projects.put(wsPath, project);
    return project;
  }

  public Optional<RegisteredProject> remove(String wsPath) {
    return Optional.ofNullable(projects.remove(wsPath));
  }

  public RegisteredProject removeOrNull(String wsPath) {
    return projects.remove(wsPath);
  }

  public boolean isRegistered(String path) {
    return projects.containsKey(path);
  }
}
