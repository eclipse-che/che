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
package org.eclipse.che.api.environment.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.common.io.CharStreams;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.machine.server.DtoConverter;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Machine API
 *
 * @author Alexander Garagatyi
 * @author Anton Korneta
 */
@Api(value = "/machine", description = "Machine REST API")
@Path("/workspace/{workspaceId}/machine")
public class MachineService extends Service {
    private final MachineProcessManager       machineProcessManager;
    private final MachineServiceLinksInjector linksInjector;
    private final WorkspaceManager            workspaceManager;
    private final CheEnvironmentValidator     environmentValidator;

    @Inject
    public MachineService(MachineProcessManager machineProcessManager,
                          MachineServiceLinksInjector linksInjector,
                          WorkspaceManager workspaceManager,
                          CheEnvironmentValidator environmentValidator) {
        this.machineProcessManager = machineProcessManager;
        this.linksInjector = linksInjector;
        this.workspaceManager = workspaceManager;
        this.environmentValidator = environmentValidator;
    }

    @GET
    @Path("/{machineId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get machine by ID")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested machine entity"),
                   @ApiResponse(code = 404, message = "Machine with specified id does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    @Deprecated
    public MachineDto getMachineById(@ApiParam(value = "Workspace ID")
                                     @PathParam("workspaceId")
                                     String workspaceId,
                                     @ApiParam(value = "Machine ID")
                                     @PathParam("machineId")
                                     String machineId)
            throws ServerException,
                   ForbiddenException,
                   NotFoundException {

        final Machine machine = workspaceManager.getMachineInstance(workspaceId, machineId);
        return linksInjector.injectLinks(DtoConverter.asDto(machine), getServiceContext());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all machines of workspace with specified ID",
                  response = MachineDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested list of machine entities"),
                   @ApiResponse(code = 400, message = "Workspace ID is not specified"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    @Deprecated
    public List<MachineDto> getMachines(@ApiParam(value = "Workspace ID")
                                        @PathParam("workspaceId")
                                        String workspaceId)
            throws ServerException,
                   BadRequestException,
                   NotFoundException {

        requiredNotNull(workspaceId, "Parameter workspace");

        WorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
        if (workspace.getRuntime() == null) {
            return Collections.emptyList();
        } else {
            return workspace.getRuntime()
                            .getMachines()
                            .stream()
                            .map(DtoConverter::asDto)
                            .map(machineDto -> linksInjector.injectLinks(machineDto, getServiceContext()))
                            .collect(Collectors.toList());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new machine based on the configuration",
                  notes = "This operation can be performed only by authorized user")
    @ApiResponses({@ApiResponse(code = 204, message = "The machine successfully created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 403, message = "The user does not have access to create the new machine"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the machine creation" +
                                                      "(e.g. The machine with such name already exists)." +
                                                      "Workspace is not in RUNNING state"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void startMachine(@ApiParam("The workspace id")
                             @PathParam("workspaceId")
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
        // definition of source should come either with a content or with location
        requiredOnlyOneNotNull(machineConfig.getSource().getLocation(), machineConfig.getSource().getContent(),
                               "Machine source should provide either location or content");

        try {
            environmentValidator.validateMachine(machineConfig);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getLocalizedMessage());
        }

        workspaceManager.startMachine(machineConfig, workspaceId);
    }

    @DELETE
    @Path("/{machineId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Stop machine")
    @ApiResponses({@ApiResponse(code = 204, message = "Machine was successfully stopped"),
                   @ApiResponse(code = 404, message = "Machine with specified id does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void stopMachine(@ApiParam(value = "Workspace ID")
                            @PathParam("workspaceId") String workspaceId,
                            @ApiParam(value = "Machine ID")
                            @PathParam("machineId") String machineId) throws NotFoundException,
                                                                             ServerException,
                                                                             ForbiddenException,
                                                                             ConflictException {
        workspaceManager.stopMachine(workspaceId, machineId);
    }

    @POST
    @Path("/{machineId}/command")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Start specified command in machine")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains entity of created machine process"),
                   @ApiResponse(code = 400, message = "Command entity is invalid"),
                   @ApiResponse(code = 404, message = "Machine with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public MachineProcessDto executeCommandInMachine(@ApiParam(value = "Workspace ID")
                                                     @PathParam("workspaceId")
                                                     String workspaceId,
                                                     @ApiParam(value = "Machine ID")
                                                     @PathParam("machineId")
                                                     String machineId,
                                                     @ApiParam(value = "Command to execute", required = true)
                                                     final CommandDto command,
                                                     @ApiParam(value = "Channel for command output")
                                                     @QueryParam("outputChannel")
                                                     String outputChannel)
            throws NotFoundException,
                   ServerException,
                   ForbiddenException,
                   BadRequestException {

        requiredNotNull(command, "Command description");
        requiredNotNull(command.getCommandLine(), "Commandline");
        return linksInjector.injectLinks(DtoConverter.asDto(machineProcessManager.exec(workspaceId,
                                                                                       machineId,
                                                                                       command,
                                                                                       outputChannel)),
                                         workspaceId,
                                         machineId,
                                         getServiceContext());
    }

    @GET
    @Path("/{machineId}/process")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get processes of machine",
                  response = MachineProcessDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains machine process entities"),
                   @ApiResponse(code = 404, message = "Machine with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<MachineProcessDto> getProcesses(@ApiParam(value = "Workspace ID")
                                                @PathParam("workspaceId")
                                                String workspaceId,
                                                @ApiParam(value = "Machine ID")
                                                @PathParam("machineId")
                                                String machineId)
            throws NotFoundException,
                   ServerException,
                   ForbiddenException {

        return machineProcessManager.getProcesses(workspaceId, machineId)
                                    .stream()
                                    .map(DtoConverter::asDto)
                                    .map(machineProcess -> linksInjector.injectLinks(machineProcess,
                                                                                     workspaceId,
                                                                                     machineId,
                                                                                     getServiceContext()))
                                    .collect(Collectors.toList());
    }

    @DELETE
    @Path("/{machineId}/process/{processId}")
    @ApiOperation(value = "Stop process in machine")
    @ApiResponses({@ApiResponse(code = 204, message = "Process was successfully stopped"),
                   @ApiResponse(code = 404, message = "Machine with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void stopProcess(@ApiParam(value = "Workspace ID")
                            @PathParam("workspaceId")
                            String workspaceId,
                            @ApiParam(value = "Machine ID")
                            @PathParam("machineId")
                            String machineId,
                            @ApiParam(value = "Process ID")
                            @PathParam("processId")
                            int processId)
            throws NotFoundException,
                   ForbiddenException,
                   ServerException {

        machineProcessManager.stopProcess(workspaceId, machineId, processId);
    }

    @GET
    @Path("/{machineId}/process/{pid}/logs")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get logs of machine process")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains logs"),
                   @ApiResponse(code = 404, message = "Machine or process with specified ID does not exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void getProcessLogs(@ApiParam(value = "Workspace ID")
                               @PathParam("workspaceId")
                               String workspaceId,
                               @ApiParam(value = "Machine ID")
                               @PathParam("machineId")
                               String machineId,
                               @ApiParam(value = "Process ID")
                               @PathParam("pid")
                               int pid,
                               @Context
                               HttpServletResponse httpServletResponse)
            throws NotFoundException,
                   ForbiddenException,
                   ServerException,
                   IOException {

        addLogsToResponse(machineProcessManager.getProcessLogReader(machineId, pid), httpServletResponse);
    }

    /**
     * Checks only one of the given object reference is {@code null}
     *
     * @param object1
     *         object reference to check
     * @param object2
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws BadRequestException
     *         when objects are both null or have both a value reference is {@code null}
     */
    private void requiredOnlyOneNotNull(Object object1, Object object2, String subject) throws BadRequestException {
        if (object1 == null && object2 == null) {
            throw new BadRequestException(subject + " required");
        }
        if (object1 != null && object2 != null) {
            throw new BadRequestException(subject + " required");
        }
    }

    private void addLogsToResponse(Reader logsReader, HttpServletResponse httpServletResponse) throws IOException {
        // Response is written directly to the servlet request stream
        httpServletResponse.setContentType("text/plain");
        CharStreams.copy(logsReader, httpServletResponse.getWriter());
        httpServletResponse.getWriter().flush();
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
}
