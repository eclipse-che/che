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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Mechanism for checking workspace agent's state.
 *
 * @author Vitalii Parfonov
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentHealthCheckerImpl implements WsAgentHealthChecker {
    protected static final Logger LOG = LoggerFactory.getLogger(WsAgentHealthCheckerImpl.class);

    private final WsAgentPingRequestFactory wsAgentPingRequestFactory;

    @Inject
    public WsAgentHealthCheckerImpl(WsAgentPingRequestFactory wsAgentPingRequestFactory) {
        this.wsAgentPingRequestFactory = wsAgentPingRequestFactory;
    }

    @Override
    public WsAgentHealthStateDto check(Machine machine) throws ServerException {
        Server wsAgent = getWsAgent(machine);
        final WsAgentHealthStateDto agentHealthStateDto = newDto(WsAgentHealthStateDto.class);
        if (wsAgent == null) {
            return agentHealthStateDto.withCode(NOT_FOUND.getStatusCode())
                                      .withReason("Workspace Agent not available");
        }
        try {
            final HttpJsonRequest pingRequest = createPingRequest(machine);
            final HttpJsonResponse response = pingRequest.request();
            return agentHealthStateDto.withCode(response.getResponseCode());
        } catch (ApiException | IOException e) {
            return agentHealthStateDto.withCode(SERVICE_UNAVAILABLE.getStatusCode())
                                      .withReason(e.getMessage());
        }
    }

    protected HttpJsonRequest createPingRequest(Machine machine) throws ServerException {
        return wsAgentPingRequestFactory.createRequest(machine);
    }

    private Server getWsAgent(Machine machine) {
        final Map<String, ? extends Server> servers = machine.getRuntime().getServers();
        for (Server server : servers.values()) {
            if (WSAGENT_REFERENCE.equals(server.getRef())) {
                return server;
            }
        }
        return null;
    }

}
