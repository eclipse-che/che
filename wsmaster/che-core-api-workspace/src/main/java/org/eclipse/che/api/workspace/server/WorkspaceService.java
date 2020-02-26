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
package org.eclipse.che.api.workspace.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;
import static org.eclipse.che.api.workspace.server.WorkspaceKeyValidator.validateKey;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_AUTO_START;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_DEVFILE_REGISTRY_URL_PROPERTY;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_PERSIST_VOLUMES_PROPERTY;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_PLUGIN_REGISTRY_URL_PROPERTY;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START;
import static org.eclipse.che.api.workspace.shared.Constants.DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.URLFileContentProvider;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.token.MachineAccessForbidden;
import org.eclipse.che.api.workspace.server.token.MachineTokenException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.ServerDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Defines Workspace REST API.
 *
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
@Api(value = "/workspace", description = "Workspace REST API")
@Path("/workspace")
public class WorkspaceService extends Service {

  private final WorkspaceManager workspaceManager;
  private final MachineTokenProvider machineTokenProvider;
  private final WorkspaceLinksGenerator linksGenerator;
  private final String pluginRegistryUrl;
  private final String devfileRegistryUrl;
  private final String apiEndpoint;
  private final boolean cheWorkspaceAutoStart;
  private final FileContentProvider devfileContentProvider;
  private final boolean defaultPersistVolumes;
  private final Long logLimitBytes;

  @Inject
  public WorkspaceService(
      @Named("che.api") String apiEndpoint,
      @Named(CHE_WORKSPACE_AUTO_START) boolean cheWorkspaceAutoStart,
      WorkspaceManager workspaceManager,
      MachineTokenProvider machineTokenProvider,
      WorkspaceLinksGenerator linksGenerator,
      @Named(CHE_WORKSPACE_PLUGIN_REGISTRY_URL_PROPERTY) @Nullable String pluginRegistryUrl,
      @Named(CHE_WORKSPACE_DEVFILE_REGISTRY_URL_PROPERTY) @Nullable String devfileRegistryUrl,
      @Named(CHE_WORKSPACE_PERSIST_VOLUMES_PROPERTY) boolean defaultPersistVolumes,
      URLFetcher urlFetcher,
      @Named(DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES) Long logLimitBytes) {
    this.apiEndpoint = apiEndpoint;
    this.cheWorkspaceAutoStart = cheWorkspaceAutoStart;
    this.workspaceManager = workspaceManager;
    this.machineTokenProvider = machineTokenProvider;
    this.linksGenerator = linksGenerator;
    this.pluginRegistryUrl = pluginRegistryUrl;
    this.devfileRegistryUrl = devfileRegistryUrl;
    this.devfileContentProvider = new URLFileContentProvider(null, urlFetcher);
    this.defaultPersistVolumes = defaultPersistVolumes;
    this.logLimitBytes = logLimitBytes;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Create a new workspace based on the configuration",
      notes =
          "This operation can be performed only by authorized user,"
              + "this user will be the owner of the created workspace",
      consumes = APPLICATION_JSON,
      produces = APPLICATION_JSON,
      response = WorkspaceConfigDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The workspace successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to create a new workspace"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during the workspace creation"
                + "(e.g. The workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response create(
      @ApiParam(value = "The configuration to create the new workspace", required = true)
          WorkspaceConfigDto config,
      @ApiParam(
              value =
                  "Workspace attribute defined in 'attrName:attrValue' format. "
                      + "The first ':' is considered as attribute name and value separator",
              examples = @Example({@ExampleProperty("attrName:value-with:colon")}))
          @QueryParam("attribute")
          List<String> attrsList,
      @ApiParam(
              value =
                  "The target infrastructure namespace (Kubernetes namespace or OpenShift"
                      + " project) where the workspace should be deployed to when started. This"
                      + " parameter is optional. The workspace creation will fail if the Che server"
                      + " is configured to not allow deploying into that infrastructure namespace.")
          @QueryParam("infrastructure-namespace")
          String infrastructureNamespace,
      @ApiParam(
              "If true then the workspace will be immediately "
                  + "started after it is successfully created")
          @QueryParam("start-after-create")
          @DefaultValue("false")
          Boolean startAfterCreate,
      @ApiParam("Namespace where workspace should be created") @QueryParam("namespace")
          String namespace)
      throws ConflictException, ServerException, BadRequestException, ForbiddenException,
          NotFoundException {
    requiredNotNull(config, "Workspace configuration");
    final Map<String, String> attributes = parseAttrs(attrsList);
    relativizeRecipeLinks(config);
    if (namespace == null) {
      namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    }
    if (!isNullOrEmpty(infrastructureNamespace)) {
      attributes.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, infrastructureNamespace);
    }
    WorkspaceImpl workspace;
    try {
      workspace = workspaceManager.createWorkspace(config, namespace, attributes);
    } catch (ValidationException x) {
      throw new BadRequestException(x.getMessage());
    }

