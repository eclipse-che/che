/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

import com.google.common.collect.Maps;

import org.eclipse.che.api.agent.server.WsAgentHealthChecker;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Named;
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
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_CREATE_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_BY_NAMESPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACES;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Defines Workspace REST API.
 *
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
@Api(value = "/workspace", description = "Workspace REST API")
@Path("/workspace")
public class WorkspaceService extends Service {

    private final WorkspaceManager              workspaceManager;
    private final WorkspaceValidator            validator;
    private final WsAgentHealthChecker          agentHealthChecker;
    private final WorkspaceServiceLinksInjector linksInjector;
    private final String                        apiEndpoint;

    @Context
    private SecurityContext securityContext;

    @Inject
    public WorkspaceService(@Named("che.api") String apiEndpoint,
                            WorkspaceManager workspaceManager,
                            WorkspaceValidator validator,
                            WsAgentHealthChecker agentHealthChecker,
                            WorkspaceServiceLinksInjector workspaceServiceLinksInjector) {
        this.apiEndpoint = apiEndpoint;
        this.workspaceManager = workspaceManager;
        this.validator = validator;
        this.agentHealthChecker = agentHealthChecker;
        this.linksInjector = workspaceServiceLinksInjector;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_CREATE_WORKSPACE)
    @ApiOperation(value = "Create a new workspace based on the configuration",
                  notes = "This operation can be performed only by authorized user," +
                          "this user will be the owner of the created workspace",
                  response = WorkspaceConfigDto.class)
    @ApiResponses({@ApiResponse(code = 201, message = "The workspace successfully created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to create a new workspace"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the workspace creation" +
                                                      "(e.g. The workspace with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response create(@ApiParam(value = "The configuration to create the new workspace", required = true)
                           WorkspaceConfigDto config,
                           @ApiParam(value = "Workspace attribute defined in 'attrName:attrValue' format. " +
                                             "The first ':' is considered as attribute name and value separator",
                                     examples = @Example({@ExampleProperty("stackId:stack123"),
                                                          @ExampleProperty("attrName:value-with:colon")}))
                           @QueryParam("attribute")
                           List<String> attrsList,
                           @ApiParam("If true then the workspace will be immediately " +
                                     "started after it is successfully created")
                           @QueryParam("start-after-create")
                           @DefaultValue("false")
                           Boolean startAfterCreate,
                           @ApiParam("Namespace where workspace should be created")
                           @QueryParam("namespace")
                           String namespace) throws ConflictException,
                                                    ServerException,
                                                    BadRequestException,
                                                    ForbiddenException,
                                                    NotFoundException {
        requiredNotNull(config, "Workspace configuration");
        final Map<String, String> attributes = parseAttrs(attrsList);
        validator.validateAttributes(attributes);
        validator.validateConfig(config);
        relativizeRecipeLinks(config);
        if (namespace == null) {
            namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
        }
        final WorkspaceImpl workspace = workspaceManager.createWorkspace(config,
                                                                         namespace,
                                                                         attributes);
        if (startAfterCreate) {
            workspaceManager.startWorkspace(workspace.getId(), null, false);
        }
        return Response.status(201)
                       .entity(linksInjector.injectLinks(asDto(workspace), getServiceContext()))
                       .build();
    }

    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get the workspace by the composite key",
                  notes = "Composite key can be just workspace ID or in the " +
                          "namespace:workspace_name form, where namespace is optional (e.g :workspace_name is valid key too.")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested workspace entity"),
                   @ApiResponse(code = 404, message = "The workspace with specified id does not exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto getByKey(@ApiParam(value = "Composite key",
                                           examples = @Example({@ExampleProperty("workspace12345678"),
                                                                @ExampleProperty("namespace:workspace_name"),
                                                                @ExampleProperty(":workspace_name")}))
                                 @PathParam("key") String key) throws NotFoundException,
                                                                      ServerException,
                                                                      ForbiddenException,
                                                                      BadRequestException {
        validateKey(key);
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        return linksInjector.injectLinks(asDto(workspace), getServiceContext());
    }

