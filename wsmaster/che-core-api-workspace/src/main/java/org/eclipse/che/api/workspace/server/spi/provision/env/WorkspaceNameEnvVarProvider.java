/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.provision.env;

import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides environment variable with workspace name
 *
 * @author Sergii Kabashniuk
 */
public class WorkspaceNameEnvVarProvider implements EnvVarProvider {

  /** Env variable for workspace name */
  public static final String CHE_WORKSPACE_NAME = "CHE_WORKSPACE_NAME";

  private final WorkspaceDao workspaceDao;

  @Inject
  public WorkspaceNameEnvVarProvider(WorkspaceDao workspaceDao) {
    this.workspaceDao = workspaceDao;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws InfrastructureException {
    try {
      WorkspaceImpl workspace = workspaceDao.get(runtimeIdentity.getWorkspaceId());
      return Pair.of(CHE_WORKSPACE_NAME, workspace.getName());
    } catch (NotFoundException | ServerException e) {
      throw new InfrastructureException(
          "Not able to get workspace name for workspace with id "
              + runtimeIdentity.getWorkspaceId(),
          e);
    }
  }
}
