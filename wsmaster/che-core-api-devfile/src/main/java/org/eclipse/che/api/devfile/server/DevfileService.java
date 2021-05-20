/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.devfile.server.DtoConverter.asDto;
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.eclipse.che.api.workspace.server.devfile.Constants.SUPPORTED_VERSIONS;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
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
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.URLEncodedUtils;
import org.eclipse.che.dto.server.DtoFactory;

/** Defines Devfile REST API. */
@Api(value = "/devfile", description = "Devfile REST API")
@Path("/devfile")
public class DevfileService extends Service {

  private final DevfileSchemaProvider schemaCachedProvider;
  private final UserDevfileManager userDevfileManager;
  private final DevfileServiceLinksInjector linksInjector;

  @Inject
  public DevfileService(
      DevfileSchemaProvider schemaCachedProvider,
      UserDevfileManager userDevfileManager,
      DevfileServiceLinksInjector linksInjector) {
    this.userDevfileManager = userDevfileManager;
    this.linksInjector = linksInjector;
    this.schemaCachedProvider = schemaCachedProvider;
  }

  /**
   * Retrieves the json schema.
   *
   * @return json schema
   */
  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieves current version of devfile JSON schema")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The schema successfully retrieved"),
    @ApiResponse(code = 404, message = "The schema for given version was not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getSchema(
      @ApiParam("Devfile schema version") @DefaultValue(CURRENT_API_VERSION) @QueryParam("version")
          String version)
      throws ServerException, NotFoundException {
    if (!SUPPORTED_VERSIONS.contains(version)) {
      throw new NotFoundException(
          String.format(
              "Devfile schema version '%s' is invalid or not supported. Supported versions are '%s'.",
              version, SUPPORTED_VERSIONS));
    }

    try {
      return Response.ok(schemaCachedProvider.getSchemaContent(version)).build();
    } catch (FileNotFoundException e) {
      throw new NotFoundException(e.getLocalizedMessage());
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  @Path("/devfile")
  @POST
  @Consumes({APPLICATION_JSON, "text/yaml", "text/x-yaml"})
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Creates a new persistent Devfile from yaml representation",
      consumes = "application/json, text/yaml, text/x-yaml",
      produces = APPLICATION_JSON,
      nickname = "createFromDevfileYaml",
      response = UserDevfileDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The devfile successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to create a new devfile"),
    @ApiResponse(code = 409, message = "Conflict error occurred during the devfile creation"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response createFromDevfileYaml(
      @ApiParam(value = "The devfile to create", required = true) DevfileDto devfile)
      throws ConflictException, BadRequestException, ForbiddenException, NotFoundException,
          ServerException {
    requiredNotNull(devfile, "Devfile");
    return Response.status(201)
        .entity(
            linksInjector.injectLinks(
                asDto(
                    userDevfileManager.createDevfile(
                        DtoFactory.newDto(UserDevfileDto.class)
                            .withDevfile(devfile)
                            .withName(NameGenerator.generate("devfile-", 16)))),
                getServiceContext()))
        .build();
  }

  @POST
  @Consumes({APPLICATION_JSON})
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Creates a new persistent Devfile",
      consumes = "application/json",
      produces = APPLICATION_JSON,
      nickname = "create",
      response = UserDevfileDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The devfile successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to create a new devfile"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during the devfile creation"
                + "(e.g. The devfile with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response createFromUserDevfile(
      @ApiParam(value = "The devfile to create", required = true) UserDevfileDto userDevfileDto)
      throws ConflictException, BadRequestException, ForbiddenException, NotFoundException,
          ServerException {
    requiredNotNull(userDevfileDto, "Devfile");
    return Response.status(201)
        .entity(
            linksInjector.injectLinks(
                asDto(userDevfileManager.createDevfile(userDevfileDto)), getServiceContext()))
        .build();
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get devfile by its identifier")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested workspace entity"),
    @ApiResponse(code = 404, message = "The devfile with specified id does not exist"),
    @ApiResponse(code = 403, message = "The user is not allowed to read devfile"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public UserDevfileDto getById(
      @ApiParam(value = "UserDevfile identifier") @PathParam("id") String id)
      throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
    requiredNotNull(id, "id");
    return linksInjector.injectLinks(asDto(userDevfileManager.getById(id)), getServiceContext());
  }

  @GET
  @Path("search")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get devfiles which user can read",
      notes =
          "This operation can be performed only by authorized user. "
              + "It is possible to add additional constraints for the desired devfiles by specifying\n"
              + "multiple query parameters that is representing fields of the devfile. All constrains\n"
              + "would be combined with \"And\" condition. Also, it is possible to specify 'like:' prefix\n"
              + "for the query parameters. In this case instead of an exact match would be used SQL pattern like search.\n"
              + "Examples id=sdfsdf5&devfile.meta.name=like:%dfdf&",
      response = UserDevfileDto.class,
      responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The devfiles successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred during devfiles fetching")
  })
  public Response getUserDevfiles(
      @ApiParam("The number of the items to skip") @DefaultValue("0") @QueryParam("skipCount")
          Integer skipCount,
      @ApiParam("The limit of the items in the response, default is 30, maximum 60")
          @DefaultValue("30")
          @QueryParam("maxItems")
          Integer maxItems,
      @ApiParam(
              "A list of fields and directions of sort. By default items would be sorted by id. Example id:asc,name:desc.")
          @QueryParam("order")
          String order)
      throws ServerException, BadRequestException {
    final Set<String> skip = ImmutableSet.of("token", "skipCount", "maxItems", "order");
    Map<String, Set<String>> queryParams = URLEncodedUtils.parse(uriInfo.getRequestUri());
    final List<Pair<String, String>> query =
        queryParams
            .entrySet()
            .stream()
            .filter(param -> !param.getValue().isEmpty())
            .filter(param -> !skip.contains(param.getKey()))
            .map(entry -> Pair.of(entry.getKey(), entry.getValue().iterator().next()))
            .collect(toList());
    List<Pair<String, String>> searchOrder = Collections.emptyList();
    if (order != null && !order.isEmpty()) {
      try {
        searchOrder =
            Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .withKeyValueSeparator(":")
                .split(order)
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .collect(toList());
      } catch (IllegalArgumentException e) {
        throw new BadRequestException("Invalid `order` query parameter format." + e.getMessage());
      }
    }
    Page<? extends UserDevfile> userDevfilesPage =
        userDevfileManager.getUserDevfiles(maxItems, skipCount, query, searchOrder);

    List<UserDevfileDto> list =
        userDevfilesPage
            .getItems()
            .stream()
            .map(DtoConverter::asDto)
            .map(dto -> linksInjector.injectLinks(asDto(dto), getServiceContext()))
            .collect(toList());

    return Response.ok().entity(list).header("Link", createLinkHeader(userDevfilesPage)).build();
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update the devfile by replacing all the existing data with update")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The devfile successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the devfile"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during devfile update"
                + "(e.g. Workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public UserDevfileDto update(
      @ApiParam("The devfile id") @PathParam("id") String id,
      @ApiParam(value = "The devfile update", required = true) UserDevfileDto update)
      throws BadRequestException, ServerException, ForbiddenException, NotFoundException,
          ConflictException {
    requiredNotNull(update, "User Devfile configuration");
    update.setId(id);
    return linksInjector.injectLinks(
        asDto(userDevfileManager.updateUserDevfile(update)), getServiceContext());
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Removes the devfile")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The devfile successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access to remove the devfile"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void delete(@ApiParam("The devfile id") @PathParam("id") String id)
      throws BadRequestException, ServerException, ForbiddenException {
    userDevfileManager.removeUserDevfile(id);
  }

  /**
   * Checks object reference is not {@code null}
   *
   * @param object object reference to check
   * @param subject used as subject of exception message "{subject} required"
   * @throws BadRequestException when object reference is {@code null}
   */
  private void requiredNotNull(Object object, String subject) throws BadRequestException {
    if (object == null) {
      throw new BadRequestException(subject + " required");
    }
  }
}
