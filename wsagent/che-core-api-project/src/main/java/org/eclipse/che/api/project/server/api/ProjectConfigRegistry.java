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

import java.util.Optional;
import java.util.Set;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.RegisteredProject;

public interface ProjectConfigRegistry {

  Set<RegisteredProject> getAll();

  Set<RegisteredProject> getAll(String wsPath);

  Optional<RegisteredProject> get(String wsPath);

  RegisteredProject getOrNull(String wsPath);

  RegisteredProject put(ProjectConfig config, boolean updated, boolean detected)
      throws ServerException;

  RegisteredProject put(String path, boolean updated, boolean detected) throws ServerException;

  Optional<RegisteredProject> remove(String wsPath);

  RegisteredProject removeOrNull(String wsPath);

  boolean isRegistered(String path);
}
