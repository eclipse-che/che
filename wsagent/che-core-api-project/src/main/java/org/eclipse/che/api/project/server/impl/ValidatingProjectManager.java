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

import static org.eclipse.che.api.fs.server.WsPathUtils.parentOf;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.RegisteredProject;

/**
 * Preforms preliminary project manager method parameters validation and on success passes execution
 * further. In current implementation next functional unit in a call chain is {@link
 * SynchronizingProjectManager}. On validation failure corresponding exception is thrown.
 */
@Singleton
public class ValidatingProjectManager implements ProjectManager {

  private final ExecutiveProjectManager executiveProjectManager;
  private final FsManager fsManager;

  @Inject
  public ValidatingProjectManager(
      ExecutiveProjectManager executiveProjectManager, FsManager fsManager) {
    this.fsManager = fsManager;
    this.executiveProjectManager = executiveProjectManager;
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
  public RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    String wsPath = projectConfig.getPath();
    if (wsPath == null) {
      throw new BadRequestException("Path is not defined.");
    }

    String parentWsPath = parentOf(wsPath);
    if (!fsManager.existsAsDir(parentWsPath)) {
      throw new NotFoundException("Parent does not exist: " + parentWsPath);
    }

    String type = projectConfig.getType();
    if (type == null) {
      throw new ConflictException("Project type is not defined: " + wsPath);
    }

    if (executiveProjectManager.get(wsPath).isPresent()) {
      throw new ConflictException("Project config already exists for: " + wsPath);
    }

    return executiveProjectManager.create(projectConfig, options);
  }

  @Override
  public RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    String wsPath = projectConfig.getPath();
    if (wsPath == null) {
      throw new BadRequestException("Project workspace path is not defined");
    }

    if (!fsManager.existsAsDir(wsPath)) {
      throw new NotFoundException("Directory does not exist: " + wsPath);
    }

    return executiveProjectManager.update(projectConfig);
  }

  @Override
  public Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    return executiveProjectManager.delete(wsPath);
  }

  @Override
  public Set<RegisteredProject> deleteAll()
      throws ServerException, ForbiddenException, ConflictException {
    return executiveProjectManager.deleteAll();
  }

  @Override
  public RegisteredProject copy(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {

    if (!fsManager.existsAsDir(srcWsPath)) {
      throw new NotFoundException("Project directory does not exist or is a file: " + srcWsPath);
    }

    String parentWsPath = parentOf(dstWsPath);
    if (!fsManager.existsAsDir(parentWsPath)) {
      throw new NotFoundException(
          "Destination parent does not exist or is a file: " + parentWsPath);
    }

    if (!overwrite && fsManager.exists(dstWsPath)) {
      throw new ConflictException("Destination item exists but overwrite is false: " + dstWsPath);
    }

    if (!executiveProjectManager.isRegistered(srcWsPath)) {
      throw new NotFoundException("Source project is not registered" + srcWsPath);
    }

    return executiveProjectManager.copy(srcWsPath, dstWsPath, overwrite);
  }

  @Override
  public RegisteredProject move(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    if (!executiveProjectManager.isRegistered(srcWsPath)) {
      throw new NotFoundException("Project is not registered: " + srcWsPath);
    }

    if (!fsManager.existsAsDir(srcWsPath)) {
      throw new NotFoundException("Project directory does not exist or is a file: " + srcWsPath);
    }

    String dstParentWsPath = parentOf(dstWsPath);
    if (!fsManager.existsAsDir(dstParentWsPath)) {
      throw new NotFoundException(
          "Destination parent directory does not exist or is a file: " + dstParentWsPath);
    }

    if (!overwrite && fsManager.existsAsDir(dstWsPath)) {
      throw new ConflictException("Destination item exists but overwrite is false: " + dstWsPath);
    }

    return executiveProjectManager.move(srcWsPath, dstWsPath, overwrite);
  }

  @Override
  public RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {
    return executiveProjectManager.setType(wsPath, type, asMixin);
  }

  @Override
  public RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {
    return executiveProjectManager.removeType(wsPath, type);
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
