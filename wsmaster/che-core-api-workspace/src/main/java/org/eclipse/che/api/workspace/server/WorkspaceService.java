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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.permission.PermissionManager;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineService;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.GET_ALL_USER_WORKSPACES;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_CREATE_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_RUNTIME_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACES;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_REMOVE_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_START_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.START_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.STOP_WORKSPACE;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;
import static org.eclipse.che.dto.server.DtoFactory.cloneDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Defines Workspace REST API.
 *
 * @author Yevhenii Voevodin
 */
@Api(value = "/workspace", description = "Workspace REST API")
@Path("/workspace")
public class WorkspaceService extends Service {

    private final WorkspaceManager  workspaceManager;
    private final PermissionManager permissionManager;
    private final MachineManager    machineManager;
    //TODO: we need keep IDE context in some property to have possibility configure it because context is different in Che and Hosted packaging
    //TODO: not good solution do it here but critical for this task  https://jira.codenvycorp.com/browse/IDEX-3619
    private final String            ideContext;

    @Context
    private SecurityContext securityContext;

    @Inject
    public WorkspaceService(WorkspaceManager workspaceManager,
                            MachineManager machineManager,
                            @Named("service.workspace.permission_manager") PermissionManager permissionManager,
                            @Named("che.ide.context") String ideContext) {
        this.workspaceManager = workspaceManager;
        this.machineManager = machineManager;
        this.permissionManager = permissionManager;
        this.ideContext = ideContext;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @GenerateLink(rel = LINK_REL_CREATE_WORKSPACE)
    @ApiOperation(value = "Create a new workspace based on the configuration",
                  notes = "This operation can be performed only by authorized user," +
                          "this user will be the owner of the created workspace",
                  response = UsersWorkspaceDto.class)
    @ApiResponses({@ApiResponse(code = 201, message = "The workspace successfully created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to create a new workspace"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the workspace creation" +
                                                      "(e.g. The workspace with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response create(@ApiParam(value = "The configuration to create the new workspace", required = true)
                           WorkspaceConfigDto config,
                           @ApiParam("The account id related to this operation")
                           @QueryParam("account")
                           String accountId) throws ConflictException,
                                                    ServerException,
                                                    BadRequestException,
                                                    ForbiddenException,
                                                    NotFoundException {
        requiredNotNull(config, "Workspace configuration required");
        return Response.status(201)
                       .entity(injectLinks(asDto(workspaceManager.createWorkspace(config, getCurrentUserId(), accountId))))
                       .build();
    }

    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Get the workspace by the composite key",
                  notes = "Composite key can be just workspace ID or in the " +
                          "username:workspace_name form, where username is optional (e.g :workspace_name is valid key too.")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested workspace entity"),
                   @ApiResponse(code = 404, message = "The workspace by specified key does not exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto getByKey(@ApiParam(value = "Composite key", examples = @Example({@ExampleProperty("workspace12345678"),
                                                                                              @ExampleProperty("username:workspace_name"),
                                                                                              @ExampleProperty(":workspace_name")})) @PathParam("key") String key)
            throws NotFoundException,
                   ServerException,
                   ForbiddenException,
                   BadRequestException {
        validateKey(key);
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        ensureUserIsWorkspaceOwner(workspace);
        return injectLinks(asDto(workspace));
    }

    @GET
    @Path("/name/{name}")
    @RolesAllowed("user")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get the workspace by the name from the workspaces owned by the current user",
                  notes = "This operation can be performed only by the authorized user")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested workspace entity"),
                   @ApiResponse(code = 404, message = "The workspace with specified name does not exist for current user "),
                   @ApiResponse(code = 403, message = "The user is not the workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    @Deprecated
    public UsersWorkspaceDto getByName(@ApiParam("The workspace name") @PathParam("name") String name) throws ServerException,
                                                                                                              BadRequestException,
                                                                                                              NotFoundException,
                                                                                                              ForbiddenException {
        return injectLinks(asDto(workspaceManager.getWorkspace(name, getCurrentUserId())));
    }

    @GET
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @GenerateLink(rel = LINK_REL_GET_WORKSPACES)
    @ApiOperation(value = "Get the workspaces owned by the current user",
                  notes = "This operation can be performed only by authorized user",
                  response = UsersWorkspaceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspaces successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")})
    public List<UsersWorkspaceDto> getWorkspaces(@ApiParam("The number of the items to skip")
                                                 @DefaultValue("0")
                                                 @QueryParam("skipCount")
                                                 Integer skipCount,
                                                 @ApiParam("The limit of the items in the response, default is 30")
                                                 @DefaultValue("30")
                                                 @QueryParam("maxItems")
                                                 Integer maxItems) throws ServerException, BadRequestException {
        //TODO add maxItems & skipCount to manager
        return workspaceManager.getWorkspaces(getCurrentUserId())
                               .stream()
                               .map(workspace -> injectLinks(asDto(workspace)))
                               .collect(toList());
    }

    @PUT
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Update the workspace by replacing all the existing data with update",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during workspace update" +
                                                      "(e.g. Workspace with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto update(@ApiParam("The workspace id")
                                    @PathParam("id")
                                    String id,
                                    @ApiParam(value = "The workspace update", required = true)
                                    WorkspaceConfigDto update) throws BadRequestException,
                                                                      ServerException,
                                                                      ForbiddenException,
                                                                      NotFoundException,
                                                                      ConflictException {
        requiredNotNull(update, "Workspace configuration");
        ensureUserIsWorkspaceOwner(id);
        return injectLinks(asDto(workspaceManager.updateWorkspace(id, update)));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("user")
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
        ensureUserIsWorkspaceOwner(id);
        workspaceManager.removeWorkspace(id);
    }

    @GET
    @Path("/{id}/runtime")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Get the runtime workspace by the id",
                  notes = "This operation can be performed only by the authorized user")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested runtime workspace entity"),
                   @ApiResponse(code = 404, message = "The runtime workspace with the specified id does not exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public RuntimeWorkspaceDto getRuntimeWorkspaceById(@ApiParam("The workspace id")
                                                       @PathParam("id")
                                                       String id) throws ServerException,
                                                                         BadRequestException,
                                                                         NotFoundException,
                                                                         ForbiddenException {
        final RuntimeWorkspaceImpl runtimeWorkspace = workspaceManager.getRuntimeWorkspace(id);
        ensureUserIsWorkspaceOwner(runtimeWorkspace);
        return injectLinks(asDto(runtimeWorkspace));
    }

    @GET
    @Path("/runtime")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Get the runtime workspaces owned by current user",
                  notes = "This operation can be performed only by authorized user",
                  response = RuntimeWorkspaceDto.class,
                  responseContainer = "List")
    @ApiResponses(@ApiResponse(code = 200, message = "Workspaces successfully fetched"))
    public List<RuntimeWorkspaceDto> getRuntimeWorkspaces(@ApiParam("The number of the items to skip")
                                                          @DefaultValue("0")
                                                          @QueryParam("skipCount")
                                                          Integer skipCount,
                                                          @ApiParam("The limit of the items in the response, default is 30")
                                                          @DefaultValue("30")
                                                          @QueryParam("maxItems")
                                                          Integer maxItems) throws BadRequestException {
        //TODO add maxItems & skipCount to manager
        return workspaceManager.getRuntimeWorkspaces(getCurrentUserId())
                               .stream()
                               .map(workspace -> injectLinks(asDto(workspace)))
                               .collect(toList());
    }

    @POST
    @Path("/{id}/runtime")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Start the workspace by the id",
                  notes = "This operation can be performed only by the workspace owner." +
                          "The workspace starts asynchronously")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace is starting"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner." +
                                                      "The operation is not allowed for the user"),
                   @ApiResponse(code = 409, message = "Any conflict occurs during the workspace start"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto startById(@ApiParam("The workspace id")
                                       @PathParam("id")
                                       String workspaceId,
                                       @ApiParam("The name of the workspace environment that should be used for start")
                                       @QueryParam("environment")
                                       String envName,
                                       @ApiParam("The account id related to this operation")
                                       @QueryParam("accountId")
                                       String accountId) throws ServerException,
                                                                BadRequestException,
                                                                NotFoundException,
                                                                ForbiddenException,
                                                                ConflictException {
        ensureUserIsWorkspaceOwner(workspaceId);

        final Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
        params.put("accountId", accountId);
        params.put("workspaceId", workspaceId);
        permissionManager.checkPermission(START_WORKSPACE, getCurrentUserId(), params);

        return injectLinks(asDto(workspaceManager.startWorkspaceById(workspaceId, envName, accountId)));
    }

    @POST
    @Path("/name/{name}/runtime")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Start workspace by name",
                  notes = "This operation can be performed only by the authorized user." +
                          "The workspace starts asynchronously")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace is starting"),
                   @ApiResponse(code = 400, message = "The workspace name is not valid"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner." +
                                                      "The operation is not allowed for the user"),
                   @ApiResponse(code = 409, message = "Any conflict occurs during the workspace start"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto startByName(@ApiParam("The name of the workspace to start")
                                         @PathParam("name")
                                         String name,
                                         @ApiParam("The name of the workspace environment that should be used for start")
                                         @QueryParam("environment")
                                         String envName,
                                         @ApiParam("The account id related to this operation")
                                         @QueryParam("accountId")
                                         String accountId) throws ServerException,
                                                                  BadRequestException,
                                                                  NotFoundException,
                                                                  ForbiddenException,
                                                                  ConflictException {
        final UsersWorkspace workspace = workspaceManager.getWorkspace(name, getCurrentUserId());
        ensureUserIsWorkspaceOwner(workspace);

        final Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
        params.put("accountId", accountId);
        params.put("workspaceId", workspace.getId());
        permissionManager.checkPermission(START_WORKSPACE, getCurrentUserId(), params);

        return injectLinks(asDto(workspaceManager.startWorkspaceByName(name, getCurrentUserId(), envName, accountId)));
    }

    @POST
    @Path("/runtime")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"user", "temp-user"})
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
    public RuntimeWorkspaceDto startTemporary(@ApiParam(value = "The configuration to start the workspace from", required = true)
                                              WorkspaceConfigDto cfg,
                                              @ApiParam("The account id related to this operation")
                                              @QueryParam("account")
                                              String accountId) throws BadRequestException,
                                                                       ForbiddenException,
                                                                       NotFoundException,
                                                                       ServerException,
                                                                       ConflictException {
        requiredNotNull(cfg, "Workspace configuration");
        permissionManager.checkPermission(START_WORKSPACE, getCurrentUserId(), "accountId", accountId);
        return injectLinks(asDto(workspaceManager.startTemporaryWorkspace(cfg, accountId)));
    }

    @POST
    @Path("/{id}/runtime/snapshot")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Recover the workspace by the id from the snapshot",
                  notes = "This operation can be performed only by the workspace owner." +
                          "The workspace recovers asynchronously")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace is starting"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist." +
                                                      "The snapshot from this workspace doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner. " +
                                                      "The operation is not allowed for the user"),
                   @ApiResponse(code = 409, message = "Any conflict occurs during the workspace start"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto recoverWorkspace(@ApiParam("The workspace id")
                                              @PathParam("id")
                                              String workspaceId,
                                              @ApiParam("The name of the workspace environment to recover from")
                                              @QueryParam("environment")
                                              String envName,
                                              @ApiParam("The account id related to this operation")
                                              @QueryParam("accountId")
                                              String accountId) throws BadRequestException,
                                                                       ForbiddenException,
                                                                       NotFoundException,
                                                                       ServerException,
                                                                       ConflictException {
        ensureUserIsWorkspaceOwner(workspaceId);

        final Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
        params.put("accountId", accountId);
        params.put("workspaceId", workspaceId);
        permissionManager.checkPermission(START_WORKSPACE, getCurrentUserId(), params);

        return injectLinks(asDto(workspaceManager.recoverWorkspace(workspaceId, envName, accountId)));
    }

    @DELETE
    @Path("/{id}/runtime")
    @RolesAllowed("user")
    @ApiOperation(value = "Stop the workspace",
                  notes = "This operation can be performed only by the workspace owner." +
                          "The workspace stops asynchronously")
    @ApiResponses({@ApiResponse(code = 204, message = "The workspace is stopping"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void stop(@ApiParam("The workspace id") @PathParam("id") String id) throws BadRequestException,
                                                                                      ForbiddenException,
                                                                                      NotFoundException,
                                                                                      ServerException {
        ensureUserIsWorkspaceOwner(id);
        workspaceManager.stopWorkspace(id);
    }

    @POST
    @Path("/{id}/snapshot")
    @RolesAllowed("user")
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
                                                                                                         ServerException {
        ensureUserIsWorkspaceOwner(workspaceId);

        workspaceManager.createSnapshot(workspaceId);
    }

    @GET
    @Path("/{id}/snapshot")
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Get the snapshot by the id",
                  notes = "This operation can be performed only by the workspace owner",
                  response = SnapshotDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Snapshots successfully fetched"),
                   @ApiResponse(code = 404, message = "The workspace with specified id doesn't exist." +
                                                      "The snapshot doesn't exist for the workspace"),
                   @ApiResponse(code = 403, message = "The user is not workspace owner"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<SnapshotDto> getSnapshot(@ApiParam("The id of the workspace") @PathParam("id") String workspaceId) throws ServerException,
                                                                                                                          BadRequestException,
                                                                                                                          NotFoundException,
                                                                                                                          ForbiddenException {
        ensureUserIsWorkspaceOwner(workspaceId);

        return workspaceManager.getSnapshot(workspaceId)
                               .stream()
                               .map(DtoConverter::asDto)
                               .map(this::injectLinks)
                               .collect(toList());
    }

    @POST
    @Path("/{id}/command")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Update the workspace by adding a new command to it",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 409, message = "The command with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto addCommand(@ApiParam("The workspace id")
                                        @PathParam("id")
                                        String id,
                                        @ApiParam(value = "The new workspace command", required = true)
                                        CommandDto newCommand) throws ServerException,
                                                                      BadRequestException,
                                                                      NotFoundException,
                                                                      ConflictException,
                                                                      ForbiddenException {
        requiredNotNull(newCommand, "Command");
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        workspace.getConfig().getCommands().add(new CommandImpl(newCommand));
        return injectLinks(asDto(workspaceManager.updateWorkspace(workspace.getId(), workspace.getConfig())));
    }

    @PUT
    @Path("/{id}/command")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Update the workspace command by replacing the command with a new one",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The command successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the workspace"),
                   @ApiResponse(code = 404, message = "The workspace or the command not found"),
                   @ApiResponse(code = 409, message = "The Command with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto updateCommand(@ApiParam("The workspace id")
                                           @PathParam("id") String id,
                                           @ApiParam(value = "The command update", required = true)
                                           CommandDto update) throws ServerException,
                                                                     BadRequestException,
                                                                     NotFoundException,
                                                                     ConflictException,
                                                                     ForbiddenException {
        requiredNotNull(update, "Command update");
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        if (!workspace.getConfig().getCommands().removeIf(cmd -> cmd.getName().equals(update.getName()))) {
            throw new NotFoundException("Workspace " + id + " doesn't contain command " + update.getName());
        }
        workspace.getConfig().getCommands().add(new CommandImpl(update));
        return injectLinks(asDto(workspaceManager.updateWorkspace(workspace.getId(), workspace.getConfig())));
    }

    @DELETE
    @Path("/{id}/command/{name}")
    @RolesAllowed("user")
    @ApiOperation(value = "Remove the command from the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 204, message = "The command successfully removed"),
                   @ApiResponse(code = 403, message = "The user does not have access delete the command"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
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
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        if (workspace.getConfig().getCommands().removeIf(command -> command.getName().equals(commandName))) {
            workspaceManager.updateWorkspace(id, workspace.getConfig());
        }
    }

    @POST
    @Path("/{id}/environment")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Add a new environment to the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspace successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to add the environment"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 409, message = "Environment with such name already exists"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto addEnvironment(@ApiParam("The workspace id")
                                            @PathParam("id")
                                            String id,
                                            @ApiParam(value = "The new environment", required = true)
                                            EnvironmentDto newEnvironment) throws ServerException,
                                                                                  BadRequestException,
                                                                                  NotFoundException,
                                                                                  ConflictException,
                                                                                  ForbiddenException {
        requiredNotNull(newEnvironment, "New environment");
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        if (workspace.getConfig()
                     .getEnvironments()
                     .stream()
                     .anyMatch(env -> env.getName().equals(newEnvironment.getName()))) {
            throw new ConflictException("Environment '" + newEnvironment.getName() + "' already exists");
        }
        workspace.getConfig().getEnvironments().add(new EnvironmentImpl(newEnvironment));
        return injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace.getConfig())));
    }

    @PUT
    @Path("/{id}/environment")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Update the workspace environment by replacing it with a new one",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The environment successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the environment"),
                   @ApiResponse(code = 404, message = "The workspace or the environment not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto updateEnvironment(@ApiParam("The workspace id")
                                               @PathParam("id")
                                               String id,
                                               @ApiParam(value = "The environment update", required = true)
                                               EnvironmentDto update) throws ServerException,
                                                                             BadRequestException,
                                                                             NotFoundException,
                                                                             ConflictException,
                                                                             ForbiddenException {
        requiredNotNull(update, "Environment description");
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        if (!workspace.getConfig()
                      .getEnvironments()
                      .stream()
                      .anyMatch(env -> env.getName().equals(update.getName()))) {
            throw new NotFoundException("Workspace " + id + " doesn't contain environment " + update.getName());
        }
        workspace.getConfig().getEnvironments().add(new EnvironmentImpl(update));
        return injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace.getConfig())));
    }

    @DELETE
    @Path("/{id}/environment/{name}")
    @RolesAllowed("user")
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
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        Iterator<EnvironmentImpl> it = workspace.getConfig().getEnvironments().iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(envName)) {
                it.remove();
                workspaceManager.updateWorkspace(id, workspace.getConfig());
            }
        }
    }

    @POST
    @Path("/{id}/project")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Adds a new project to the workspace",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The project successfully added to the workspace"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to add the project"),
                   @ApiResponse(code = 404, message = "The workspace not found"),
                   @ApiResponse(code = 409, message = "Any conflict error occurs"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto addProject(@ApiParam("The workspace id")
                                        @PathParam("id")
                                        String id,
                                        @ApiParam(value = "The new project", required = true)
                                        ProjectConfigDto newProject) throws ServerException,
                                                                            BadRequestException,
                                                                            NotFoundException,
                                                                            ConflictException,
                                                                            ForbiddenException {
        requiredNotNull(newProject, "New project config");
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        workspace.getConfig().getProjects().add(new ProjectConfigImpl(newProject));
        return injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace.getConfig())));
    }

    @PUT
    @Path("/{id}/project")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Update the workspace project by replacing it with a new one",
                  notes = "This operation can be performed only by the workspace owner")
    @ApiResponses({@ApiResponse(code = 200, message = "The project successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to update the project"),
                   @ApiResponse(code = 404, message = "The workspace or the project not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public UsersWorkspaceDto updateProject(@ApiParam("The workspace id")
                                           @PathParam("id")
                                           String id,
                                           @ApiParam(value = "The project update", required = true)
                                           ProjectConfigDto update) throws ServerException,
                                                                           BadRequestException,
                                                                           NotFoundException,
                                                                           ConflictException,
                                                                           ForbiddenException {
        requiredNotNull(update, "Project config");
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        if (!workspace.getConfig().getProjects().removeIf(project -> project.getName().equals(update.getName()))) {
            throw new NotFoundException("Workspace " + id + " doesn't contain project " + update.getName());
        }
        workspace.getConfig().getProjects().add(new ProjectConfigImpl(update));
        return injectLinks(asDto(workspaceManager.updateWorkspace(id, workspace.getConfig())));
    }

    @DELETE
    @Path("/{id}/project/{name}")
    @RolesAllowed("user")
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
                              @PathParam("name")
                              String projectName) throws ServerException,
                                                         BadRequestException,
                                                         NotFoundException,
                                                         ConflictException,
                                                         ForbiddenException {
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(id);
        ensureUserIsWorkspaceOwner(workspace);
        if (workspace.getConfig().getProjects().removeIf(project -> project.getName().equals(projectName))) {
            workspaceManager.updateWorkspace(id, workspace.getConfig());
        }
    }

    @POST
    @Path("/{id}/machine")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @ApiOperation(value = "Create a new machine based on the configuration",
                  notes = "This operation can be performed only by authorized user")
    @ApiResponses({@ApiResponse(code = 201, message = "The machine successfully created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to create the new machine"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the machine creation" +
                                                      "(e.g. The machine with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response createMachine(@ApiParam("The workspace id")
                                  @PathParam("id")
                                  String workspaceId,
                                  @ApiParam(value = "The new machine configuration", required = true)
                                  MachineConfigDto machineConfig) throws ForbiddenException,
                                                                         NotFoundException,
                                                                         ServerException,
                                                                         ConflictException,
                                                                         BadRequestException {
        requiredNotNull(machineConfig, "Machine configuration");
        requiredNotNull(machineConfig.getType(), "Machine type");
        requiredNotNull(machineConfig.getSource(), "Machine source");
        requiredNotNull(machineConfig.getSource().getType(), "Machine source type");
        requiredNotNull(machineConfig.getSource().getLocation(), "Machine source location");

        RuntimeWorkspaceImpl runtimeWorkspace = workspaceManager.getRuntimeWorkspace(workspaceId);

        ensureUserIsWorkspaceOwner(runtimeWorkspace);

        final MachineImpl machine = machineManager.createMachineAsync(machineConfig, workspaceId, runtimeWorkspace.getActiveEnv());

        return Response.status(201)
                       .entity(MachineService.injectLinks(org.eclipse.che.api.machine.server.DtoConverter.asDto(machine),
                                                          getServiceContext()))
                       .build();
    }

    /**
     * Checks that principal from current {@link EnvironmentContext#getUser() context} is in 'workspace/owner' role
     * if he is not throws {@link ForbiddenException}.
     *
     * <p>{@link SecurityContext#isUserInRole(String)} is not the case,
     * as it works only for 'user', 'tmp-user', 'system/admin', 'system/manager.
     */
    private void ensureUserIsWorkspaceOwner(String workspaceId)
            throws ServerException, BadRequestException, ForbiddenException, NotFoundException {
        final UsersWorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
        ensureUserIsWorkspaceOwner(workspace);
    }

    /**
     * Checks that principal from current {@link EnvironmentContext#getUser() context} is in 'workspace/owner' role
     * if he is not throws {@link ForbiddenException}.
     *
     * <p>{@link SecurityContext#isUserInRole(String)} is not the case,
     * as it works only for 'user', 'tmp-user', 'system/admin', 'system/manager.
     */
    private void ensureUserIsWorkspaceOwner(UsersWorkspace usersWorkspace) throws ServerException, BadRequestException, ForbiddenException {
        final String userId = getCurrentUserId();
        if (!usersWorkspace.getOwner().equals(userId)) {
            throw new ForbiddenException("User '" + userId + "' doesn't have access to '" + usersWorkspace.getId() + "' workspace");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends UsersWorkspace & Hyperlinks> T injectLinks(T workspace) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final List<Link> links = new ArrayList<>();
        // add common workspace links
        links.add(createLink("POST",
                             uriBuilder.clone()
                                       .path(getClass(), "startById")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_START_WORKSPACE));
        links.add(createLink("DELETE",
                             uriBuilder.clone()
                                       .path(getClass(), "delete")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_REMOVE_WORKSPACE));
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(getClass(), "getWorkspaces")
                                       .build()
                                       .toString(),
                             APPLICATION_JSON,
                             GET_ALL_USER_WORKSPACES));
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(getClass(), "getSnapshot")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             "get workspace's snapshot"));

        //TODO here we add url to IDE with workspace name not good solution do it here but critical for this task  https://jira.codenvycorp.com/browse/IDEX-3619
        final URI ideUri = uriBuilder.clone()
                                     .replacePath(ideContext)
                                     .path(workspace.getConfig().getName())
                                     .build();
        links.add(createLink("GET", ideUri.toString(), TEXT_HTML, "ide url"));

        // add workspace channel link
        final Link workspaceChannelLink = createLink("GET",
                                                     getServiceContext().getBaseUriBuilder()
                                                                        .path("ws")
                                                                        .path(workspace.getId())
                                                                        .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                        .build()
                                                                        .toString(),
                                                     null);
        final LinkParameter channelParameter = newDto(LinkParameter.class).withName("channel")
                                                                          .withRequired(true);

        links.add(cloneDto(workspaceChannelLink).withRel(LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL)
                                                .withParameters(singletonList(cloneDto(channelParameter).withDefaultValue("workspace:" + workspace.getId()))));

        // add machine channels links to machines configs
        final WorkspaceConfigDto workspaceConfigDto = (WorkspaceConfigDto)workspace.getConfig();
        workspaceConfigDto.getEnvironments()
                          .stream()
                          .forEach(environmentDto -> injectMachineChannelsLinks(environmentDto,
                                                                                workspace.getId(),
                                                                                workspaceChannelLink,
                                                                                channelParameter));

        // add links for runtime workspace
        if (RuntimeWorkspaceDto.class.isAssignableFrom(workspace.getClass())) {
            links.add(createLink("GET",
                                 uriBuilder.clone()
                                           .path(getClass(), "getRuntimeWorkspaceById")
                                           .build(workspace.getId())
                                           .toString(),
                                 APPLICATION_JSON,
                                 "self link"));
            RuntimeWorkspaceDto runtimeWorkspace = (RuntimeWorkspaceDto)workspace;
            runtimeWorkspace.getMachines()
                            .forEach(machineDto -> machineDto.withLinks(
                                    singletonList(createLink("GET",
                                                             getServiceContext().getBaseUriBuilder()
                                                                                .path("/machine/{id}")
                                                                                .build(machineDto.getId())
                                                                                .toString(),
                                                             APPLICATION_JSON,
                                                             "get machine"))));
        } else {
            links.add(createLink("GET",
                                 uriBuilder.clone()
                                           .path(getClass(), "getByKey")
                                           .build(workspace.getId())
                                           .toString(),
                                 APPLICATION_JSON,
                                 "self link"));
        }
        // add links for running workspace
        if (workspace.getStatus() == RUNNING) {
            links.add(createLink("GET",
                                 uriBuilder.clone()
                                           .path(getClass(), "getRuntimeWorkspaceById")
                                           .build(workspace.getId())
                                           .toString(),
                                 APPLICATION_JSON,
                                 LINK_REL_GET_RUNTIME_WORKSPACE));
            links.add(createLink("DELETE",
                                 uriBuilder.clone()
                                           .path(getClass(), "stop")
                                           .build(workspace.getId())
                                           .toString(),
                                 STOP_WORKSPACE));

            if (RuntimeWorkspaceDto.class.isAssignableFrom(workspace.getClass())) {
                RuntimeWorkspaceDto runtimeWorkspace = (RuntimeWorkspaceDto)workspace;
                runtimeWorkspace.getDevMachine()
                                .getRuntime()
                                .getServers()
                                .values()
                                .stream()
                                .filter(server ->  WSAGENT_REFERENCE.equals(server.getRef()))
                                .findAny()
                                .ifPresent(wsAgent -> links.add(createLink("GET",
                                                                           UriBuilder.fromUri(wsAgent.getUrl())
                                                                                     .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                                     .build()
                                                                                     .toString(),
                                                                           WSAGENT_REFERENCE)));
            }
        }
        return (T)workspace.withLinks(links);
    }

    private void injectMachineChannelsLinks(EnvironmentDto environmentDto,
                                            String workspaceId,
                                            Link machineChannelLink,
                                            LinkParameter channelParameter) {

        for (MachineConfigDto machineConfigDto : environmentDto.getMachineConfigs()) {
            MachineService.injectMachineChannelsLinks(machineConfigDto,
                                                      workspaceId,
                                                      environmentDto.getName(),
                                                      machineChannelLink,
                                                      channelParameter);
        }
    }

    private SnapshotDto injectLinks(SnapshotDto snapshotDto) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link machineLink = createLink("GET",
                                            getServiceContext().getBaseUriBuilder()
                                                               .path("/machine/{id}")
                                                               .build(snapshotDto.getId())
                                                               .toString(),
                                            APPLICATION_JSON,
                                            "get machine");
        final Link workspaceCfgLink = createLink("GET",
                                                 uriBuilder.clone()
                                                           .path(getClass(), "getRuntimeWorkspaceById")
                                                           .build(snapshotDto.getId())
                                                           .toString(),
                                                 APPLICATION_JSON,
                                                 "get workspace config");
        final Link runtimeWorkspaceLink = createLink("GET",
                                                     uriBuilder.clone()
                                                               .path(getClass(), "getRuntimeWorkspaceById")
                                                               .build(snapshotDto.getWorkspaceId())
                                                               .toString(),
                                                     APPLICATION_JSON,
                                                     "get runtime workspace");
        final Link workspaceSnapshotLink = createLink("GET",
                                                      uriBuilder.clone()
                                                                .path(getClass(), "getSnapshot")
                                                                .build(snapshotDto.getWorkspaceId())
                                                                .toString(),
                                                      APPLICATION_JSON,
                                                      "get workspace's snapshot");
        return snapshotDto.withLinks(asList(machineLink, workspaceCfgLink, runtimeWorkspaceLink, workspaceSnapshotLink));
    }

    private static String getCurrentUserId() {
        return EnvironmentContext.getCurrent().getUser().getId();
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
}
