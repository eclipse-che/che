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
package org.eclipse.che.multiuser.api.permission.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
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
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.api.permission.shared.dto.DomainDto;
import org.eclipse.che.multiuser.api.permission.shared.dto.PermissionsDto;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;

/**
 * Defines Permissions REST API
 *
 * @author Sergii Leschenko
 */
@Api(value = "/permissions", description = "Permissions REST API")
@Path("/permissions")
public class PermissionsService extends Service {
  private final PermissionsManager permissionsManager;
  private final InstanceParameterValidator instanceValidator;

  @Inject
  public PermissionsService(
      PermissionsManager permissionsManager, InstanceParameterValidator instanceValidator) {
    this.permissionsManager = permissionsManager;
    this.instanceValidator = instanceValidator;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get all supported domains or only requested if domain parameter specified",
    response = DomainDto.class,
    responseContainer = "List"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The domains successfully fetched"),
    @ApiResponse(code = 404, message = "Requested domain is not supported"),
    @ApiResponse(code = 500, message = "Internal server error occurred during domains fetching")
  })
  public List<DomainDto> getSupportedDomains(
      @ApiParam("Id of requested domain") @QueryParam("domain") String domainId)
      throws NotFoundException {
    if (isNullOrEmpty(domainId)) {
      return permissionsManager.getDomains().stream().map(this::asDto).collect(Collectors.toList());
    } else {
      return singletonList(asDto(permissionsManager.getDomain(domainId)));
    }
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @ApiOperation("Store given permissions")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The permissions successfully stored"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "Domain of permissions is not supported"),
    @ApiResponse(
      code = 409,
      message = "New permissions removes last 'setPermissions' of given instance"
    ),
    @ApiResponse(
      code = 409,
      message = "Given domain requires non nullable value for instance but it is null"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred during permissions storing")
  })
  public void storePermissions(
      @ApiParam(value = "The permissions to store", required = true) PermissionsDto permissionsDto)
      throws ServerException, BadRequestException, ConflictException, NotFoundException {
    checkArgument(permissionsDto != null, "Permissions descriptor required");
    checkArgument(!isNullOrEmpty(permissionsDto.getUserId()), "User required");
    checkArgument(!isNullOrEmpty(permissionsDto.getDomainId()), "Domain required");
    instanceValidator.validate(permissionsDto.getDomainId(), permissionsDto.getInstanceId());
    checkArgument(!permissionsDto.getActions().isEmpty(), "One or more actions required");

    permissionsManager.storePermission(permissionsDto);
  }

  @GET
  @Path("/{domain}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get permissions of current user which are related to specified domain and instance",
    response = PermissionsDto.class
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The permissions successfully fetched"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "Specified domain is unsupported"),
    @ApiResponse(
      code = 404,
      message = "Permissions for current user with specified domain and instance was not found"
    ),
    @ApiResponse(
      code = 409,
      message = "Given domain requires non nullable value for instance but it is null"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred during permissions fetching")
  })
  public PermissionsDto getCurrentUsersPermissions(
      @ApiParam(value = "Domain id to retrieve user's permissions") @PathParam("domain")
          String domain,
      @ApiParam(value = "Instance id to retrieve user's permissions") @QueryParam("instance")
          String instance)
      throws BadRequestException, NotFoundException, ConflictException, ServerException {
    instanceValidator.validate(domain, instance);
    return toDto(
        permissionsManager.get(
            EnvironmentContext.getCurrent().getSubject().getUserId(), domain, instance));
  }

  @GET
  @Path("/{domain}/all")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get permissions which are related to specified domain and instance",
    response = PermissionsDto.class,
    responseContainer = "List"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The permissions successfully fetched"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "Specified domain is unsupported"),
    @ApiResponse(
      code = 409,
      message = "Given domain requires non nullable value for instance but it is null"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred during permissions fetching")
  })
  public Response getUsersPermissions(
      @ApiParam(value = "Domain id to retrieve users' permissions") @PathParam("domain")
          String domain,
      @ApiParam(value = "Instance id to retrieve users' permissions") @QueryParam("instance")
          String instance,
      @ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
      @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
      throws ServerException, NotFoundException, ConflictException, BadRequestException {
    instanceValidator.validate(domain, instance);
    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");

    final Page<AbstractPermissions> permissionsPage =
        permissionsManager.getByInstance(domain, instance, maxItems, skipCount);
    return Response.ok()
        .entity(permissionsPage.getItems(this::toDto))
        .header("Link", createLinkHeader(permissionsPage))
        .build();
  }

  @DELETE
  @Path("/{domain}")
  @ApiOperation("Removes user's permissions related to the particular instance of specified domain")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The permissions successfully removed"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "Specified domain is unsupported"),
    @ApiResponse(code = 409, message = "User has last 'setPermissions' of given instance"),
    @ApiResponse(
      code = 409,
      message = "Given domain requires non nullable value for instance but it is null"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred during permissions removing")
  })
  public void removePermissions(
      @ApiParam("Domain id to remove user's permissions") @PathParam("domain") String domain,
      @ApiParam(value = "Instance id to remove user's permissions") @QueryParam("instance")
          String instance,
      @ApiParam(value = "User id", required = true) @QueryParam("user") @Required String user)
      throws BadRequestException, NotFoundException, ConflictException, ServerException {
    instanceValidator.validate(domain, instance);
    permissionsManager.remove(user, domain, instance);
  }

  private DomainDto asDto(PermissionsDomain domain) {
    return DtoFactory.newDto(DomainDto.class)
        .withId(domain.getId())
        .withAllowedActions(domain.getAllowedActions());
  }

  private void checkArgument(boolean expression, String message) throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(message);
    }
  }

  private PermissionsDto toDto(Permissions permissions) {
    return DtoFactory.newDto(PermissionsDto.class)
        .withUserId(permissions.getUserId())
        .withDomainId(permissions.getDomainId())
        .withInstanceId(permissions.getInstanceId())
        .withActions(permissions.getActions());
  }
}
