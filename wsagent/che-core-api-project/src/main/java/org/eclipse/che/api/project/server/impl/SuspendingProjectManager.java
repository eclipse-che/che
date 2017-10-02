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
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;

@Singleton
public class SuspendingProjectManager
    implements org.eclipse.che.api.project.server.api.ProjectManager {

  private final ProjectManager projectManager;
  private final FileWatcherManager fileWatcherManager;

  @Inject
  public SuspendingProjectManager(
      ProjectManager projectManager, FileWatcherManager fileWatcherManager) {
    this.fileWatcherManager = fileWatcherManager;
    this.projectManager = projectManager;
  }

  @Override
  public boolean isRegistered(String wsPath) {
    return projectManager.isRegistered(wsPath);
  }

  @Override
  public Optional<RegisteredProject> get(String wsPath) {
    return projectManager.get(wsPath);
  }

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    return projectManager.getClosest(wsPath);
  }

  @Override
  public RegisteredProject getOrNull(String wsPath) {
    return projectManager.getOrNull(wsPath);
  }

  @Override
  public RegisteredProject getClosestOrNull(String wsPath) {
    return projectManager.getClosestOrNull(wsPath);
  }

  @Override
  public Set<RegisteredProject> getAll() {
    return projectManager.getAll();
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {
    return projectManager.getAll(wsPath);
  }

  @Override
  public Set<RegisteredProject> createAll(Map<ProjectConfig, Map<String, String>> projectConfigs)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    fileWatcherManager.suspend();
    try {
      return projectManager.createAll(projectConfigs);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    fileWatcherManager.suspend();
    try {
      return projectManager.create(projectConfig, options);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    fileWatcherManager.suspend();
    try {
      return projectManager.updateAll(projectConfigs);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    fileWatcherManager.suspend();
    try {
      return projectManager.update(projectConfig);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    fileWatcherManager.suspend();
    try {
      return projectManager.deleteAll(wsPaths);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    fileWatcherManager.suspend();
    try {
      return projectManager.delete(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Set<RegisteredProject> deleteAll()
      throws ServerException, ForbiddenException, ConflictException {
    fileWatcherManager.suspend();
    try {
      return projectManager.deleteAll();
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public RegisteredProject copy(String srcWsPath, String srcDstPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    fileWatcherManager.suspend();
    try {
      return projectManager.copy(srcWsPath, srcDstPath, overwrite);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public RegisteredProject move(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    fileWatcherManager.suspend();
    try {
      return projectManager.move(srcWsPath, dstWsPath, overwrite);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {
    return null;
  }

  @Override
  public RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {
    return null;
  }

  @Override
  public RegisteredProject doImport(
      NewProjectConfig newProjectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    fileWatcherManager.suspend();
    try {
      return projectManager.doImport(newProjectConfig, rewrite, consumer);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> newProjectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    fileWatcherManager.suspend();
    try {
      return projectManager.doImport(newProjectConfigs, rewrite, consumer);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    fileWatcherManager.suspend();
    try {
      return projectManager.doImport(wsPath, sourceStorage, rewrite, consumer);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    fileWatcherManager.suspend();
    try {
      return projectManager.doImport(projectLocations, rewrite, consumer);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public ProjectTypeResolution qualify(String path, String projectTypeId)
      throws ServerException, NotFoundException {
    return projectManager.qualify(path, projectTypeId);
  }

  @Override
  public List<ProjectTypeResolution> qualify(String path)
      throws ServerException, NotFoundException {
    return projectManager.qualify(path);
  }
}
