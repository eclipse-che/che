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
package org.eclipse.che.multiuser.organization.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.multiuser.organization.api.DtoConverter.asDto;

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
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * Defines Organization REST API.
 *
 * @author Sergii Leschenko
 */
@Api(value = "/organization", description = "Organization REST API")
@Path("/organization")
public class OrganizationService extends Service {
  private final OrganizationManager organizationManager;
  private final OrganizationLinksInjector linksInjector;
  private final OrganizationValidator organizationValidator;

  @Inject
  public OrganizationService(
      OrganizationManager organizationManager,
      OrganizationLinksInjector linksInjector,
      OrganizationValidator organizationValidator) {
    this.organizationManager = organizationManager;
    this.linksInjector = linksInjector;
    this.organizationValidator = organizationValidator;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create new organization", response = OrganizationDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The organization successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(
      code = 409,
      message =
          "Conflict error occurred during the organization creation"
              + "(e.g. The organization with such name already exists)"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response create(
      @ApiParam(value = "Organization to create", required = true) OrganizationDto organization)
      throws BadRequestException, NotFoundException, ConflictException, ServerException {
    organizationValidator.checkOrganization(organization);
    return Response.status(201)
        .entity(
            linksInjector.injectLinks(
                asDto(organizationManager.create(organization)), getServiceContext()))
        .build();
  }

  @POST
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update organization", response = OrganizationDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "The organization successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "The organization with given id was not found"),
    @ApiResponse(
      code = 409,
      message =
          "Conflict error occurred during the organization creation"
              + "(e.g. The organization with such name already exists)"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public OrganizationDto update(
      @ApiParam("Organization id") @PathParam("id") String organizationId,
      @ApiParam(value = "Organization to update", required = true) OrganizationDto organization)
      throws BadRequestException, ConflictException, NotFoundException, ServerException {
    organizationValidator.checkOrganization(organization);
    return linksInjector.injectLinks(
        asDto(organizationManager.update(organizationId, organization)), getServiceContext());
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation("Remove organization with given id")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The organization successfully removed"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void remove(@ApiParam("Organization id") @PathParam("id") String organization)
      throws ServerException {
    organizationManager.remove(organization);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Path("/{organizationId}")
  @ApiOperation(value = "Get organization by id", response = OrganizationDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "The organization successfully fetched"),
    @ApiResponse(code = 404, message = "The organization with given id was not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public OrganizationDto getById(
      @ApiParam("Organization id") @PathParam("organizationId") String organizationId)
      throws NotFoundException, ServerException {
    return linksInjector.injectLinks(
        asDto(organizationManager.getById(organizationId)), getServiceContext());
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Path("/find")
  @ApiOperation(value = "Find organization by name", response = OrganizationDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "The organization successfully fetched"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "The organization with given name was not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public OrganizationDto find(
      @ApiParam(value = "Organization name", required = true) @QueryParam("name")
          String organizationName)
      throws NotFoundException, ServerException, BadRequestException {
    checkArgument(organizationName != null, "Missed organization's name");
    return linksInjector.injectLinks(
        asDto(organizationManager.getByName(organizationName)), getServiceContext());
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Path("/{parent}/organizations")
  @ApiOperation(
    value = "Get child organizations",
    response = OrganizationDto.class,
    responseContainer = "list"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The child organizations successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getByParent(
      @ApiParam("Parent organization id") @PathParam("parent") String parent,
      @ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
      @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
      throws ServerException, BadRequestException {

    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");
    final Page<? extends Organization> organizationsPage =
        organizationManager.getByParent(parent, maxItems, skipCount);
    return Response.ok()
        .entity(
            organizationsPage.getItems(
                organization ->
                    linksInjector.injectLinks(asDto(organization), getServiceContext())))
        .header("Link", createLinkHeader(organizationsPage))
        .build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get user's organizations",
    notes = "When user parameter is missed then will be fetched current user's organizations",
    response = OrganizationDto.class,
    responseContainer = "list"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The organizations successfully fetched"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getOrganizations(
      @ApiParam(value = "User id") @QueryParam("user") String userId,
      @ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
      @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
      throws ServerException, BadRequestException {

    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");
    if (userId == null) {
      userId = EnvironmentContext.getCurrent().getSubject().getUserId();
    }
    final Page<? extends Organization> organizationsPage =
        organizationManager.getByMember(userId, maxItems, skipCount);
    return Response.ok()
        .entity(
            organizationsPage.getItems(
                organization ->
                    linksInjector.injectLinks(asDto(organization), getServiceContext())))
        .header("Link", createLinkHeader(organizationsPage))
        .build();
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * @param expression a boolean expression
   * @param errorMessage the exception message to use if the check fails
   * @throws BadRequestException if {@code expression} is false
   */
  private void checkArgument(boolean expression, String errorMessage) throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(errorMessage);
    }
  }
}
