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
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.shared.RegisteredProject;

/**
 * Registry for projects
 *
 * @author gazarenkov
 */
public interface ProjectConfigRegistry {

  /** @return all the projects */
  Set<RegisteredProject> getAll();

  /**
   * @param wsPath root path
   * @return all the projects under wsPath
   */
  Set<RegisteredProject> getAll(String wsPath);

  /**
   * @param wsPath
   * @return project on wsPath as Optional object
   */
  Optional<RegisteredProject> get(String wsPath);

  /**
   * @param wsPath
   * @return project on wsPath or null
   */
  RegisteredProject getOrNull(String wsPath);

  /**
   * registers project
   *
   * @param config project config
   * @param updated whether project just u
   * @param detected
   * @return created project
   */
  RegisteredProject put(ProjectConfig config, boolean updated, boolean detected);

  /**
   * registers a folder on wsPath as a project
   *
   * @param wsPath path
   * @param updated whether project just u
   * @param detected
   * @return created project
   */
  RegisteredProject putIfAbsent(String wsPath, boolean updated, boolean detected);

  /**
   * deletes project
   *
   * @param wsPath path
   * @return deleted project
   */
  Optional<RegisteredProject> remove(String wsPath);

  /**
   * deletes project
   *
   * @param wsPath path
   * @return deleted project
   */
  RegisteredProject removeOrNull(String wsPath);

  /**
   * whether path contains registered project
   *
   * @param path
   * @return true or false
   */
  boolean isRegistered(String path);

  /**
   * closest registered project
   *
   * @param wsPath path
   * @return RegisteredProject
   */
  Optional<RegisteredProject> getClosest(String wsPath);
}
