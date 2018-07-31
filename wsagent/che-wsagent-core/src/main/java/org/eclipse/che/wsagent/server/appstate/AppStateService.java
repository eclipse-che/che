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
package org.eclipse.che.wsagent.server.appstate;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.google.inject.Inject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.rest.annotations.Required;

/**
 * Service allows to get or persist serialized IDE state by user identifier.
 *
 * @author Roman Nikitenko
 */
@Path("app/state")
public class AppStateService {
  private AppStateManager appStateManager;

  @Inject
  public AppStateService(AppStateManager appStateManager) {
    this.appStateManager = appStateManager;
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Load saved serialized IDE state of current workspace by user identifier",
    notes =
        "It is expected that saved IDE state object is valid, so any validations are not performed. "
            + "Empty string will be returned when IDE state is not found."
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "User ID should be defined"),
    @ApiResponse(code = 500, message = "Server error")
  })
  public String loadAppState(
      @ApiParam(value = "User identifier") @Required @QueryParam("userId") String userId)
      throws ServerException, BadRequestException {
    try {
      return appStateManager.loadAppState(userId);
    } catch (ValidationException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @POST
  @Path("update")
  @Consumes(APPLICATION_JSON)
  @ApiOperation(
    value = "Save serialized IDE state of current workspace for given user",
    notes =
        "It is expected that incoming IDE state object is valid, so any validations are not performed."
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "User ID should be defined"),
    @ApiResponse(code = 500, message = "Server error")
  })
  public void saveState(
      @ApiParam(value = "User identifier") @Required @QueryParam("userId") String userId,
      @ApiParam(value = "Serialized IDE state") String json)
      throws ServerException, BadRequestException {
    try {
      appStateManager.saveState(userId, json);
    } catch (ValidationException e) {
      throw new BadRequestException(e.getMessage());
    }
  }
}
