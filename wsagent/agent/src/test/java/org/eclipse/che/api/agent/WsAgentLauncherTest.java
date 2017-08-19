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

import static org.eclipse.che.api.workspace.shared.Constants.WS_AGENT_PROCESS_NAME;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class WsAgentLauncherTest {
  private static final String MACHINE_ID = "machineId";
  private static final String WORKSPACE_ID = "testWorkspaceId";
  private static final String WS_AGENT_PORT = Constants.WS_AGENT_PORT;
  private static final long WS_AGENT_MAX_START_TIME_MS = 1000;
  private static final long WS_AGENT_PING_DELAY_MS = 1;
  private static final String WS_AGENT_SERVER_LOCATION = "ws-agent.com:456789/";
  private static final String WS_AGENT_SERVER_URL = "http://" + WS_AGENT_SERVER_LOCATION;
  private static final String WS_AGENT_SERVER_LOCATION_EXT = "ws-agent-ext.com:456789/";
  private static final String WS_AGENT_SERVER_URL_EXT = "http://" + WS_AGENT_SERVER_LOCATION;
  private static final ServerPropertiesImpl SERVER_PROPERTIES =
      new ServerPropertiesImpl(null, WS_AGENT_SERVER_LOCATION, WS_AGENT_SERVER_URL);
  private static final ServerImpl SERVER =
      new ServerImpl(
          "ref", "http", WS_AGENT_SERVER_LOCATION_EXT, WS_AGENT_SERVER_URL_EXT, SERVER_PROPERTIES);
  private static final String WS_AGENT_TIMED_OUT_MESSAGE = "timeout error message";

  @Mock private MachineProcessManager machineProcessManager;
  @Mock private HttpJsonRequestFactory requestFactory;
  @Mock private Instance machine;
  @Mock private HttpJsonResponse pingResponse;
  @Mock private MachineRuntimeInfoImpl machineRuntime;
  @Mock private WsAgentPingRequestFactory wsAgentPingRequestFactory;
  @Mock private Agent agent;

  private HttpJsonRequest pingRequest;
  private WsAgentLauncher wsAgentLauncher;

  @BeforeMethod
  public void setUp() throws Exception {
    wsAgentLauncher =
        new WsAgentLauncher(
            () -> machineProcessManager,
            wsAgentPingRequestFactory,
            null,
            WS_AGENT_MAX_START_TIME_MS,
            WS_AGENT_PING_DELAY_MS,
            WS_AGENT_TIMED_OUT_MESSAGE);
    pingRequest = Mockito.mock(HttpJsonRequest.class, new SelfReturningAnswer());
    Mockito.when(agent.getScript()).thenReturn("script");
    Mockito.when(machine.getId()).thenReturn(MACHINE_ID);
    Mockito.when(machine.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    Mockito.when(machine.getRuntime()).thenReturn(machineRuntime);
    Mockito.when(machine.getNode()).thenReturn(Mockito.mock(InstanceNode.class));
    Mockito.doReturn(Collections.<String, Server>singletonMap(WS_AGENT_PORT, SERVER))
        .when(machineRuntime)
        .getServers();
    Mockito.when(requestFactory.fromUrl(Matchers.anyString())).thenReturn(pingRequest);
    Mockito.when(wsAgentPingRequestFactory.createRequest(machine)).thenReturn(pingRequest);
    Mockito.when(pingRequest.request()).thenReturn(pingResponse);
    Mockito.when(pingResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
  }

  @Test
  public void shouldStartWsAgentUsingMachineExec() throws Exception {
    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(machineProcessManager)
        .exec(
            Matchers.eq(WORKSPACE_ID),
            Matchers.eq(MACHINE_ID),
            Matchers.eq(
                new CommandImpl(
                    "org.eclipse.che.ws-agent",
                    "script\n" + WsAgentLauncher.DEFAULT_WS_AGENT_RUN_COMMAND,
                    WS_AGENT_PROCESS_NAME)),
            Matchers.eq(WsAgentLauncher.getWsAgentProcessOutputChannel(WORKSPACE_ID)));
  }

  @Test
  public void shouldPingWsAgentAfterStart() throws Exception {
    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(pingRequest).request();
    Mockito.verify(pingResponse).getResponseCode();
  }

  @Test
  public void shouldPingWsAgentMultipleTimesAfterStartIfPingFailsWithException() throws Exception {
    Mockito.when(pingRequest.request())
        .thenThrow(new ServerException(""), new BadRequestException(""), new IOException())
        .thenReturn(pingResponse);

    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(pingRequest, Mockito.times(4)).request();
    Mockito.verify(pingResponse).getResponseCode();
  }

  @Test
  public void shouldPingWsAgentMultipleTimesAfterStartIfPingReturnsNotOKResponseCode()
      throws Exception {
    Mockito.when(pingResponse.getResponseCode())
        .thenReturn(
            HttpURLConnection.HTTP_CREATED,
            HttpURLConnection.HTTP_NO_CONTENT,
            HttpURLConnection.HTTP_OK);

    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(pingRequest, Mockito.times(3)).request();
    Mockito.verify(pingResponse, Mockito.times(3)).getResponseCode();
  }

  @Test
  public void shouldNotPingWsAgentAfterFirstSuccessfulPing() throws Exception {
    Mockito.when(pingRequest.request()).thenThrow(new ServerException("")).thenReturn(pingResponse);

    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(pingRequest, Mockito.times(2)).request();
    Mockito.verify(pingResponse).getResponseCode();
  }

  @Test(
    expectedExceptions = ServerException.class,
    expectedExceptionsMessageRegExp = "Test exception"
  )
  public void shouldThrowMachineExceptionIfMachineManagerExecInDevMachineThrowsNotFoundException()
      throws Exception {
    Mockito.when(
            machineProcessManager.exec(
                Matchers.anyString(),
                Matchers.anyString(),
                Matchers.any(Command.class),
                Matchers.anyString()))
        .thenThrow(new NotFoundException("Test exception"));

    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(machineProcessManager)
        .exec(
            Matchers.anyString(),
            Matchers.anyString(),
            Matchers.any(Command.class),
            Matchers.anyString());
  }

  @Test(
    expectedExceptions = ServerException.class,
    expectedExceptionsMessageRegExp = "Test exception"
  )
  public void shouldThrowMachineExceptionIfMachineManagerExecInDevMachineThrowsMachineException()
      throws Exception {
    Mockito.when(
            machineProcessManager.exec(
                Matchers.anyString(),
                Matchers.anyString(),
                Matchers.any(Command.class),
                Matchers.anyString()))
        .thenThrow(new MachineException("Test exception"));

    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(machineProcessManager)
        .exec(
            Matchers.anyString(),
            Matchers.anyString(),
            Matchers.any(Command.class),
            Matchers.anyString());
  }

  @Test(
    expectedExceptions = ServerException.class,
    expectedExceptionsMessageRegExp = "Test exception"
  )
  public void shouldThrowExceptionIfMachineManagerExecInDevMachineThrowsBadRequestException()
      throws Exception {
    Mockito.when(
            machineProcessManager.exec(
                Matchers.anyString(),
                Matchers.anyString(),
                Matchers.any(Command.class),
                Matchers.anyString()))
        .thenThrow(new BadRequestException("Test exception"));

    wsAgentLauncher.launch(machine, agent);

    Mockito.verify(machineProcessManager)
        .exec(
            Matchers.anyString(),
            Matchers.anyString(),
            Matchers.any(Command.class),
            Matchers.anyString());
  }

  @Test(
    expectedExceptions = ServerException.class,
    expectedExceptionsMessageRegExp = WS_AGENT_TIMED_OUT_MESSAGE
  )
  public void shouldThrowMachineExceptionIfPingsWereUnsuccessfulTooLong() throws Exception {
    Mockito.when(pingRequest.request()).thenThrow(new ServerException(""));

    wsAgentLauncher.launch(machine, agent);
  }
}
