/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.activity;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for accessing API for updating activity timestamp of running workspaces.
 *
 * @author Anton Korneta
 */
@Singleton
@Path("/activity")
public class WorkspaceActivityService extends Service {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceActivityService.class);

  private final WorkspaceActivityManager workspaceActivityManager;
  private final WorkspaceManager workspaceManager;

  @Inject
  public WorkspaceActivityService(
      WorkspaceActivityManager workspaceActivityManager, WorkspaceManager wsManager) {
    this.workspaceActivityManager = workspaceActivityManager;
    this.workspaceManager = wsManager;
  }

  @PUT
  @Path("/{wsId}")
  @ApiOperation(
    value = "Notifies workspace activity",
    notes = "Notifies workspace activity to prevent stop by timeout when workspace is used."
  )
  @ApiResponses(@ApiResponse(code = 204, message = "Activity counted"))
  public void active(@ApiParam(value = "Workspace id") @PathParam("wsId") String wsId)
      throws ForbiddenException, NotFoundException, ServerException {
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(wsId);
    if (workspace.getStatus() == RUNNING) {
      workspaceActivityManager.update(wsId, System.currentTimeMillis());
      LOG.debug("Updated activity on workspace {}", wsId);
    }
  }
}
