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
package org.eclipse.che.api.installer.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.eclipse.che.api.installer.server.DtoConverter.asDto;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * Defines Installer REST API.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leshchenko
 * @see InstallerRegistry
 * @see Installer
 */
@Api(value = "/installer", description = "Installer REST API")
@Path("/installer")
public class InstallerRegistryService extends Service {

  public static final String TOTAL_ITEMS_COUNT_HEADER = "Total-Items-Count";

  private final InstallerRegistry installerRegistry;

  @Inject
  public InstallerRegistryService(InstallerRegistry installerRegistry) {
    this.installerRegistry = installerRegistry;
  }

  @GET
  @Path("/{key}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get the specified the installer", response = InstallerDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested installer entity"),
    @ApiResponse(code = 400, message = "Installer key has wrong format"),
    @ApiResponse(code = 404, message = "Installer not found in the registry"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public InstallerDto getInstaller(@ApiParam("The installer key") @PathParam("key") String key)
      throws InstallerException {
    return asDto(installerRegistry.getInstaller(key));
  }

  @GET
  @Path("/{id}/versions")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get a list of available versions of the specified installer",
    response = List.class
  )
  @ApiResponses({
    @ApiResponse(
      code = 200,
      message = "The response contains available versions of the specified installers"
    ),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public List<String> getVersions(@ApiParam("The installer id") @PathParam("id") String id)
      throws InstallerException {
    return installerRegistry.getVersions(id);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get a collection of the available installers",
    response = Installer.class,
    responseContainer = "collection"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains collection of available installers"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getInstallers(
      @ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
      @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
      throws InstallerException, BadRequestException {

    try {
      Page<? extends Installer> installers = installerRegistry.getInstallers(maxItems, skipCount);
      return Response.ok()
          .entity(
              installers.getItems().stream().map(DtoConverter::asDto).collect(Collectors.toList()))
          .header("Link", createLinkHeader(installers))
          .header(TOTAL_ITEMS_COUNT_HEADER, installers.getTotalItemsCount())
          .build();
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage());
    }
  }

  @POST
  @Path("/orders")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Order the specified installers",
    response = Installer.class,
    responseContainer = "list"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains the list of ordered installers"),
    @ApiResponse(code = 400, message = "Specified list contains invalid installer key"),
    @ApiResponse(code = 404, message = "Specified list contains unavailable installer"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public List<InstallerDto> getOrderedInstallers(List<String> keys) throws InstallerException {
    return installerRegistry
        .getOrderedInstallers(keys)
        .stream()
        .map(DtoConverter::asDto)
        .collect(Collectors.toList());
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @ApiOperation(value = "Add installer")
  @ApiResponses({
    @ApiResponse(code = 201, message = "Installer successfully added"),
    @ApiResponse(
      code = 409,
      message = "Installer with corresponding fully-qualified name already exists"
    ),
    @ApiResponse(code = 500, message = "Couldn't create installer due to internal server error")
  })
  public Response add(InstallerDto installerDto) throws InstallerException {
    installerRegistry.add(installerDto);
    return Response.status(CREATED).build();
  }

  @DELETE
  @Path("/{key}")
  @ApiOperation(value = "Remove installer")
  @ApiResponses({
    @ApiResponse(code = 204, message = "Installer successfully removed"),
    @ApiResponse(code = 400, message = "Invalid installer key"),
    @ApiResponse(code = 500, message = "Couldn't remove installer due to internal server error")
  })
  public void remove(@ApiParam("The installer key") @PathParam("key") String key)
      throws InstallerException {
    installerRegistry.remove(key);
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @ApiOperation(value = "Update installer")
  @ApiResponses({
    @ApiResponse(code = 204, message = "Installer successfully updated"),
    @ApiResponse(
      code = 404,
      message = "Installer with corresponding fully-qualified name doesn't exist in the registry"
    ),
    @ApiResponse(code = 500, message = "Couldn't update installer due to internal server error")
  })
  public void update(InstallerDto installerDto) throws InstallerException {
    installerRegistry.update(installerDto);
  }
}