    if (startAfterCreate) {
      workspaceManager.startWorkspace(workspace.getId(), null, new HashMap<>());
    }
    return Response.status(201).entity(asDtoWithLinksAndToken(workspace)).build();
  }

  @Path("/devfile")
  @POST
  @Consumes({APPLICATION_JSON, "text/yaml", "text/x-yaml"})
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Creates a new workspace based on the Devfile.",
      consumes = "application/json, text/yaml, text/x-yaml",
      produces = APPLICATION_JSON,
      nickname = "createFromDevfile",
      response = WorkspaceConfigDto.class)
  @ApiResponses({
    @ApiResponse(code = 201, message = "The workspace successfully created"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to create a new workspace"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during the workspace creation"
                + "(e.g. The workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response create(
      @ApiParam(value = "The devfile of the workspace to create", required = true)
          DevfileDto devfile,
      @ApiParam(
              value =
                  "Workspace attribute defined in 'attrName:attrValue' format. "
                      + "The first ':' is considered as attribute name and value separator",
              examples = @Example({@ExampleProperty("attrName:value-with:colon")}))
          @QueryParam("attribute")
          List<String> attrsList,
      @ApiParam(
              value =
                  "The target infrastructure namespace (Kubernetes namespace or OpenShift"
                      + " project) where the workspace should be deployed to when started. This"
                      + " parameter is optional. The workspace creation will fail if the Che server"
                      + " is configured to not allow deploying into that infrastructure namespace.")
          @QueryParam("infrastructure-namespace")
          String infrastructureNamespace,
      @ApiParam(
              "If true then the workspace will be immediately "
                  + "started after it is successfully created")
          @QueryParam("start-after-create")
          @DefaultValue("false")
          Boolean startAfterCreate,
      @ApiParam("Che namespace where workspace should be created") @QueryParam("namespace")
          String namespace,
      @HeaderParam(CONTENT_TYPE) MediaType contentType)
      throws ConflictException, BadRequestException, ForbiddenException, NotFoundException,
          ServerException {
    requiredNotNull(devfile, "Devfile");
    final Map<String, String> attributes = parseAttrs(attrsList);
    if (namespace == null) {
      namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    }
    if (!isNullOrEmpty(infrastructureNamespace)) {
      attributes.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, infrastructureNamespace);
    }
    WorkspaceImpl workspace;
    try {
      workspace =
          workspaceManager.createWorkspace(
              devfile,
              namespace,
              attributes,
              // create a new cache for each request so that we don't have to care about lifetime
              // of the cache, etc. The content is cached only for the duration of this call
              // (i.e. all the validation and provisioning of the devfile will download each
              // referenced file only once per request)
              FileContentProvider.cached(devfileContentProvider));
    } catch (ValidationException x) {
      throw new BadRequestException(x.getMessage());
    }

    if (startAfterCreate) {
      workspaceManager.startWorkspace(workspace.getId(), null, new HashMap<>());
    }
    return Response.status(201).entity(asDtoWithLinksAndToken(workspace)).build();
  }

  @GET
  @Path("/{key:.*}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get the workspace by the composite key",
      notes =
          "Composite key can be just workspace ID or in the "
              + "namespace:workspace_name form, where namespace is optional (e.g :workspace_name is valid key too."
              + "namespace/workspace_name form, where namespace can contain '/' character.")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested workspace entity"),
    @ApiResponse(code = 404, message = "The workspace with specified id does not exist"),
    @ApiResponse(code = 403, message = "The user is not workspace owner"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto getByKey(
      @ApiParam(
              value = "Composite key",
              examples =
                  @Example({
                    @ExampleProperty("workspace12345678"),
                    @ExampleProperty("namespace/workspace_name"),
                    @ExampleProperty("namespace_part_1/namespace_part_2/workspace_name")
                  }))
          @PathParam("key")
          String key,
      @ApiParam("Whether to include internal servers into runtime or not")
          @DefaultValue("false")
          @QueryParam("includeInternalServers")
          String includeInternalServers)
      throws NotFoundException, ServerException, ForbiddenException, BadRequestException {
    validateKey(key);
    boolean bIncludeInternalServers =
        isNullOrEmpty(includeInternalServers) || Boolean.parseBoolean(includeInternalServers);
    return filterServers(
        asDtoWithLinksAndToken(workspaceManager.getWorkspace(key)), bIncludeInternalServers);
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get workspaces which user can read",
      notes = "This operation can be performed only by authorized user",
      response = WorkspaceDto.class,
      responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspaces successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")
  })
  public Response getWorkspaces(
      @ApiParam("The number of the items to skip") @DefaultValue("0") @QueryParam("skipCount")
          Integer skipCount,
      @ApiParam("The limit of the items in the response, default is 30")
          @DefaultValue("30")
          @QueryParam("maxItems")
          Integer maxItems,
      @ApiParam("Workspace status") @QueryParam("status") String status)
      throws ServerException, BadRequestException {
    Page<WorkspaceImpl> workspacesPage =
        workspaceManager.getWorkspaces(
            EnvironmentContext.getCurrent().getSubject().getUserId(), false, maxItems, skipCount);
    return Response.ok()
        .entity(
            workspacesPage
                .getItems()
                .stream()
                .filter(ws -> status == null || status.equalsIgnoreCase(ws.getStatus().toString()))
                .map(DtoConverter::asDto)
                .collect(toList()))
        .header("Link", createLinkHeader(workspacesPage))
        .build();
  }

  @GET
  @Path("/namespace/{namespace:.*}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Get workspaces by given namespace",
      notes = "This operation can be performed only by authorized user",
      response = WorkspaceDto.class,
      responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspaces successfully fetched"),
    @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")
  })
  public List<WorkspaceDto> getByNamespace(
      @ApiParam("Workspace status") @QueryParam("status") String status,
      @ApiParam("The namespace") @PathParam("namespace") String namespace)
      throws ServerException, BadRequestException {
    return asDtosWithLinks(
        Pages.stream(
                (maxItems, skipCount) ->
                    workspaceManager.getByNamespace(namespace, false, maxItems, skipCount))
            .filter(ws -> status == null || status.equalsIgnoreCase(ws.getStatus().toString()))
            .collect(toList()));
  }

  @PUT
  @Path("/{id}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the workspace by replacing all the existing data with update",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspace successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
    @ApiResponse(
        code = 409,
        message =
            "Conflict error occurred during workspace update"
                + "(e.g. Workspace with such name already exists)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto update(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam(value = "The workspace update", required = true) WorkspaceDto update)
      throws BadRequestException, ServerException, ForbiddenException, NotFoundException,
          ConflictException {
    checkArgument(
        update.getConfig() != null ^ update.getDevfile() != null,
        "Required non-null workspace configuration or devfile update but not both");
    relativizeRecipeLinks(update.getConfig());
    return asDtoWithLinksAndToken(doUpdate(id, update));
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(
      value = "Removes the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The workspace successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access to remove the workspace"),
    @ApiResponse(code = 404, message = "The workspace doesn't exist"),
    @ApiResponse(code = 409, message = "The workspace is not stopped(has runtime)"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void delete(@ApiParam("The workspace id") @PathParam("id") String id)
      throws BadRequestException, ServerException, NotFoundException, ConflictException,
          ForbiddenException {
    workspaceManager.removeWorkspace(id);
  }

  @POST
  @Path("/{id}/runtime")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Start the workspace by the id",
      notes =
          "This operation can be performed only by the workspace owner."
              + "The workspace starts asynchronously")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspace is starting"),
    @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
    @ApiResponse(
        code = 403,
        message = "The user is not workspace owner." + "The operation is not allowed for the user"),
    @ApiResponse(code = 409, message = "Any conflict occurs during the workspace start"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto startById(
      @ApiParam("The workspace id") @PathParam("id") String workspaceId,
      @ApiParam("The name of the workspace environment that should be used for start")
          @QueryParam("environment")
          String envName,
      @QueryParam(DEBUG_WORKSPACE_START) @DefaultValue("false") Boolean debugWorkspaceStart)
      throws ServerException, BadRequestException, NotFoundException, ForbiddenException,
          ConflictException {

    Map<String, String> options = new HashMap<>();
    if (debugWorkspaceStart) {
      options.put(DEBUG_WORKSPACE_START, debugWorkspaceStart.toString());
      options.put(DEBUG_WORKSPACE_START_LOG_LIMIT_BYTES, logLimitBytes.toString());
    }

    return asDtoWithLinksAndToken(workspaceManager.startWorkspace(workspaceId, envName, options));
  }

  @POST
  @Path("/runtime")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Start the temporary workspace from the given configuration",
      notes =
          "This operation can be performed only by the authorized user or temp user."
              + "The workspace starts synchronously")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspace is starting"),
    @ApiResponse(code = 400, message = "The update config is not valid"),
    @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
    @ApiResponse(
        code = 403,
        message = "The user is not workspace owner" + "The operation is not allowed for the user"),
    @ApiResponse(
        code = 409,
        message =
            "Any conflict occurs during the workspace start"
                + "(e.g. workspace with such name already exists"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto startFromConfig(
      @ApiParam(value = "The configuration to start the workspace from", required = true)
          WorkspaceConfigDto config,
      @ApiParam("Weather this workspace is temporary or not")
          @QueryParam("temporary")
          @DefaultValue("false")
          Boolean isTemporary,
      @ApiParam("Namespace where workspace should be created") @QueryParam("namespace")
          String namespace)
      throws BadRequestException, ForbiddenException, NotFoundException, ServerException,
          ConflictException {
    requiredNotNull(config, "Workspace configuration");
    relativizeRecipeLinks(config);
    if (namespace == null) {
      namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    }

    try {
      WorkspaceImpl workspace =
          workspaceManager.startWorkspace(config, namespace, isTemporary, new HashMap<>());
      return asDtoWithLinksAndToken(workspace);
    } catch (ValidationException x) {
      throw new BadRequestException(x.getMessage());
    }
  }

  @DELETE
  @Path("/{id}/runtime")
  @ApiOperation(
      value = "Stop the workspace",
      notes =
          "This operation can be performed only by the workspace owner."
              + "The workspace stops asynchronously")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The workspace is stopping"),
    @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
    @ApiResponse(code = 403, message = "The user is not workspace owner"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void stop(@ApiParam("The workspace id") @PathParam("id") String id)
      throws ForbiddenException, NotFoundException, ServerException, ConflictException {
    workspaceManager.stopWorkspace(id, emptyMap());
  }

  @POST
  @Path("/{id}/command")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the workspace by adding a new command to it",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspace successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
    @ApiResponse(code = 404, message = "The workspace not found"),
    @ApiResponse(code = 409, message = "The command with such name already exists"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto addCommand(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam(value = "The new workspace command", required = true) CommandDto newCommand)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    requiredNotNull(newCommand, "Command");
    WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    workspace.getConfig().getCommands().add(new CommandImpl(newCommand));
    return asDtoWithLinksAndToken(doUpdate(id, workspace));
  }

  @PUT
  @Path("/{id}/command/{name}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the workspace command by replacing the command with a new one",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The command successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
    @ApiResponse(code = 404, message = "The workspace or the command not found"),
    @ApiResponse(code = 409, message = "The Command with such name already exists"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto updateCommand(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam("The name of the command") @PathParam("name") String cmdName,
      @ApiParam(value = "The command update", required = true) CommandDto update)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    requiredNotNull(update, "Command update");
    WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    List<CommandImpl> commands = workspace.getConfig().getCommands();
    if (!commands.removeIf(cmd -> cmd.getName().equals(cmdName))) {
      throw new NotFoundException(
          format("Workspace '%s' doesn't contain command '%s'", id, cmdName));
    }
    commands.add(new CommandImpl(update));
    return asDtoWithLinksAndToken(doUpdate(id, workspace));
  }

  @DELETE
  @Path("/{id}/command/{name}")
  @ApiOperation(
      value = "Remove the command from the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The command successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access delete the command"),
    @ApiResponse(code = 404, message = "The workspace not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void deleteCommand(
      @ApiParam("The id of the workspace") @PathParam("id") String id,
      @ApiParam("The name of the command to remove") @PathParam("name") String commandName)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    if (workspace
        .getConfig()
        .getCommands()
        .removeIf(command -> command.getName().equals(commandName))) {
      doUpdate(id, workspace);
    }
  }

  @POST
  @Path("/{id}/environment")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Add a new environment to the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The workspace successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to add the environment"),
    @ApiResponse(code = 404, message = "The workspace not found"),
    @ApiResponse(code = 409, message = "Environment with such name already exists"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto addEnvironment(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam(value = "The new environment", required = true) EnvironmentDto newEnvironment,
      @ApiParam(value = "The name of the environment", required = true) @QueryParam("name")
          String envName)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    requiredNotNull(newEnvironment, "New environment");
    requiredNotNull(envName, "New environment name");
    relativizeRecipeLinks(newEnvironment);
    WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    workspace.getConfig().getEnvironments().put(envName, new EnvironmentImpl(newEnvironment));
    return asDtoWithLinksAndToken(doUpdate(id, workspace));
  }

  @PUT
  @Path("/{id}/environment/{name}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the workspace environment by replacing it with a new one",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The environment successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the environment"),
    @ApiResponse(code = 404, message = "The workspace or the environment not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto updateEnvironment(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam("The name of the environment") @PathParam("name") String envName,
      @ApiParam(value = "The environment update", required = true) EnvironmentDto update)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    requiredNotNull(update, "Environment description");
    relativizeRecipeLinks(update);
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    EnvironmentImpl previous =
        workspace.getConfig().getEnvironments().put(envName, new EnvironmentImpl(update));
    if (previous == null) {
      throw new NotFoundException(
          format("Workspace '%s' doesn't contain environment '%s'", id, envName));
    }
    return asDtoWithLinksAndToken(doUpdate(id, workspace));
  }

  @DELETE
  @Path("/{id}/environment/{name}")
  @ApiOperation(
      value = "Remove the environment from the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The environment successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access remove the environment"),
    @ApiResponse(code = 404, message = "The workspace not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void deleteEnvironment(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam("The name of the environment") @PathParam("name") String envName)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    if (workspace.getConfig().getEnvironments().remove(envName) != null) {
      doUpdate(id, workspace);
    }
  }

  @POST
  @Path("/{id}/project")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Adds a new project to the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The project successfully added to the workspace"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to add the project"),
    @ApiResponse(code = 404, message = "The workspace not found"),
    @ApiResponse(code = 409, message = "Any conflict error occurs"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto addProject(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam(value = "The new project", required = true) ProjectConfigDto newProject)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    requiredNotNull(newProject, "New project config");
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    workspace.getConfig().getProjects().add(new ProjectConfigImpl(newProject));
    return asDtoWithLinksAndToken(doUpdate(id, workspace));
  }

  @PUT
  @Path("/{id}/project/{path:.*}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Update the workspace project by replacing it with a new one",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The project successfully updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 403, message = "The user does not have access to update the project"),
    @ApiResponse(code = 404, message = "The workspace or the project not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public WorkspaceDto updateProject(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam("The path to the project") @PathParam("path") String path,
      @ApiParam(value = "The project update", required = true) ProjectConfigDto update)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    requiredNotNull(update, "Project config");
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    final List<ProjectConfigImpl> projects = workspace.getConfig().getProjects();
    final String normalizedPath = path.startsWith("/") ? path : '/' + path;
    if (!projects.removeIf(project -> project.getPath().equals(normalizedPath))) {
      throw new NotFoundException(
          format("Workspace '%s' doesn't contain project with path '%s'", id, normalizedPath));
    }
    projects.add(new ProjectConfigImpl(update));
    return asDtoWithLinksAndToken(doUpdate(id, workspace));
  }

  @DELETE
  @Path("/{id}/project/{path:.*}")
  @ApiOperation(
      value = "Remove the project from the workspace",
      notes = "This operation can be performed only by the workspace owner")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The project successfully removed"),
    @ApiResponse(code = 403, message = "The user does not have access remove the project"),
    @ApiResponse(code = 404, message = "The workspace not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void deleteProject(
      @ApiParam("The workspace id") @PathParam("id") String id,
      @ApiParam("The name of the project to remove") @PathParam("path") String path)
      throws ServerException, BadRequestException, NotFoundException, ConflictException,
          ForbiddenException {
    final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
    if (workspace.getConfig() == null) {
      throw new ConflictException(
          "This method can not be invoked for workspace created from Devfile. Use update workspace method instead.");
    }
    final String normalizedPath = path.startsWith("/") ? path : '/' + path;
    if (workspace
        .getConfig()
        .getProjects()
        .removeIf(project -> project.getPath().equals(normalizedPath))) {
      doUpdate(id, workspace);
    }
  }

  @GET
  @Path("/settings")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get workspace server configuration values")
  @ApiResponses({@ApiResponse(code = 200, message = "The response contains server settings")})
  public Map<String, String> getSettings() {
    Builder<String, String> settings = ImmutableMap.builder();

    settings.put(
        Constants.SUPPORTED_RECIPE_TYPES,
        Joiner.on(",").join(workspaceManager.getSupportedRecipes()));
    settings.put(CHE_WORKSPACE_AUTO_START, Boolean.toString(cheWorkspaceAutoStart));

    if (pluginRegistryUrl != null) {
      settings.put("cheWorkspacePluginRegistryUrl", pluginRegistryUrl);
    }

    if (devfileRegistryUrl != null) {
      settings.put("cheWorkspaceDevfileRegistryUrl", devfileRegistryUrl);
    }

    settings.put(CHE_WORKSPACE_PERSIST_VOLUMES_PROPERTY, Boolean.toString(defaultPersistVolumes));

    return settings.build();
  }

  private static Map<String, String> parseAttrs(List<String> attributes)
      throws BadRequestException, ForbiddenException {
    if (attributes == null) {
      // we need to make room for the potential infrastructure namespace that can be put into
      // this map by the callers...
      return Maps.newHashMapWithExpectedSize(1);
    }
    final Map<String, String> res = Maps.newHashMapWithExpectedSize(attributes.size());
    for (String attribute : attributes) {
      final int colonIdx = attribute.indexOf(':');
      if (colonIdx == -1) {
        throw new BadRequestException(
            "Attribute '"
                + attribute
                + "' is not valid, "
                + "it should contain name and value separated with colon. "
                + "For example: attributeName:attributeValue");
      }
      String name = attribute.substring(0, colonIdx);
      String value = attribute.substring(colonIdx + 1);

      if (name.isEmpty())
        throw new BadRequestException(
            "Attribute '"
                + attribute
                + "' is not valid, "
                + "Empty attribute name is not allowed. ");
      if (name.startsWith("codenvy"))
        throw new ForbiddenException(
            "Attribute '" + attribute + "' is not allowed. 'codenvy' prefix is reserved. ");

      res.put(name, value);
    }
    return res;
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

  /**
   * Checks the specified expression.
   *
   * @param expression the expression to check
   * @param errorMessage error message that should be used if expression is false
   * @throws BadRequestException when the expression is false
   */
  private void checkArgument(boolean expression, String errorMessage) throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(String.valueOf(errorMessage));
    }
  }

  private void relativizeRecipeLinks(WorkspaceConfigDto config) {
    if (config != null) {
      Map<String, EnvironmentDto> environments = config.getEnvironments();
      if (environments != null && !environments.isEmpty()) {
        for (EnvironmentDto environment : environments.values()) {
          relativizeRecipeLinks(environment);
        }
      }
    }
  }

  private void relativizeRecipeLinks(EnvironmentDto environment) {
    if (environment != null) {
      RecipeDto recipe = environment.getRecipe();
      if (recipe != null) {
        if ("dockerfile".equals(recipe.getType())) {
          String location = recipe.getLocation();
          if (location != null && location.startsWith(apiEndpoint)) {
            recipe.setLocation(location.substring(apiEndpoint.length()));
          }
        }
      }
    }
  }

  private WorkspaceImpl doUpdate(String id, Workspace update)
      throws BadRequestException, ConflictException, NotFoundException, ServerException {
    try {
      return workspaceManager.updateWorkspace(id, update);
    } catch (ValidationException x) {
      throw new BadRequestException(x.getMessage());
    }
  }

  private List<WorkspaceDto> asDtosWithLinks(List<WorkspaceImpl> workspaces)
      throws ServerException {
    List<WorkspaceDto> result = new ArrayList<>();
    for (WorkspaceImpl workspace : workspaces) {
      result.add(
          asDto(workspace).withLinks(linksGenerator.genLinks(workspace, getServiceContext())));
    }
    return result;
  }

  private WorkspaceDto asDtoWithLinksAndToken(WorkspaceImpl workspace) throws ServerException {
    WorkspaceDto workspaceDto =
        asDto(workspace).withLinks(linksGenerator.genLinks(workspace, getServiceContext()));

    RuntimeDto runtimeDto = workspaceDto.getRuntime();
    if (runtimeDto != null) {
      try {
        runtimeDto.setMachineToken(machineTokenProvider.getToken(workspace.getId()));
      } catch (MachineAccessForbidden e) {
        // set runtime to null since user doesn't have the required permissions
        workspaceDto.setRuntime(null);
      } catch (MachineTokenException e) {
        throw new ServerException(e.getMessage(), e);
      }
    }

    return workspaceDto;
  }

  private WorkspaceDto filterServers(WorkspaceDto workspace, boolean includeInternal) {
    // no runtime - nothing to filter
    if (workspace.getRuntime() == null) {
      return workspace;
    }
    // if it is needed to include internal there is nothing to filter
    if (includeInternal) {
      return workspace;
    }
    for (MachineDto machine : workspace.getRuntime().getMachines().values()) {
      Map<String, ServerDto> filteredServers = new HashMap<>();
      machine
          .getServers()
          .forEach(
              (name, server) -> {
                if (!ServerConfig.isInternal(server.getAttributes())) {
                  filteredServers.put(name, server);
                }
              });
      machine.withServers(filteredServers);
    }

    return workspace;
  }
}
