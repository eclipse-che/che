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
package org.eclipse.che.api.project.server.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;

public interface ProjectManager {

  boolean isRegistered(String wsPath);

  Optional<RegisteredProject> get(String wsPath);

  Optional<RegisteredProject> getClosest(String wsPath);

  RegisteredProject getOrNull(String wsPath);

  RegisteredProject getClosestOrNull(String wsPath);

  Set<RegisteredProject> getAll();

  Set<RegisteredProject> getAll(String wsPath);

  RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException;

  Set<RegisteredProject> createAll(Map<ProjectConfig, Map<String, String>> projectConfigs)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException;

  RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException;

  Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException;

  Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException;

  Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException;

  Set<RegisteredProject> deleteAll() throws ServerException, ForbiddenException, ConflictException;

  RegisteredProject copy(String src, String dst, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException;

  RegisteredProject move(String src, String dst, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException;

  RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException;

  RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException;

  RegisteredProject doImport(
      NewProjectConfig projectConfigs, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException;

  Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> projectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException;

  Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException;

  RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException;

  ProjectTypeResolution qualify(String path, String projectTypeId)
      throws ServerException, NotFoundException;

  List<ProjectTypeResolution> qualify(String path) throws ServerException, NotFoundException;
}
