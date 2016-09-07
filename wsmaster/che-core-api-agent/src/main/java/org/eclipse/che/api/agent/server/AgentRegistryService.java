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

import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
public class AgentRegistryService extends Service {

    private final AgentRegistry agentRegistry;

    @Inject
    public AgentRegistryService(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @GET
    @Path("/name/{name}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets the latest version of the agent", response = AgentDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The agent successfully created"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Agent getByName(@ApiParam("The agent name") @PathParam("name") String name) throws ApiException {
        Agent agent = agentRegistry.getAgent(name);
        return asDto(agent);
    }

    @GET
    @Path("/name/{name}/version/{version}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets the agent of the specific version", response = AgentDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The agent successfully created"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Agent getByName(@ApiParam("The agent name") @PathParam("name") String name,
                           @ApiParam("The agent version") @PathParam("version") String version) throws ApiException {
        Agent agent = agentRegistry.getAgent(name, version);
        return asDto(agent);
    }

    @GET
    @Path("/versions/{name}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a list of the available versions of the specific agent", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<String> getVersions(@ApiParam("The agent name") @PathParam("name") String name) throws ApiException {
        return agentRegistry.getVersions(name);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a list of the available agents", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<String> getAgents() throws ApiException {
        return agentRegistry.getAgents();
    }
}
