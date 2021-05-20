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
package org.eclipse.che.multiuser.resource.api.free;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.multiuser.resource.api.DtoConverter;
import org.eclipse.che.multiuser.resource.model.FreeResourcesLimit;
import org.eclipse.che.multiuser.resource.shared.dto.FreeResourcesLimitDto;

/**
 * Defines REST API for managing of free resources limits
 *
 * @author Sergii Leschenko
 */
@Api(value = "resource-free", description = "Free resources limit REST API")
@Path("/resource/free")
public class FreeResourcesLimitService extends Service {
  private final FreeResourcesLimitManager freeResourcesLimitManager;
  private final FreeResourcesLimitValidator freeResourcesLimitValidator;

  @Inject
  public FreeResourcesLimitService(
      FreeResourcesLimitValidator freeResourcesLimitValidator,
      FreeResourcesLimitManager freeResourcesLimitManager) {
    this.freeResourcesLimitManager = freeResourcesLimitManager;
    this.freeResourcesLimitValidator = freeResourcesLimitValidator;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Store free resources limit", response = FreeResourcesLimitDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The resources limit successfully stored"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 409, message = "The specified account doesn't exist"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response storeFreeResourcesLimit(
      @ApiParam(value = "Free resources limit") FreeResourcesLimitDto resourcesLimit)
      throws BadRequestException, NotFoundException, ConflictException, ServerException {
    freeResourcesLimitValidator.check(resourcesLimit);
    return Response.status(201)
        .entity(DtoConverter.asDto(freeResourcesLimitManager.store(resourcesLimit)))
        .build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get free resources limits",
      response = FreeResourcesLimitDto.class,
      responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The resources limits successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getFreeResourcesLimits(
      @ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
      @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
      throws ServerException {

    final Page<? extends FreeResourcesLimit> limitsPage =
        freeResourcesLimitManager.getAll(maxItems, skipCount);

    return Response.ok()
        .entity(limitsPage.getItems(DtoConverter::asDto))
        .header("Link", createLinkHeader(limitsPage))
        .build();
  }

  @GET
  @Path("/{accountId}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get free resources limit for account with given id",
      response = FreeResourcesLimitDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "The resources limit successfully fetched"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "Resources limit for given account was not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public FreeResourcesLimitDto getFreeResourcesLimit(
      @ApiParam(value = "Account id") @PathParam("accountId") String accountId)
      throws BadRequestException, NotFoundException, ServerException {
    return DtoConverter.asDto(freeResourcesLimitManager.get(accountId));
  }

  @DELETE
  @Path("/{accountId}")
  @ApiOperation(
      value = "Remove free resources limit for account with given id",
      response = FreeResourcesLimitDto.class)
  @ApiResponses({
    @ApiResponse(code = 204, message = "The resources limit successfully removed"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void removeFreeResourcesLimit(
      @ApiParam(value = "Account id") @PathParam("accountId") String accountId)
      throws ServerException {
    freeResourcesLimitManager.remove(accountId);
  }
}
