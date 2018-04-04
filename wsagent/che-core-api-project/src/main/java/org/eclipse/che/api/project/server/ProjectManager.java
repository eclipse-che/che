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
package org.eclipse.che.api.project.server;

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
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;

/** Facade for project related operations */
public interface ProjectManager {

  /**
   * Show if project configuration is contained in the project config registry
   *
   * @param wsPath absolute workspace path of a project
   * @return
   */
  boolean isRegistered(String wsPath);

  /**
   * Get an optional containing either project (if it exist) or nothing (if it does not exist)
   *
   * @param wsPath absolute workspace path of a project
   * @return
   */
  Optional<RegisteredProject> get(String wsPath);

  /**
   * Get optional with closest project that contains specified file system item
   *
   * @param wsPath absolute workspace path of a file system item
   * @return
   */
  Optional<RegisteredProject> getClosest(String wsPath);

  /**
   * Get either project (if it exist) or null (if it does not exist)
   *
   * @param wsPath absolute workspace path of a project
   * @return
   */
  RegisteredProject getOrNull(String wsPath);

  /**
   * Get closest project that contains specified file system item or null
   *
   * @param wsPath absolute workspace path of a file system item
   * @return
   */
  RegisteredProject getClosestOrNull(String wsPath);

  /**
   * Get all registered projects
   *
   * @return
   */
  Set<RegisteredProject> getAll();

  /**
   * Get all sub-projects of a project denoted by workspace path. If a project is not registered or
   * has no sub-projects - returns empty list.
   *
   * @param wsPath absolute workspace path of a project
   * @return
   */
  Set<RegisteredProject> getAll(String wsPath);

  /**
   * Create project with specified configuration and creation options
   *
   * @param projectConfig configuration of a project that is going to be created
   * @param options options of a project creation process
   * @return
   * @throws ConflictException is thrown if project is already registered, or project type is not
   *     defined
   * @throws ForbiddenException is thrown if operation is forbidden
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is thrown if parent location does not exist
   * @throws BadRequestException is thrown if project path is not defined
   */
  RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException;

  /**
   * Create all projects defined by specified configuration map
   *
   * @param projectConfigs map of project configurations and options
   * @return
   * @throws ConflictException is thrown if project is already registered, or project type is not
   *     defined
   * @throws ForbiddenException is thrown if an operation is forbidden
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is thrown if parent location does not exist
   * @throws BadRequestException is thrown if project path is not defined
   */
  Set<RegisteredProject> createAll(Map<ProjectConfig, Map<String, String>> projectConfigs)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException;

  /**
   * Update a project with a new configuration
   *
   * @param projectConfig project configuration
   * @return
   * @throws ForbiddenException is thrown if an operation is forbidden
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is thrown if a project directory does not exist
   * @throws ConflictException is thrown if a project does not exist
   * @throws BadRequestException is thrown if project path is not defined
   */
  RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException;

  /**
   * Update all projects with new configurations
   *
   * @param projectConfigs project configuration set
   * @return
   * @throws ForbiddenException is thrown if an operation is forbidden
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is thrown if a project directory does not exist
   * @throws ConflictException is thrown if a project does not exist
   * @throws BadRequestException is thrown if project path is not defined
   */
  Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException;

  /**
   * Delete the project denoted by the path
   *
   * @param wsPath absolute workspace path of a project
   * @return
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws ForbiddenException is thrown if an operation is forbidden
   * @throws NotFoundException is thrown if a project directory does not exist
   * @throws ConflictException is thrown if the item that the path denotes is not a project
   */
  Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException;

  /**
   * Delete all projects denoted by the paths
   *
   * @param wsPaths set of absolute workspace paths
   * @return
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws ForbiddenException is thrown if an operation is forbidden
   * @throws NotFoundException is thrown if a project directory does not exist
   * @throws ConflictException is thrown if the item that the path denotes is not a project
   */
  Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException;

