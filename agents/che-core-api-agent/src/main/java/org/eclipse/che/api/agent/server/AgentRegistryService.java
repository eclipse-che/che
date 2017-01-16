/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.dto.AgentDto;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import java.util.Collection;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.agent.server.DtoConverter.asDto;

/**
 * Defines Agent REST API.
 *
 * @see AgentRegistry
 * @see Agent
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
    @Path("/id/{id}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets the latest version of the agent", response = AgentDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested agent entity"),
                   @ApiResponse(code = 404, message = "Agent not found in the registry"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Agent getById(@ApiParam("The agent id") @PathParam("id") String id) throws ApiException {
        try {
            return asDto(agentRegistry.getAgent(new AgentKeyImpl(id)));
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @GET
    @Path("/id/{id}/version/{version}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets the specific version of the agent", response = AgentDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested agent entity"),
                   @ApiResponse(code = 404, message = "Agent not found in the registry"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Agent getByName(@ApiParam("The agent id") @PathParam("id") String id,
                           @ApiParam("The agent version") @PathParam("version") String version) throws ApiException {
        try {
            return asDto(agentRegistry.getAgent(new AgentKeyImpl(id, version)));
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage(), e);
        }

    }

    @GET
    @Path("/versions/{id}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a list of available versions of the giving agent", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains available versions of the giving agent"),
                   @ApiResponse(code = 404, message = "Agent not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<String> getVersions(@ApiParam("The agent id") @PathParam("id") String id) throws ApiException {
        try {
            return agentRegistry.getVersions(id);
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a collection of the available agents", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains collection of available agents"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Collection<Agent> getAgents() throws ApiException {
        try {
            return agentRegistry.getAgents();
        } catch (AgentNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (AgentException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }
}
