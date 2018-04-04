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

import java.util.Optional;
import java.util.Set;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;

public interface ProjectSynchronizer {

  /** Synchronizes Project Config state on Agent and Master */
  void synchronize() throws ServerException;

  /** @return projects from Workspace Config */
  Set<ProjectConfig> getAll() throws ServerException;

  /** @return sub projects from Workspace Config */
  Set<ProjectConfig> getAll(String wsPath) throws ServerException;

  /** @return project with workspace path */
  Optional<ProjectConfig> get(String wsPath) throws ServerException;

  /** @return project with workspace path */
  ProjectConfig getOrNull(String wsPath) throws ServerException;
  /**
   * Adds project to Workspace Config
   *
   * @param project the project config
   */
  void add(ProjectConfig project) throws ServerException;

  /**
   * Updates particular project in Workspace Config
   *
   * @param project the project config
   */
  void update(ProjectConfig project) throws ServerException;

  /**
   * Removes particular project in Workspace Config
   *
   * @param project the project config
   */
  void remove(ProjectConfig project) throws ServerException;
}
