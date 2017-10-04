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

import static java.io.File.separator;

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
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.FsPathResolver;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;

@Singleton
public class ValidatingProjectManager
    implements ProjectManager {

  private final SynchronizingProjectManager synchronizingProjectManager;
  private final FsManager fsManager;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final FsPathResolver fsPathResolver;

  @Inject
  public ValidatingProjectManager(
      SynchronizingProjectManager synchronizingProjectManager,
      FsManager fsManager,
      ProjectConfigRegistry projectConfigRegistry,
      FsPathResolver fsPathResolver) {
    this.fsManager = fsManager;
    this.synchronizingProjectManager = synchronizingProjectManager;
    this.projectConfigRegistry = projectConfigRegistry;
    this.fsPathResolver = fsPathResolver;
  }

  @Override
  public boolean isRegistered(String wsPath) {
    return synchronizingProjectManager.isRegistered(wsPath);
  }

  @Override
  public Optional<RegisteredProject> get(String wsPath) {
    return synchronizingProjectManager.get(wsPath);
  }

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    return synchronizingProjectManager.getClosest(wsPath);
  }

  @Override
  public RegisteredProject getOrNull(String wsPath) {
    return synchronizingProjectManager.getOrNull(wsPath);
  }

  @Override
  public RegisteredProject getClosestOrNull(String wsPath) {
    return synchronizingProjectManager.getClosestOrNull(wsPath);
  }

  @Override
  public Set<RegisteredProject> getAll() {
    return synchronizingProjectManager.getAll();
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {
    return synchronizingProjectManager.getAll(wsPath);
  }

  @Override
  public Set<RegisteredProject> createAll(Map<ProjectConfig, Map<String, String>> projectConfigs)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
      BadRequestException {
    for (ProjectConfig projectConfig : projectConfigs.keySet()) {
      String wsPath = projectConfig.getPath();
      if (wsPath == null) {
        throw new BadRequestException("Path is not defined.");
      }

      String parentWsPath = fsPathResolver.getParentWsPath(wsPath);
      if (!fsManager.isRoot(parentWsPath) || !fsManager.existsAsDirectory(parentWsPath)) {
        throw new NotFoundException("Parent does not exist: " + parentWsPath);
      }

      String type = projectConfig.getType();
      if (type == null) {
        throw new ConflictException("Project type is not defined: " + wsPath);
      }

      if (projectConfigRegistry.get(wsPath).isPresent()) {
        throw new ConflictException("Project config already exists for: " + wsPath);
      }
    }

    return synchronizingProjectManager.createAll(projectConfigs);
  }

  @Override
  public RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
      BadRequestException {
    String wsPath = projectConfig.getPath();
    if (wsPath == null) {
      throw new BadRequestException("Path is not defined.");
    }

    String parentWsPath = fsPathResolver.getParentWsPath(wsPath);
    if (!fsManager.isRoot(parentWsPath) || !fsManager.existsAsDirectory(parentWsPath)) {
      throw new NotFoundException("Parent does not exist: " + parentWsPath);
    }

    String type = projectConfig.getType();
    if (type == null) {
      throw new ConflictException("Project type is not defined: " + wsPath);
    }

    if (projectConfigRegistry.get(wsPath).isPresent()) {
      throw new ConflictException("Project config already exists for: " + wsPath);
    }

    return synchronizingProjectManager.create(projectConfig, options);
  }

  @Override
  public Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
      BadRequestException {

    for (ProjectConfig projectConfig : projectConfigs) {
      String wsPath = projectConfig.getPath();
      if (wsPath == null) {
        throw new BadRequestException("Project workspace path is not defined");
      }

      if (!fsManager.existsAsDirectory(wsPath)) {
        throw new NotFoundException("Directory does not exist: " + wsPath);
      }
    }
    return synchronizingProjectManager.updateAll(projectConfigs);
  }

  @Override
  public RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
      BadRequestException {
    String wsPath = projectConfig.getPath();
    if (wsPath == null) {
      throw new BadRequestException("Project workspace path is not defined");
    }

    if (!fsManager.existsAsDirectory(wsPath)) {
      throw new NotFoundException("Directory does not exist: " + wsPath);
    }

    return synchronizingProjectManager.update(projectConfig);
  }

  @Override
  public Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    return synchronizingProjectManager.deleteAll(wsPaths);
  }

  @Override
  public Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    return synchronizingProjectManager.delete(wsPath);
  }

  @Override
  public Set<RegisteredProject> deleteAll()
      throws ServerException, ForbiddenException, ConflictException {
    return synchronizingProjectManager.deleteAll();
  }

  @Override
  public RegisteredProject copy(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {

    if (!fsManager.existsAsDirectory(srcWsPath)) {
      throw new NotFoundException("Project directory does not exist or is a file: " + srcWsPath);
    }

    String parentWsPath = dstWsPath.substring(0, dstWsPath.lastIndexOf(separator));
    if (!fsManager.existsAsDirectory(parentWsPath)) {
      throw new NotFoundException(
          "Destination parent does not exist or is a file: " + parentWsPath);
    }

    if (!overwrite && fsManager.exists(dstWsPath)) {
      throw new ConflictException("Destination item exists but overwrite is false: " + dstWsPath);
    }

    if (!projectConfigRegistry.isRegistered(srcWsPath)) {
      throw new NotFoundException("Source project is not registered" + srcWsPath);
    }

    return synchronizingProjectManager.copy(srcWsPath, dstWsPath, overwrite);
  }

  @Override
  public RegisteredProject move(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    if (!projectConfigRegistry.isRegistered(srcWsPath)) {
      throw new NotFoundException("Project is not registered: " + srcWsPath);
    }

    if (!fsManager.existsAsDirectory(srcWsPath)) {
      throw new NotFoundException("Project directory does not exist or is a file: " + srcWsPath);
    }

    String dstParentWsPath = fsPathResolver.getParentWsPath(dstWsPath);
    if (!fsManager.existsAsDirectory(dstParentWsPath)) {
      throw new NotFoundException(
          "Destination parent directory does not exist or is a file: " + dstParentWsPath);
    }

    if (!overwrite && fsManager.existsAsDirectory(dstWsPath)) {
      throw new ConflictException("Destination item exists but overwrite is false: " + dstWsPath);
    }

    return synchronizingProjectManager.move(srcWsPath, dstWsPath, overwrite);
  }

  @Override
  public RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
      ForbiddenException {
    return synchronizingProjectManager.setType(wsPath, type, asMixin);
  }

  @Override
  public RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
      ForbiddenException {
    return synchronizingProjectManager.removeType(wsPath, type);
  }

  @Override
  public RegisteredProject doImport(
      NewProjectConfig newProjectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
      NotFoundException, BadRequestException {
    return synchronizingProjectManager.doImport(newProjectConfig, rewrite, consumer);
  }

  @Override
  public Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> newProjectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
      NotFoundException, BadRequestException {
    return synchronizingProjectManager.doImport(newProjectConfigs, rewrite, consumer);
  }

  @Override
  public RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
      NotFoundException {
    return synchronizingProjectManager.doImport(wsPath, sourceStorage, rewrite, consumer);
  }

  @Override
  public Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
      NotFoundException {
    return synchronizingProjectManager.doImport(projectLocations, rewrite, consumer);
  }

  @Override
  public ProjectTypeResolution qualify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException {
    return synchronizingProjectManager.qualify(wsPath, projectTypeId);
  }

  @Override
  public List<ProjectTypeResolution> qualify(String wsPath)
      throws ServerException, NotFoundException {
    return synchronizingProjectManager.qualify(wsPath);
  }
}
