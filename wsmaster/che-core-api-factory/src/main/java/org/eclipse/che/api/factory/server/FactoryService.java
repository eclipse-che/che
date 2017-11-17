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
package org.eclipse.che.api.factory.server;

import static java.lang.Boolean.parseBoolean;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.factory.server.FactoryLinksHelper.createLinks;

import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.agent.server.filters.AddExecInstallerInEnvironmentUtil;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.factory.server.builder.FactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.AuthorDto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.URLEncodedUtils;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines Factory REST API.
 *
 * @author Anton Korneta
 * @author Florent Benoit
 */
@Api(value = "/factory", description = "Factory manager")
@Path("/factory")
public class FactoryService extends Service {
  private static final Logger LOG = LoggerFactory.getLogger(FactoryService.class);

  /** Error message if there is no plugged resolver. */
  public static final String ERROR_NO_RESOLVER_AVAILABLE =
      "Cannot build factory with any of the provided parameters.";

  /** Validate query parameter. If true, factory will be validated */
  public static final String VALIDATE_QUERY_PARAMETER = "validate";

  /** Set of resolvers for factories. Injected through an holder. */
  private final Set<FactoryParametersResolver> factoryParametersResolvers;

  private final FactoryManager factoryManager;
  private final UserManager userManager;
  private final PreferenceManager preferenceManager;
  private final FactoryEditValidator editValidator;
  private final FactoryCreateValidator createValidator;
  private final FactoryAcceptValidator acceptValidator;
  private final FactoryBuilder factoryBuilder;
  private final WorkspaceManager workspaceManager;

