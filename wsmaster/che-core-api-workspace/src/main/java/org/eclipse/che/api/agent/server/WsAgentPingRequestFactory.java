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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.machine.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Creates a request for pinging Workspace Agent.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class WsAgentPingRequestFactory {
    protected static final Logger LOG = LoggerFactory.getLogger(WsAgentPingRequestFactory.class);

    private static final String WS_AGENT_SERVER_NOT_FOUND_ERROR     = "Workspace agent server not found in dev machine.";
    private static final String WS_AGENT_URL_IS_NULL_OR_EMPTY_ERROR = "URL of Workspace Agent is null or empty.";

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final int                    wsAgentPingConnectionTimeoutMs;

    @Inject
    public WsAgentPingRequestFactory(HttpJsonRequestFactory httpJsonRequestFactory,
                                     @Named("che.workspace.agent.dev.ping_conn_timeout_ms") int wsAgentPingConnectionTimeoutMs) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.wsAgentPingConnectionTimeoutMs = wsAgentPingConnectionTimeoutMs;
    }

    /**
     * Creates request which can check if workspace agent is pinging.
     *
     * @param machine
     *         machine instance
     * @return instance of {@link HttpJsonRequest}
     * @throws ServerException
     *         if internal server error occurred
     */
    public HttpJsonRequest createRequest(Machine machine) throws ServerException {
        Map<String, ? extends Server> servers = machine.getRuntime().getServers();
        Server wsAgentServer = servers.get(Constants.WS_AGENT_PORT);
        if (wsAgentServer == null) {
            LOG.error("{} WorkspaceId: {}, DevMachine Id: {}, found servers: {}",
                      WS_AGENT_SERVER_NOT_FOUND_ERROR, machine.getWorkspaceId(), machine.getId(), servers);
            throw new ServerException(WS_AGENT_SERVER_NOT_FOUND_ERROR);
        }
        String wsAgentPingUrl = wsAgentServer.getProperties().getInternalUrl();
        if (isNullOrEmpty(wsAgentPingUrl)) {
            LOG.error(WS_AGENT_URL_IS_NULL_OR_EMPTY_ERROR);
            throw new ServerException(WS_AGENT_URL_IS_NULL_OR_EMPTY_ERROR);
        }
        // since everrest mapped on the slash in case of it absence
        // we will always obtain not found response
        if (!wsAgentPingUrl.endsWith("/")) {
            wsAgentPingUrl = wsAgentPingUrl.concat("/");
        }
        return httpJsonRequestFactory.fromUrl(wsAgentPingUrl)
                                     .setMethod(HttpMethod.GET)
                                     .setTimeout(wsAgentPingConnectionTimeoutMs);
    }
}
