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

import com.google.common.annotations.Beta;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Required;
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
      notes = "Notifies workspace activity to prevent stop by timeout when workspace is used.")
  @ApiResponses(@ApiResponse(code = 204, message = "Activity counted"))
  public void active(@ApiParam(value = "Workspace id") @PathParam("wsId") String wsId)
      throws ForbiddenException, NotFoundException, ServerException {
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(wsId);
    if (workspace.getStatus() == RUNNING) {
      workspaceActivityManager.update(wsId, System.currentTimeMillis());
      LOG.debug("Updated activity on workspace {}", wsId);
    }
  }

  @Beta
  @GET
  @ApiOperation("Retrieves the IDs of workspaces that have been in given state.")
  @ApiResponses(
      @ApiResponse(
          code = 200,
          message = "Array of workspace IDs produced.",
          response = String[].class))
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWorkspacesByActivity(
      @QueryParam("status") @Required @ApiParam("The requested status of the workspaces")
          WorkspaceStatus status,
      @QueryParam("threshold")
          @DefaultValue("-1")
          @ApiParam(
              "Optionally, limit the results only to workspaces that have been in the provided"
                  + " status since before this time (in epoch millis). If both threshold and minDuration"
                  + " are specified, minDuration is NOT taken into account.")
          long threshold,
      @QueryParam("minDuration")
          @DefaultValue("-1")
          @ApiParam(
              "Instead of a threshold, one can also use this parameter to specify the minimum"
                  + " duration that the workspaces need to have been in the given state. The duration is"
                  + " specified in milliseconds. If both threshold and minDuration are specified,"
                  + " minDuration is NOT taken into account.")
          long minDuration,
      @QueryParam("maxItems")
          @DefaultValue("" + Pages.DEFAULT_PAGE_SIZE)
          @ApiParam("Maximum number of items on a page of results.")
          int maxItems,
      @QueryParam("skipCount") @DefaultValue("0") @ApiParam("How many items to skip.")
          long skipCount)
      throws ServerException, BadRequestException {

    if (status == null) {
      throw new BadRequestException("The status query parameter is query.");
    }

    long limit = threshold;

    if (limit == -1) {
      limit = System.currentTimeMillis();
      if (minDuration != -1) {
        limit -= minDuration;
      }
    }

    Page<String> data =
        workspaceActivityManager.findWorkspacesInStatus(status, limit, maxItems, skipCount);

    return Response.ok(data).header("Link", createLinkHeader(data)).build();
  }
}