  @Inject
  public FactoryService(
      FactoryManager factoryManager,
      UserManager userManager,
      PreferenceManager preferenceManager,
      FactoryCreateValidator createValidator,
      FactoryAcceptValidator acceptValidator,
      FactoryEditValidator editValidator,
      FactoryBuilder factoryBuilder,
      WorkspaceManager workspaceManager,
      FactoryParametersResolverHolder factoryParametersResolverHolder) {
    this.factoryManager = factoryManager;
    this.userManager = userManager;
    this.createValidator = createValidator;
    this.preferenceManager = preferenceManager;
    this.acceptValidator = acceptValidator;
    this.editValidator = editValidator;
    this.factoryBuilder = factoryBuilder;
    this.workspaceManager = workspaceManager;
    this.factoryParametersResolvers =
        factoryParametersResolverHolder.getFactoryParametersResolvers();
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create a new factory based on configuration")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Factory successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "User does not have rights to create factory"),
    @ApiResponse(code = 409, message = "When factory with given name and creator already exists"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public FactoryDto saveFactory(FactoryDto factory)
      throws BadRequestException, ServerException, ForbiddenException, ConflictException,
          NotFoundException {
    requiredNotNull(factory, "Factory configuration");
    factoryBuilder.checkValid(factory);
    processDefaults(factory);
    AddExecInstallerInEnvironmentUtil.addExecInstaller(factory.getWorkspace());
    createValidator.validateOnCreate(factory);
    return injectLinks(asDto(factoryManager.saveFactory(factory)));
  }

  @GET
  @Path("/{id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get factory by its identifier",
    notes = "If validate parameter is not specified, retrieved factory wont be validated"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "Response contains requested factory entry"),
    @ApiResponse(code = 400, message = "Missed required parameters, failed to validate factory"),
    @ApiResponse(code = 404, message = "Factory with specified identifier does not exist"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public FactoryDto getFactory(
      @ApiParam(value = "Factory identifier") @PathParam("id") String factoryId,
      @ApiParam(
            value = "Whether or not to validate values like it is done when accepting the factory",
            allowableValues = "true, false",
            defaultValue = "false"
          )
          @DefaultValue("false")
          @QueryParam("validate")
          Boolean validate)
      throws BadRequestException, NotFoundException, ServerException {
    final FactoryDto factoryDto = asDto(factoryManager.getById(factoryId));
    if (validate) {
      acceptValidator.validateOnAccept(factoryDto);
    }
    return injectLinks(factoryDto);
  }

  @GET
  @Path("/find")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value =
        "Get factory by attribute, "
            + "the attribute must match one of the Factory model fields with type 'String', "
            + "e.g. (factory.name, factory.creator.name)"
            + " This method is going to be deprecated or limited in scope in 6.0 GA "
            + "since it's not optimized on backend performance. "
            +"Expected parameters creator.userId=? or name=?."
      ,
    notes =
        "If specify more than one value for a single query parameter then will be taken the first one"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "Response contains list requested factories"),
    @ApiResponse(
      code = 400,
      message = "When query does not contain at least one attribute to search for"
    ),
    @ApiResponse(code = 500, message = "Internal server error")
  })
  @Deprecated
  public List<FactoryDto> getFactoryByAttribute(
      @DefaultValue("0") @QueryParam("skipCount") Integer skipCount,
      @DefaultValue("30") @QueryParam("maxItems") Integer maxItems,
      @Context UriInfo uriInfo)
      throws BadRequestException, ServerException {
    final Set<String> skip = ImmutableSet.of("token", "skipCount", "maxItems");
    final List<Pair<String, String>> query =
        URLEncodedUtils.parse(uriInfo.getRequestUri())
            .entrySet()
            .stream()
            .filter(param -> !skip.contains(param.getKey()) && !param.getValue().isEmpty())
            .map(entry -> Pair.of(entry.getKey(), entry.getValue().iterator().next()))
            .collect(toList());
    checkArgument(!query.isEmpty(), "Query must contain at least one attribute");

    for (Pair<String, String> pair : query) {
      if (!pair.first.equals("creator.userId") && !pair.first.equals("name")) {
        LOG.warn(
            "Method factory.find is going to be removed or limited in scope in 6.0 GA."
                + " Requested attributes {}, skipCount {}, maxItems {}",
            query,
            skip,
            maxItems);
        break;
      }
    }

    final List<FactoryDto> factories = new ArrayList<>();
    for (Factory factory : factoryManager.getByAttribute(maxItems, skipCount, query)) {
      factories.add(injectLinks(asDto(factory)));
    }
    return factories;
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Update factory information by configuration and specified identifier",
    notes =
        "Update factory based on the factory id which is passed in a path parameter. "
            + "For perform this operation user needs respective rights"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "Factory successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "User does not have rights to update factory"),
    @ApiResponse(code = 404, message = "Factory to update not found"),
    @ApiResponse(
      code = 409,
      message =
          "Conflict error occurred during factory update"
              + "(e.g. Factory with such name and creator already exists)"
    ),
    @ApiResponse(code = 500, message = "Internal server error")
  })
  public FactoryDto updateFactory(
      @ApiParam(value = "Factory identifier") @PathParam("id") String factoryId, FactoryDto update)
      throws BadRequestException, NotFoundException, ServerException, ForbiddenException,
          ConflictException {
    requiredNotNull(update, "Factory configuration");
    update.setId(factoryId);
    final Factory existing = factoryManager.getById(factoryId);
    // check if the current user has enough access to edit the factory
    editValidator.validate(existing);
    factoryBuilder.checkValid(update, true);
    // validate the new content
    createValidator.validateOnCreate(update);
    return injectLinks(asDto(factoryManager.updateFactory(update)));
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(
    value = "Removes factory by its identifier",
    notes =
        "Removes factory based on the factory id which is passed in a path parameter. "
            + "For perform this operation user needs respective rights"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "Factory successfully removed"),
    @ApiResponse(code = 403, message = "User not authorized to call this operation"),
    @ApiResponse(code = 404, message = "Factory not found"),
    @ApiResponse(code = 500, message = "Internal server error")
  })
  public void removeFactory(@ApiParam(value = "Factory identifier") @PathParam("id") String id)
      throws ForbiddenException, ServerException {
    factoryManager.removeFactory(id);
  }

  @GET
  @Path("/workspace/{ws-id}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Construct factory from workspace",
    notes = "This call returns a Factory.json that is used to create a factory"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "Response contains requested factory JSON"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 404, message = "Workspace not found"),
    @ApiResponse(code = 500, message = "Internal server error")
  })
  public Response getFactoryJson(
      @ApiParam(value = "Workspace identifier") @PathParam("ws-id") String wsId,
      @ApiParam(value = "Project path") @QueryParam("path") String path)
      throws BadRequestException, NotFoundException, ServerException {
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(wsId);
    excludeProjectsWithoutLocation(workspace, path);
    final FactoryDto factoryDto =
        DtoFactory.newDto(FactoryDto.class)
            .withV("4.0")
            .withWorkspace(
                org.eclipse.che.api.workspace.server.DtoConverter.asDto(workspace.getConfig()));
    return Response.ok(factoryDto, APPLICATION_JSON)
        .header(CONTENT_DISPOSITION, "attachment; filename=factory.json")
        .build();
  }

  @POST
  @Path("/resolver")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Create factory by providing map of parameters",
    notes = "Get JSON with factory information"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "Factory successfully built from parameters"),
    @ApiResponse(code = 400, message = "Missed required parameters, failed to validate factory"),
    @ApiResponse(code = 500, message = "Internal server error")
  })
  public FactoryDto resolveFactory(
      @ApiParam(value = "Parameters provided to create factories") Map<String, String> parameters,
      @ApiParam(
            value = "Whether or not to validate values like it is done when accepting a Factory",
            allowableValues = "true,false",
            defaultValue = "false"
          )
          @DefaultValue("false")
          @QueryParam(VALIDATE_QUERY_PARAMETER)
          Boolean validate)
      throws ServerException, BadRequestException {

    // check parameter
    requiredNotNull(parameters, "Factory build parameters");

    // search matching resolver and create factory from matching resolver
    for (FactoryParametersResolver resolver : factoryParametersResolvers) {
      if (resolver.accept(parameters)) {
        final FactoryDto factory = resolver.createFactory(parameters);
        if (validate) {
          acceptValidator.validateOnAccept(factory);
        }
        return injectLinks(factory);
      }
    }
    // no match
    throw new BadRequestException(ERROR_NO_RESOLVER_AVAILABLE);
  }

  /** Injects factory links. If factory is named then accept named link will be injected. */
  private FactoryDto injectLinks(FactoryDto factory) {
    String username = null;
    if (factory.getCreator() != null && factory.getCreator().getUserId() != null) {
      try {
        username = userManager.getById(factory.getCreator().getUserId()).getName();
      } catch (ApiException ignored) {
        // when impossible to get username then named factory link won't be injected
      }
    }
    return factory.withLinks(createLinks(factory, getServiceContext(), username));
  }

  /**
   * Filters workspace projects and removes projects without source location. If there is no at
   * least one project with source location then {@link BadRequestException} will be thrown
   */
  private static void excludeProjectsWithoutLocation(
      WorkspaceImpl usersWorkspace, String projectPath) throws BadRequestException {
    final boolean notEmptyPath = projectPath != null;
    // Condition for sifting valid project in user's workspace
    Predicate<ProjectConfig> predicate =
        projectConfig -> {
          // if project is a sub project (it's path contains another project) , then location can be
          // null
          final boolean isSubProject = projectConfig.getPath().indexOf('/', 1) != -1;
          final boolean hasNotEmptySource =
              projectConfig.getSource() != null
                  && projectConfig.getSource().getType() != null
                  && projectConfig.getSource().getLocation() != null;

          return !(notEmptyPath && !projectPath.equals(projectConfig.getPath()))
              && (isSubProject || hasNotEmptySource);
        };

    // Filtered out projects by path and source storage presence
    final List<ProjectConfigImpl> filtered =
        usersWorkspace.getConfig().getProjects().stream().filter(predicate).collect(toList());
    checkArgument(
        !filtered.isEmpty(),
        "Unable to create factory from this workspace, "
            + "because it does not contains projects with source storage");
    usersWorkspace.getConfig().setProjects(filtered);
  }

  /**
   * Checks the current user if it is not temporary then adds to the factory creator information and
   * time of creation
   */
  private void processDefaults(FactoryDto factory) throws ForbiddenException {
    try {
      final String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
      final User user = userManager.getById(userId);
      if (user == null || parseBoolean(preferenceManager.find(userId).get("temporary"))) {
        throw new ForbiddenException("Current user is not allowed to use this method.");
      }
      factory.setCreator(
          DtoFactory.newDto(AuthorDto.class)
              .withUserId(userId)
              .withName(user.getName())
              .withEmail(user.getEmail())
              .withCreated(System.currentTimeMillis()));
    } catch (NotFoundException | ServerException ex) {
      throw new ForbiddenException("Current user is not allowed to use this method");
    }
  }

  /** Converts {@link Factory} to dto object */
  private FactoryDto asDto(Factory factory) throws ServerException {
    try {
      return DtoConverter.asDto(factory, userManager.getById(factory.getCreator().getUserId()));
    } catch (ServerException | NotFoundException ex) {
      throw new ServerException("Failed to retrieve factory creator");
    }
  }

  /** Usage of a dedicated class to manage the optional resolvers */
  protected static class FactoryParametersResolverHolder {

    /** Optional inject for the resolvers. */
    @com.google.inject.Inject(optional = true)
    private Set<FactoryParametersResolver> factoryParametersResolvers;

    /**
     * Provides the set of resolvers if there are some else return an empty set.
     *
     * @return a non null set
     */
    public Set<FactoryParametersResolver> getFactoryParametersResolvers() {
      if (factoryParametersResolvers != null) {
        return factoryParametersResolvers;
      } else {
        return Collections.emptySet();
      }
    }
  }

  /**
   * Creates factory image from input stream. InputStream should be closed manually.
   *
   * @param is input stream with image data
   * @param mediaType media type of image
   * @param name image name
   * @return factory image, if {@param is} has no content then empty factory image will be returned
   * @throws BadRequestException when factory image exceeded maximum size
   * @throws ServerException when any server errors occurs
   */
  public static FactoryImage createImage(InputStream is, String mediaType, String name)
      throws BadRequestException, ServerException {
    try {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final byte[] buffer = new byte[1024];
      int read;
      while ((read = is.read(buffer, 0, buffer.length)) != -1) {
        out.write(buffer, 0, read);
        if (out.size() > 1024 * 1024) {
          throw new BadRequestException("Maximum upload size exceeded.");
        }
      }

      if (out.size() == 0) {
        return new FactoryImage();
      }
      out.flush();

      return new FactoryImage(out.toByteArray(), mediaType, name);
    } catch (IOException ioEx) {
      throw new ServerException(ioEx.getLocalizedMessage());
    }
  }

  /**
   * Checks object reference is not {@code null}
   *
   * @param object object reference to check
   * @param subject used as subject of exception message "{subject} required"
   * @throws BadRequestException when object reference is {@code null}
   */
  private static void requiredNotNull(Object object, String subject) throws BadRequestException {
    if (object == null) {
      throw new BadRequestException(subject + " required");
    }
  }

  /**
   * Checks that expression is true, throws {@link BadRequestException} otherwise.
   *
   * <p>Exception uses error message built from error message template and error message parameters.
   */
  private static void checkArgument(boolean expression, String errorMessage)
      throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(errorMessage);
    }
  }
}
