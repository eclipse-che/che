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
package org.eclipse.che.api.project.server.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.project.shared.RegisteredProject;

/**
 * Synchronize project registry state on workspace agent and master using actual implementation of
 * {@link ProjectSynchronizer} and passes execution further. In current implementation next unit in
 * a call chain is {@link ExecutiveProjectManager}
 */
@Singleton
public class SynchronizingProjectManager implements ProjectManager {

  private final ExecutiveProjectManager executiveProjectManager;
  private final ProjectSynchronizer projectSynchronizer;

  @Inject
  public SynchronizingProjectManager(
      ExecutiveProjectManager executiveProjectManager, ProjectSynchronizer projectSynchronizer) {
    this.executiveProjectManager = executiveProjectManager;
    this.projectSynchronizer = projectSynchronizer;
  }

  @Override
  public boolean isRegistered(String wsPath) {
    return executiveProjectManager.isRegistered(wsPath);
  }

  @Override
  public Optional<RegisteredProject> get(String wsPath) {
    return executiveProjectManager.get(wsPath);
  }

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    return executiveProjectManager.getClosest(wsPath);
  }

  @Override
  public RegisteredProject getOrNull(String wsPath) {
    return executiveProjectManager.getOrNull(wsPath);
  }

  @Override
  public RegisteredProject getClosestOrNull(String wsPath) {
    return executiveProjectManager.getClosestOrNull(wsPath);
  }

  @Override
  public Set<RegisteredProject> getAll() {
    return executiveProjectManager.getAll();
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {
    return executiveProjectManager.getAll(wsPath);
  }

  @Override
  public Set<RegisteredProject> createAll(Map<ProjectConfig, Map<String, String>> projectConfigs)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    try {
      return executiveProjectManager.createAll(projectConfigs);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    try {
      return executiveProjectManager.create(projectConfig, options);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    try {
      return executiveProjectManager.updateAll(projectConfigs);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    try {
      return executiveProjectManager.update(projectConfig);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    try {
      return executiveProjectManager.deleteAll(wsPaths);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    try {
      return executiveProjectManager.delete(wsPath);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public Set<RegisteredProject> deleteAll()
      throws ServerException, ForbiddenException, ConflictException {
    try {
      return executiveProjectManager.deleteAll();
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject copy(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    try {
      return executiveProjectManager.copy(srcWsPath, dstWsPath, overwrite);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject move(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    try {
      return executiveProjectManager.move(srcWsPath, dstWsPath, overwrite);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {
    try {
      return executiveProjectManager.setType(wsPath, type, asMixin);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {
    try {
      return executiveProjectManager.removeType(wsPath, type);
    } finally {
      projectSynchronizer.synchronize();
    }
  }

  @Override
  public RegisteredProject doImport(
      NewProjectConfig projectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    return executiveProjectManager.doImport(projectConfig, rewrite, consumer);
  }

  @Override
  public Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> newProjectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    return executiveProjectManager.doImport(newProjectConfigs, rewrite, consumer);
  }

  @Override
  public RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    return executiveProjectManager.doImport(wsPath, sourceStorage, rewrite, consumer);
  }

  @Override
  public Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    return executiveProjectManager.doImport(projectLocations, rewrite, consumer);
  }

  @Override
  public ProjectTypeResolution verify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException {
    return executiveProjectManager.verify(wsPath, projectTypeId);
  }

  @Override
  public List<ProjectTypeResolution> recognize(String wsPath)
      throws ServerException, NotFoundException {
    return executiveProjectManager.recognize(wsPath);
  }
}
