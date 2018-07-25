/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.SEPARATOR;
import static org.eclipse.che.api.fs.server.WsPathUtils.isRoot;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.eclipse.che.api.project.shared.RegisteredProject;

@Singleton
public class InmemoryProjectRegistry implements ProjectConfigRegistry {

  private final Map<String, RegisteredProject> projects = new ConcurrentHashMap<>();

  private final RegisteredProjectFactory registeredProjectFactory;

  @Inject
  public InmemoryProjectRegistry(RegisteredProjectFactory registeredProjectFactory) {
    this.registeredProjectFactory = registeredProjectFactory;
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
            .filter(it -> it.getKey().startsWith(wsPath + SEPARATOR))
            .filter(it -> !it.getKey().equals(wsPath))
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
  public synchronized RegisteredProject put(
      ProjectConfig config, boolean updated, boolean detected) {
    String wsPath = config.getPath();
    RegisteredProject project = registeredProjectFactory.create(wsPath, config, updated, detected);
    projects.put(wsPath, project);
    return project;
  }

  @Override
  public synchronized RegisteredProject putIfAbsent(
      String wsPath, boolean updated, boolean detected) {
    RegisteredProject registeredProject = projects.get(wsPath);
    if (registeredProject != null) {
      return registeredProject;
    }

    RegisteredProject project = registeredProjectFactory.create(wsPath, null, updated, detected);
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

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    while (!isRoot(wsPath)) {

      if (projects.containsKey(wsPath)) {
        return Optional.of(projects.get(wsPath));
      }
      wsPath = WsPathUtils.parentOf(wsPath);
    }

    return empty();
  }
}