    @GET
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_GET_WORKSPACES)
    @ApiOperation(value = "Get workspaces which user can read",
                  notes = "This operation can be performed only by authorized user",
                  response = WorkspaceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspaces successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")})
    public List<WorkspaceDto> getWorkspaces(@ApiParam("The number of the items to skip")
                                            @DefaultValue("0")
                                            @QueryParam("skipCount")
                                            Integer skipCount,
                                            @ApiParam("The limit of the items in the response, default is 30")
                                            @DefaultValue("30")
                                            @QueryParam("maxItems")
                                            Integer maxItems,
                                            @ApiParam("Workspace status")
                                            @QueryParam("status")
                                            String status) throws ServerException, BadRequestException {
        //TODO add maxItems & skipCount to manager
        return workspaceManager.getWorkspaces(EnvironmentContext.getCurrent().getSubject().getUserId())
                               .stream()
                               .filter(ws -> status == null || status.equalsIgnoreCase(ws.getStatus().toString()))
                               .map(workspace -> linksInjector.injectLinks(asDto(workspace), getServiceContext()))
                               .collect(toList());
    }

    @GET
    @Path("/namespace/{namespace}")
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_GET_BY_NAMESPACE)
    @ApiOperation(value = "Get workspaces by given namespace",
                  notes = "This operation can be performed only by authorized user",
                  response = WorkspaceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspaces successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")})
    public List<WorkspaceDto> getByNamespace(@ApiParam("Workspace status")
                                             @QueryParam("status")
                                             String status,
                                             @ApiParam("The namespace")
                                             @PathParam("namespace")
                                             String namespace) throws ServerException, BadRequestException {
        return workspaceManager.getByNamespace(namespace)
                               .stream()
                               .filter(ws -> status == null || status.equalsIgnoreCase(ws.getStatus().toString()))
                               .map(workspace -> linksInjector.injectLinks(asDto(workspace), getServiceContext()))
                               .collect(toList());
    }

    @PUT
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Update the workspace by replacing all the existing data with update",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during workspace update" +
                                                      "(e.g. Workspace with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto update(@ApiParam("The workspace id")
                               @PathParam("id")
                               String id,
                               @ApiParam(value = "The workspace update", required = true)
                               WorkspaceDto update) throws BadRequestException,
                                                           ServerException,
                                                           ForbiddenException,
                                                           NotFoundException,
                                                           ConflictException {
        requiredNotNull(update, "Workspace configuration");
        validator.validateWorkspace(update);
        relativizeRecipeLinks(update.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(id, update)), getServiceContext());
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Removes the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 204, message = "The workspace successfully removed"),
                   @ApiResponse(code = 403, message = "The user does not have access to remove the workspace"),
                   @ApiResponse(code = 404, message = "The workspace doesn't exist"),
                   @ApiResponse(code = 409, message = "The workspace is not stopped(has runtime)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void delete(@ApiParam("The workspace id") @PathParam("id") String id) throws BadRequestException,
                                                                                        ServerException,
                                                                                        NotFoundException,
                                                                                        ConflictException,
                                                                                        ForbiddenException {
        workspaceManager.removeSnapshots(id);
        workspaceManager.removeWorkspace(id);
    }

    @POST
    @Path("/{id}/runtime")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Start the workspace by the id",
                  notes = "This operation can be performed only by the workspace owner." +
                          "The workspace starts asynchronously")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace is starting"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner." +
                                                      "The operation is not allowed for the user"),
                   @ApiResponse(code = 409, message = "Any conflict occurs during the workspace start"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto startById(@ApiParam("The workspace id")
                                  @PathParam("id")
                                  String workspaceId,
                                  @ApiParam("The name of the workspace environment that should be used for start")
                                  @QueryParam("environment")
                                  String envName,
                                  @ApiParam("Restore workspace from snapshot")
                                  @QueryParam("restore")
                                  Boolean restore) throws ServerException,
                                                          BadRequestException,
                                                          NotFoundException,
                                                          ForbiddenException,
                                                          ConflictException {
        return linksInjector.injectLinks(asDto(workspaceManager.startWorkspace(workspaceId, envName, restore)),
                                         getServiceContext());
    }

    @POST
    @Path("/runtime")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Start the temporary workspace from the given configuration",
                  notes = "This operation can be performed only by the authorized user or temp user." +
                          "The workspace starts synchronously")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace is starting"),
                   @ApiResponse(code = 400, message = "The update config is not valid"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner" +
                                                      "The operation is not allowed for the user"),
                   @ApiResponse(code = 409, message = "Any conflict occurs during the workspace start" +
                                                      "(e.g. workspace with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto startFromConfig(@ApiParam(value = "The configuration to start the workspace from", required = true)
                                        WorkspaceConfigDto config,
                                        @ApiParam("Weather this workspace is temporary or not")
                                        @QueryParam("temporary")
                                        Boolean isTemporary,
                                        @ApiParam("Namespace where workspace should be created")
                                        @QueryParam("namespace")
                                        String namespace) throws BadRequestException,
                                                                 ForbiddenException,
                                                                 NotFoundException,
                                                                 ServerException,
                                                                 ConflictException {
        requiredNotNull(config, "Workspace configuration");
        validator.validateConfig(config);
        relativizeRecipeLinks(config);
        if (namespace == null) {
            namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
        }
        return linksInjector.injectLinks(asDto(workspaceManager.startWorkspace(config,
                                                                               namespace,
                                                                               firstNonNull(isTemporary, false))), getServiceContext());
    }

    @DELETE
    @Path("/{id}/runtime")
    @ApiOperation(value = "Stop the workspace",
                  notes = "This operation can be performed only by the workspace owner." +
                          "The workspace stops asynchronously")
    @ApiResponses({@ApiResponse(code = 204, message = "The workspace is stopping"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void stop(@ApiParam("The workspace id") @PathParam("id") String id,
                     @ApiParam("Whether to snapshot workspace before stopping it")
                     @QueryParam("create-snapshot") Boolean createSnapshot) throws ForbiddenException,
                                                                                   NotFoundException,
                                                                                   ServerException,
                                                                                   ConflictException {
        workspaceManager.stopWorkspace(id, createSnapshot);
    }

    @POST
    @Path("/{id}/snapshot")
    @ApiOperation(value = "Create a snapshot from the workspace",
                  notes = "This operation can be performed only by the workspace owner.")
    @ApiResponses({@ApiResponse(code = 200, message = "The snapshot successfully created"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist."),
                   @ApiResponse(code = 403, message = "The user is not workspace owner. " +
                                                      "The operation is not allowed for the user"),
                   @ApiResponse(code = 409, message = "Any conflict occurs during the snapshot creation"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void createSnapshot(@ApiParam("The workspace id") @PathParam("id") String workspaceId) throws BadRequestException,
                                                                                                         ForbiddenException,
                                                                                                         NotFoundException,
                                                                                                         ServerException,
                                                                                                         ConflictException {
        workspaceManager.createSnapshot(workspaceId);
    }

    @GET
    @Path("/{id}/snapshot")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get the snapshot by the id",
                  notes = "This operation can be performed only by the workspace owner",
                  response = SnapshotDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Snapshots successfully fetched"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist." +
                                                      "The snapshot doesn't exist for the workspace"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<SnapshotDto> getSnapshot(@ApiParam("The id of the workspace") @PathParam("id") String workspaceId)
            throws ServerException,
                   BadRequestException,
                   NotFoundException,
                   ForbiddenException {

        return workspaceManager.getSnapshot(workspaceId)
                               .stream()
                               .map(DtoConverter::asDto)
                               .map(snapshotDto -> linksInjector.injectLinks(snapshotDto, getServiceContext()))
                               .collect(toList());
    }

    @POST
    @Path("/{id}/command")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Update the workspace by adding a new command to it",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 409, message = "The command with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto addCommand(@ApiParam("The workspace id")
                                   @PathParam("id")
                                   String id,
                                   @ApiParam(value = "The new workspace command", required = true)
                                   CommandDto newCommand) throws ServerException,
                                                                 BadRequestException,
                                                                 NotFoundException,
                                                                 ConflictException,
                                                                 ForbiddenException {
        requiredNotNull(newCommand, "Command");
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        workspace.getConfig().getCommands().add(new CommandImpl(newCommand));
        validator.validateConfig(workspace.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(workspace.getId(), workspace)), getServiceContext());
    }

    @PUT
    @Path("/{id}/command/{name}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Update the workspace command by replacing the command with a new one",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The command successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
                   @ApiResponse(code = 404, message = "The workspace or the command not found"),
                   @ApiResponse(code = 409, message = "The Command with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto updateCommand(@ApiParam("The workspace id")
                                      @PathParam("id") String id,
                                      @ApiParam("The name of the command")
                                      @PathParam("name")
                                      String cmdName,
                                      @ApiParam(value = "The command update", required = true)
                                      CommandDto update) throws ServerException,
                                                                BadRequestException,
                                                                NotFoundException,
                                                                ConflictException,
                                                                ForbiddenException {
        requiredNotNull(update, "Command update");
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        final List<CommandImpl> commands = workspace.getConfig().getCommands();
        if (!commands.removeIf(cmd -> cmd.getName().equals(cmdName))) {
            throw new NotFoundException(format("Workspace '%s' doesn't contain command '%s'", id, cmdName));
        }
        commands.add(new CommandImpl(update));
        validator.validateConfig(workspace.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(workspace.getId(), workspace)), getServiceContext());
    }

    @DELETE
    @Path("/{id}/command/{name}")
    @ApiOperation(value = "Remove the command from the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 204, message = "The command successfully removed"),
                   @ApiResponse(code = 403, message = "The user does not have access delete the command"),
                   @ApiResponse(code = 404, message = "The workspace or the command not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void deleteCommand(@ApiParam("The id of the workspace")
                              @PathParam("id")
                              String id,
                              @ApiParam("The name of the command to remove")
                              @PathParam("name")
                              String commandName) throws ServerException,
                                                         BadRequestException,
                                                         NotFoundException,
                                                         ConflictException,
                                                         ForbiddenException {
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        if (workspace.getConfig().getCommands().removeIf(command -> command.getName().equals(commandName))) {
            workspaceManager.updateWorkspace(id, workspace);
        } else {
            throw new NotFoundException(format("Command with name '%s' was not found in workspace '%s'",
                                               commandName,
                                               workspace.getConfig().getName()));
        }
    }

    @POST
    @Path("/{id}/environment")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Add a new environment to the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to add the environment"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 409, message = "Environment with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto addEnvironment(@ApiParam("The workspace id")
                                       @PathParam("id")
                                       String id,
                                       @ApiParam(value = "The new environment", required = true)
                                       EnvironmentDto newEnvironment,
                                       @ApiParam(value = "The name of the environment", required = true)
                                       @QueryParam("name")
                                       String envName) throws ServerException,
                                                              BadRequestException,
                                                              NotFoundException,
                                                              ConflictException,
                                                              ForbiddenException {
        requiredNotNull(newEnvironment, "New environment");
        requiredNotNull(envName, "New environment name");
        relativizeRecipeLinks(newEnvironment);
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        workspace.getConfig().getEnvironments().put(envName, new EnvironmentImpl(newEnvironment));
        validator.validateConfig(workspace.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace)), getServiceContext());
    }

    @PUT
    @Path("/{id}/environment/{name}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Update the workspace environment by replacing it with a new one",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The environment successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the environment"),
                   @ApiResponse(code = 404, message = "The workspace or the environment not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto updateEnvironment(@ApiParam("The workspace id")
                                          @PathParam("id")
                                          String id,
                                          @ApiParam("The name of the environment")
                                          @PathParam("name")
                                          String envName,
                                          @ApiParam(value = "The environment update", required = true)
                                          EnvironmentDto update) throws ServerException,
                                                                        BadRequestException,
                                                                        NotFoundException,
                                                                        ConflictException,
                                                                        ForbiddenException {
        requiredNotNull(update, "Environment description");
        relativizeRecipeLinks(update);
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        EnvironmentImpl previous = workspace.getConfig().getEnvironments().put(envName, new EnvironmentImpl(update));
        if (previous == null) {
            throw new NotFoundException(format("Workspace '%s' doesn't contain environment '%s'", id, envName));
        }
        validator.validateConfig(workspace.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace)), getServiceContext());
    }

    @DELETE
    @Path("/{id}/environment/{name}")
    @ApiOperation(value = "Remove the environment from the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 204, message = "The environment successfully removed"),
                   @ApiResponse(code = 403, message = "The user does not have access remove the environment"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void deleteEnvironment(@ApiParam("The workspace id")
                                  @PathParam("id")
                                  String id,
                                  @ApiParam("The name of the environment")
                                  @PathParam("name")
                                  String envName) throws ServerException,
                                                         BadRequestException,
                                                         NotFoundException,
                                                         ConflictException,
                                                         ForbiddenException {
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        if (workspace.getConfig().getEnvironments().remove(envName) != null) {
            workspaceManager.updateWorkspace(id, workspace);
        }
    }

    @POST
    @Path("/{id}/project")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Adds a new project to the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The project successfully added to the workspace"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to add the project"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 409, message = "Any conflict error occurs"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto addProject(@ApiParam("The workspace id")
                                   @PathParam("id")
                                   String id,
                                   @ApiParam(value = "The new project", required = true)
                                   ProjectConfigDto newProject) throws ServerException,
                                                                       BadRequestException,
                                                                       NotFoundException,
                                                                       ConflictException,
                                                                       ForbiddenException {
        requiredNotNull(newProject, "New project config");
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        workspace.getConfig().getProjects().add(new ProjectConfigImpl(newProject));
        validator.validateConfig(workspace.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace)), getServiceContext());
    }

    @PUT
    @Path("/{id}/project/{path:.*}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Update the workspace project by replacing it with a new one",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The project successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the project"),
                   @ApiResponse(code = 404, message = "The workspace or the project not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WorkspaceDto updateProject(@ApiParam("The workspace id")
                                      @PathParam("id")
                                      String id,
                                      @ApiParam("The path to the project")
                                      @PathParam("path")
                                      String path,
                                      @ApiParam(value = "The project update", required = true)
                                      ProjectConfigDto update) throws ServerException,
                                                                      BadRequestException,
                                                                      NotFoundException,
                                                                      ConflictException,
                                                                      ForbiddenException {
        requiredNotNull(update, "Project config");
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        final List<ProjectConfigImpl> projects = workspace.getConfig().getProjects();
        final String normalizedPath = path.startsWith("/") ? path : '/' + path;
        if (!projects.removeIf(project -> project.getPath().equals(normalizedPath))) {
            throw new NotFoundException(format("Workspace '%s' doesn't contain project with path '%s'",
                                               id,
                                               normalizedPath));
        }
        projects.add(new ProjectConfigImpl(update));
        validator.validateConfig(workspace.getConfig());
        return linksInjector.injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace)), getServiceContext());
    }

    @DELETE
    @Path("/{id}/project/{path:.*}")
    @ApiOperation(value = "Remove the project from the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 204, message = "The project successfully removed"),
                   @ApiResponse(code = 403, message = "The user does not have access remove the project"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void deleteProject(@ApiParam("The workspace id")
                              @PathParam("id")
                              String id,
                              @ApiParam("The name of the project to remove")
                              @PathParam("path")
                              String path) throws ServerException,
                                                  BadRequestException,
                                                  NotFoundException,
                                                  ConflictException,
                                                  ForbiddenException {
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        final String normalizedPath = path.startsWith("/") ? path : '/' + path;
        if (workspace.getConfig().getProjects().removeIf(project -> project.getPath().equals(normalizedPath))) {
            workspaceManager.updateWorkspace(id, workspace);
        }
    }

    @GET
    @Path("/{id}/check")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get state of the workspace agent by the workspace id")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested workspace entity"),
                   @ApiResponse(code = 404, message = "The workspace with specified id does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public WsAgentHealthStateDto checkAgentHealth(@ApiParam(value = "Workspace id")
                                                  @PathParam("id") String key) throws NotFoundException, ServerException {
        final WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        if (WorkspaceStatus.RUNNING != workspace.getStatus()) {
            return newDto(WsAgentHealthStateDto.class).withWorkspaceStatus(workspace.getStatus());
        }

        final MachineImpl devMachine = workspace.getRuntime().getDevMachine();
        if (devMachine == null) {
            return newDto(WsAgentHealthStateDto.class)
                    .withWorkspaceStatus(workspace.getStatus())
                    .withCode(NOT_FOUND.getStatusCode())
                    .withReason("Workspace Agent isn't available if Dev machine isn't RUNNING");
        }

        final WsAgentHealthStateDto check = agentHealthChecker.check(devMachine);
        check.setWorkspaceStatus(workspace.getStatus());
        return check;
    }

    private static Map<String, String> parseAttrs(List<String> attributes) throws BadRequestException {
        if (attributes == null) {
            return emptyMap();
        }
        final Map<String, String> res = Maps.newHashMapWithExpectedSize(attributes.size());
        for (String attribute : attributes) {
            final int colonIdx = attribute.indexOf(':');
            if (colonIdx == -1) {
                throw new BadRequestException("Attribute '" + attribute + "' is not valid, " +
                                              "it should contain name and value separated with colon. " +
                                              "For example: attributeName:attributeValue");
            }
            res.put(attribute.substring(0, colonIdx), attribute.substring(colonIdx + 1));
        }
        return res;
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(subject + " required");
        }
    }

    /*
     * Validate composite key.
     *
     */
    private void validateKey(String key) throws BadRequestException {
        String[] parts = key.split(":", -1); // -1 is to prevent skipping trailing part
        switch (parts.length) {
            case 1: {
                return; // consider it's id
            }
            case 2: {
                if (parts[1].isEmpty()) {
                    throw new BadRequestException("Wrong composite key format - workspace name required to be set.");
                }
                break;
            }
            default: {
                throw new BadRequestException(format("Wrong composite key %s. Format should be 'username:workspace_name'. ", key));
            }
        }
    }

    private void relativizeRecipeLinks(WorkspaceConfigDto config) {
        config.getEnvironments().values().forEach(this::relativizeRecipeLinks);
    }

    private void relativizeRecipeLinks(EnvironmentDto environment) {
        EnvironmentRecipeDto recipe =  environment.getRecipe();
        if (recipe.getType().equals("dockerfile")) {
            String location = recipe.getLocation();
            if (location != null && location.startsWith(apiEndpoint)) {
                recipe.setLocation(location.substring(apiEndpoint.length()));
            }
        }
    }
}
