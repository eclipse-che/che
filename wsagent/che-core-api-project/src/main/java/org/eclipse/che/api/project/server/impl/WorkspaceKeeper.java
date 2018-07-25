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

import java.util.Set;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;

/**
 * Abstraction for keeping current workspace
 *
 * @author gazarenkov
 */
public interface WorkspaceKeeper {

  /** @return projects from Workspace Config */
  Set<ProjectConfig> getProjects() throws ServerException;

  /** @return workspace's runtime */
  Runtime getRuntime() throws ServerException;
}
