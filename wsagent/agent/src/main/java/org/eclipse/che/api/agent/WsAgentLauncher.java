/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.agent;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.workspace.shared.Constants.WS_AGENT_PROCESS_NAME;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.agent.server.WsAgentHealthChecker;
import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.agent.server.launcher.AgentLauncher;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts ws agent in the machine and waits until ws agent sends notification about its start.
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
@Singleton
public class WsAgentLauncher implements AgentLauncher {
  protected static final Logger LOG = LoggerFactory.getLogger(WsAgentLauncher.class);

  private static final String WS_AGENT_PROCESS_OUTPUT_CHANNEL = "workspace:%s:ext-server:output";
  protected static final String DEFAULT_WS_AGENT_RUN_COMMAND = "~/che/ws-agent/bin/catalina.sh run";

  private final Provider<MachineProcessManager> machineProcessManagerProvider;
  private final WsAgentPingRequestFactory wsAgentPingRequestFactory;
  private final WsAgentHealthChecker wsAgentHealthChecker;
  private final long wsAgentMaxStartTimeMs;
  private final long wsAgentPingDelayMs;
  private final String pingTimedOutErrorMessage;
  private final int wsAgentPingSuccessThreshold;
  private final String wsAgentRunCommand;

  @Inject
  public WsAgentLauncher(
      Provider<MachineProcessManager> machineProcessManagerProvider,
      WsAgentHealthChecker wsAgentHealthChecker,
      WsAgentPingRequestFactory wsAgentPingRequestFactory,
      @Nullable @Named("machine.ws_agent.run_command") String wsAgentRunCommand,
      @Named("che.workspace.agent.dev.max_start_time_ms") long wsAgentMaxStartTimeMs,
      @Named("che.workspace.agent.dev.ping_delay_ms") long wsAgentPingDelayMs,
      @Named("che.workspace.agent.dev.ping_success_threshold") int wsAgentPingSuccessThreshold,
      @Named("che.workspace.agent.dev.ping_timeout_error_msg") String pingTimedOutErrorMessage) {
    this.machineProcessManagerProvider = machineProcessManagerProvider;
    this.wsAgentPingRequestFactory = wsAgentPingRequestFactory;
    this.wsAgentHealthChecker = wsAgentHealthChecker;
    this.wsAgentMaxStartTimeMs = wsAgentMaxStartTimeMs;
    this.wsAgentPingDelayMs = wsAgentPingDelayMs;
    this.wsAgentPingSuccessThreshold = wsAgentPingSuccessThreshold;
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
  public boolean shouldBlockMachineStartOnError() {
    return true;
  }

  @Override
  public void launch(Instance machine, Agent agent) throws ServerException {
    final String wsAgentPingUrl;
    try {
      wsAgentPingUrl = wsAgentPingRequestFactory.createRequest(machine).getUrl();
    } catch (ServerException e) {
      throw new MachineException(e.getServiceError());
    }

    String script =
        agent.getScript() + "\n" + firstNonNull(wsAgentRunCommand, DEFAULT_WS_AGENT_RUN_COMMAND);
    try {
      // for server side type of command mean nothing
      // but we will use it as marker on
      // client side for track this command
      CommandImpl command = new CommandImpl(getAgentId(), script, WS_AGENT_PROCESS_NAME);

      machineProcessManagerProvider
          .get()
          .exec(
              machine.getWorkspaceId(),
              machine.getId(),
              command,
              getWsAgentProcessOutputChannel(machine.getWorkspaceId()));

      final long pingStartTimestamp = System.currentTimeMillis();
      LOG.debug(
          "Starts pinging ws agent. Workspace ID:{}. Url:{}. Timestamp:{}",
          machine.getWorkspaceId(),
          wsAgentPingUrl,
          pingStartTimestamp);

      int pingSuccess = 0;
      while (System.currentTimeMillis() - pingStartTimestamp < wsAgentMaxStartTimeMs) {
        if (pingWsAgent(machine)) {
          pingSuccess++;
        } else {
          pingSuccess = 0;
        }

        if (pingSuccess == wsAgentPingSuccessThreshold) {
          return;
        }

        Thread.sleep(wsAgentPingDelayMs);
      }
    } catch (BadRequestException | ServerException | NotFoundException e) {
      throw new ServerException(e.getServiceError());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ServerException("Ws agent pinging is interrupted");
    }
    LOG.error(
        "Fail pinging ws agent with {} url in {} workspace in {} machine on {} node.",
        wsAgentPingUrl,
        machine.getWorkspaceId(),
        machine.getId(),
        machine.getNode().getHost());
    throw new ServerException(pingTimedOutErrorMessage);
  }

  public static String getWsAgentProcessOutputChannel(String workspaceId) {
    return String.format(WS_AGENT_PROCESS_OUTPUT_CHANNEL, workspaceId);
  }

  private boolean pingWsAgent(Instance machine) throws ServerException {
    try {
      WsAgentHealthStateDto state = wsAgentHealthChecker.check(machine);
      if (state.getCode() == HttpURLConnection.HTTP_OK) {
        return true;
      }
    } catch (ApiException ignored) {
    }
    return false;
  }
}
