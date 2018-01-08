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
package org.eclipse.che.api.workspace.server.hc.probe;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_EXEC_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceProbesFactoryTest {
  private static final String WORKSPACE_ID = "wsId";
  private static final String MACHINE_NAME = "machine1";
  private static final String TOKEN = "token1";
  private static final ServerImpl SERVER = new ServerImpl().withUrl("https://localhost:4040/path1");

  @Mock private MachineTokenProvider tokenProvider;

  private WorkspaceProbesFactory probesFactory;

  @BeforeMethod
  public void setUp() throws Exception {
    when(tokenProvider.getToken(WORKSPACE_ID)).thenReturn(TOKEN);

    probesFactory = new WorkspaceProbesFactory(tokenProvider);
  }

  @Test
  public void shouldNotCreateProbesFactoriesForOtherServers() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            WORKSPACE_ID,
            MACHINE_NAME,
            ImmutableMap.of("server1/http", SERVER, "terminal/http", SERVER, "terminal1", SERVER));

    assertTrue(wsProbes.getProbes().isEmpty());
  }

  @Test
  public void returnsProbesForAMachineForWsAgent() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            WORKSPACE_ID, MACHINE_NAME, singletonMap(SERVER_WS_AGENT_HTTP_REFERENCE, SERVER));

    verifyHttpProbeConfig(
        wsProbes,
        SERVER_WS_AGENT_HTTP_REFERENCE,
        3,
        1,
        10,
        10,
        120,
        "/path1/",
        "localhost",
        4040,
        "https",
        singletonMap(HttpHeaders.AUTHORIZATION, TOKEN));
  }

  @Test
  public void returnsProbesForAMachineForTerminal() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            WORKSPACE_ID,
            MACHINE_NAME,
            singletonMap(
                SERVER_TERMINAL_REFERENCE, new ServerImpl().withUrl("wss://localhost:4040")));

    verifyHttpProbeConfig(
        wsProbes,
        SERVER_TERMINAL_REFERENCE,
        3,
        1,
        10,
        10,
        120,
        "/liveness",
        "localhost",
        4040,
        "https",
        emptyMap());
  }

  @Test
  public void returnsProbesForAMachineForExec() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            WORKSPACE_ID, MACHINE_NAME, singletonMap(SERVER_EXEC_AGENT_HTTP_REFERENCE, SERVER));

    verifyHttpProbeConfig(
        wsProbes,
        SERVER_EXEC_AGENT_HTTP_REFERENCE,
        3,
        1,
        10,
        10,
        120,
        "/liveness",
        "localhost",
        4040,
        "https",
        emptyMap());
  }

  public void verifyHttpProbeConfig(
      WorkspaceProbes wsProbes,
      String serverName,
      int failureThreshold,
      int successThreshold,
      int initialDelay,
      int period,
      int timeout,
      String path,
      String host,
      int port,
      String protocol,
      Map<String, String> headers)
      throws Exception {

    assertEquals(wsProbes.getWorkspaceId(), WORKSPACE_ID);
    List<ProbeFactory> probes = wsProbes.getProbes();
    assertEquals(probes.size(), 1);
    ProbeFactory probeFactory = probes.get(0);
    assertTrue(probeFactory instanceof HttpProbeFactory);
    HttpProbeFactory httpProbeFactory = (HttpProbeFactory) probeFactory;
    assertEquals(httpProbeFactory.getMachineName(), MACHINE_NAME);
    assertEquals(httpProbeFactory.getServerName(), serverName);
    assertEquals(httpProbeFactory.getWorkspaceId(), WORKSPACE_ID);
    HttpProbeConfig probeConfig = httpProbeFactory.getProbeConfig();
    assertEquals(probeConfig.getFailureThreshold(), failureThreshold);
    assertEquals(probeConfig.getSuccessThreshold(), successThreshold);
    assertEquals(probeConfig.getInitialDelaySeconds(), initialDelay);
    assertEquals(probeConfig.getPeriodSeconds(), period);
    assertEquals(probeConfig.getTimeoutSeconds(), timeout);
    assertEquals(probeConfig.getPath(), path);
    assertEquals(probeConfig.getHost(), host);
    assertEquals(probeConfig.getPort(), port);
    assertEquals(probeConfig.getScheme(), protocol);
    assertEquals(probeConfig.getHeaders(), headers);
  }
}