  /**
   * Delete all projects
   *
   * @return
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws ForbiddenException is thrown if an operation is forbidden
   * @throws ConflictException is thrown if the item that the path denotes is not a project
   */
  Set<RegisteredProject> deleteAll() throws ServerException, ForbiddenException, ConflictException;

  /**
   * Copy project to a new destination
   *
   * @param src absolute workspace path of project source
   * @param dst absolute workspace path of project destination
   * @param overwrite overwrite on copy marker
   * @return
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is thrown if either project source directory does not exist or
   *     destination parent directory does not exist
   * @throws ConflictException is thrown if project at destination location already exist and
   *     overwrite is disabled
   * @throws ForbiddenException is thrown if an operation is forbidden
   */
  RegisteredProject copy(String src, String dst, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException;

  /**
   * Move project to a new destination
   *
   * @param src absolute workspace path of project source
   * @param dst absolute workspace path of project destination
   * @param overwrite overwrite on move marker
   * @return
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is thrown if either project source directory does not exist or
   *     destination parent directory does not exist
   * @throws ConflictException is thrown if project at destination location already exist and
   *     overwrite is disabled
   * @throws ForbiddenException is thrown if an operation is forbidden
   */
  RegisteredProject move(String src, String dst, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException;

  /**
   * Set type to a project
   *
   * @param wsPath absolute workspace path of a project
   * @param type project type
   * @param asMixin type mix-in marker
   * @return
   * @throws ConflictException
   * @throws NotFoundException
   * @throws ServerException
   * @throws BadRequestException
   * @throws ForbiddenException
   */
  RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException;

  /**
   * Remote type from a project
   *
   * @param wsPath absolute workspace path of a project
   * @param type project type
   * @return
   * @throws ConflictException
   * @throws NotFoundException
   * @throws ServerException
   * @throws BadRequestException
   * @throws ForbiddenException
   */
  RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException;

  /**
   * Import the project with specified configuration
   *
   * @param projectConfig project configuration
   * @param rewrite rewrite on import project marker
   * @param consumer json rpc message transmitter
   * @return
   * @throws ServerException
   * @throws ForbiddenException
   * @throws UnauthorizedException
   * @throws ConflictException
   * @throws NotFoundException
   * @throws BadRequestException
   */
  RegisteredProject doImport(
      NewProjectConfig projectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException;

  /**
   * Import all projects with specified configurations
   *
   * @param projectConfigs project configurations
   * @param rewrite rewrite on import project marker
   * @param consumer json rpc message transmitter
   * @return
   * @throws ServerException
   * @throws ForbiddenException
   * @throws UnauthorizedException
   * @throws ConflictException
   * @throws NotFoundException
   * @throws BadRequestException
   */
  Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> projectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException;

  /**
   * Import the project with specified locations
   *
   * @param projectLocations project locations
   * @param rewrite rewrite on import project marker
   * @param consumer json rpc message transmitter
   * @return
   * @throws ServerException
   * @throws ForbiddenException
   * @throws UnauthorizedException
   * @throws ConflictException
   * @throws NotFoundException
   */
  Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException;

  /**
   * Import the project with specified location
   *
   * @param wsPath absolute workspace path of a project
   * @param sourceStorage project source storage
   * @param rewrite rewrite on import project marker
   * @param consumer json rpc message transmitter
   * @return
   * @throws ServerException
   * @throws ForbiddenException
   * @throws UnauthorizedException
   * @throws ConflictException
   * @throws NotFoundException
   */
  RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException;

  /**
   * Verify project regarding its type
   *
   * @param wsPath absolute workspace path of a project
   * @param projectTypeId project type
   * @return
   * @throws ServerException is thrown if an error happend during operation execution
   * @throws NotFoundException is thrown if there is no project located at specified path
   */
  ProjectTypeResolution verify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException;

  /**
   * Recognize a project
   *
   * @param wsPath absolute workspace path of a project
   * @return
   * @throws ServerException is thrown if an error happend during operation execution
   * @throws NotFoundException is thrown if there is no project located at specified path
   */
  List<ProjectTypeResolution> recognize(String wsPath) throws ServerException, NotFoundException;
}
