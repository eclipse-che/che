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
package org.eclipse.che.workspace.infrastructure.docker.old.agents;

import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.HttpURLConnection;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.workspace.shared.Constants.WS_AGENT_PROCESS_NAME;

/**
 * Starts ws agent in the machine and waits until ws agent sends notification about its start.
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
@Singleton
public class WsAgentLauncher implements AgentLauncher {
    protected static final Logger LOG = LoggerFactory.getLogger(WsAgentLauncher.class);

    private static final   String WS_AGENT_PROCESS_OUTPUT_CHANNEL = "workspace:%s:ext-server:output";
    protected static final String DEFAULT_WS_AGENT_RUN_COMMAND    = "~/che/ws-agent/bin/catalina.sh run";

//    private final Provider<MachineProcessManager> machineProcessManagerProvider;
    private final WsAgentPingRequestFactory       wsAgentPingRequestFactory;
    private final long                            wsAgentMaxStartTimeMs;
    private final long                            wsAgentPingDelayMs;
    private final String                          pingTimedOutErrorMessage;
    private final String                          wsAgentRunCommand;

    @Inject
    public WsAgentLauncher(//Provider<MachineProcessManager> machineProcessManagerProvider,
                           WsAgentPingRequestFactory wsAgentPingRequestFactory,
                           @Nullable @Named("machine.ws_agent.run_command") String wsAgentRunCommand,
                           @Named("che.workspace.agent.dev.max_start_time_ms") long wsAgentMaxStartTimeMs,
                           @Named("che.workspace.agent.dev.ping_delay_ms") long wsAgentPingDelayMs,
                           @Named("che.workspace.agent.dev.ping_timeout_error_msg") String pingTimedOutErrorMessage) {
//        this.machineProcessManagerProvider = machineProcessManagerProvider;
        this.wsAgentPingRequestFactory = wsAgentPingRequestFactory;
        this.wsAgentMaxStartTimeMs = wsAgentMaxStartTimeMs;
        this.wsAgentPingDelayMs = wsAgentPingDelayMs;
        this.pingTimedOutErrorMessage = pingTimedOutErrorMessage;
        this.wsAgentRunCommand = wsAgentRunCommand;
    }

    @Override
    public String getAgentId() {
        return "org.eclipse.che.ws-agent";
    }

    @Override
    public String getMachineType() {
        return "docker";
    }

    @Override
    public void launch(DockerService machine, Agent agent) throws InfrastructureException {
        final HttpJsonRequest wsAgentPingRequest;
        try {
            wsAgentPingRequest = createPingRequest(machine);
        } catch (ServerException e) {
            throw new InfrastructureException(e);
        }

        String script = agent.getScript() + "\n" + firstNonNull(wsAgentRunCommand, DEFAULT_WS_AGENT_RUN_COMMAND);

        final String wsAgentPingUrl = wsAgentPingRequest.getUrl();
        try {
            // for server side type of command mean nothing
            // but we will use it as marker on
            // client side for track this command
            CommandImpl command = new CommandImpl(getAgentId(), script, WS_AGENT_PROCESS_NAME);

//            machineProcessManagerProvider.get().exec(machine.getWorkspaceId(),
//                                                     machine.getId(),
//                                                     command,
//                                                     getWsAgentProcessOutputChannel(machine.getWorkspaceId()));

            final long pingStartTimestamp = System.currentTimeMillis();
            LOG.debug("Starts pinging ws agent. Workspace ID:{}. Url:{}. Timestamp:{}",
//                      machine.getWorkspaceId(),
                      wsAgentPingUrl,
                      pingStartTimestamp);

            while (System.currentTimeMillis() - pingStartTimestamp < wsAgentMaxStartTimeMs) {
                if (pingWsAgent(wsAgentPingRequest)) {
                    return;
                } else {
                    Thread.sleep(wsAgentPingDelayMs);
                }
            }
        } catch (ServerException e) {
            throw new InfrastructureException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InfrastructureException("Ws agent pinging is interrupted");
        }
        LOG.error("Fail pinging ws agent. Workspace ID:{}. Url:{}. Timestamp:{}",
//                  machine.getWorkspaceId(),
                  wsAgentPingUrl);
        throw new InfrastructureException(pingTimedOutErrorMessage);
    }

    public static String getWsAgentProcessOutputChannel(String workspaceId) {
        return String.format(WS_AGENT_PROCESS_OUTPUT_CHANNEL, workspaceId);
    }

    // forms the ping request based on information about the machine.
    protected HttpJsonRequest createPingRequest(DockerService machine) throws ServerException {
        return wsAgentPingRequestFactory.createRequest(null);
    }

    private boolean pingWsAgent(HttpJsonRequest wsAgentPingRequest) throws ServerException {
        try {
            final HttpJsonResponse pingResponse = wsAgentPingRequest.request();
            if (pingResponse.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (ApiException | IOException ignored) {
        }
        return false;
    }
}
