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
package org.eclipse.che.api.agent.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.inject.Inject;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.dto.server.JsonArrayImpl;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.agent.server.DtoConverter.asDto;

/**
 * Defines Agent REST API.
 *
 * @author Anatoliy Bazko
 */
@Api(value = "/agent", description = "Agent REST API")
@Path("/agent")
public class AgentService extends Service {

    private final AgentRegistry agentRegistry;

    @Context
    private SecurityContext securityContext;

    @Inject
    public AgentService(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @POST
    @Path("{name}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Create a new agent", response = AgentDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The agent successfully created"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response create(@ApiParam("The agent name") @PathParam("name") String name) throws ServerException, NotFoundException {
        try {
            Agent agent = agentRegistry.createAgent(name);
            return Response.status(Response.Status.OK).entity(asDto(agent)).build();
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @POST
    @Path("{name}/{version}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Create a new agent", response = AgentDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The agent successfully created"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response create(@ApiParam("The agent name") @PathParam("name") String name,
                           @ApiParam("The agent version") @PathParam("version") String version) throws ServerException, NotFoundException {

        try {
            Agent agent = agentRegistry.createAgent(name, version);
            return Response.status(Response.Status.OK).entity(asDto(agent)).build();
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Path("versions")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a list of the available versions of the specific agent", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getVersions(@ApiParam("The agent name") @PathParam("name") String name) throws ServerException, NotFoundException {
        try {
            Collection<String> versions = agentRegistry.getVersions(name);
            return Response.status(Response.Status.OK).entity(new JsonArrayImpl<>(new ArrayList<>(versions))).build();
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a list of the available agents", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getAgents() throws ServerException, NotFoundException {
        try {
            Collection<String> agents = agentRegistry.getAgents();
            return Response.status(Response.Status.OK).entity(new JsonArrayImpl<>(new ArrayList<>(agents))).build();
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage());
        }
    }
}
