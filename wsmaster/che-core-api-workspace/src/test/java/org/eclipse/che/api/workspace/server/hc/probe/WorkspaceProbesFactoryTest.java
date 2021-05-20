/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import static org.mockito.Mockito.lenient;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
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
  private static final int SERVER_PING_SUCCESS_THRESHOLD = 1;
  private static final ServerImpl SERVER = new ServerImpl().withUrl("https://localhost:4040/path1");

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "default", "id1", "infraNamespace");

  @Mock private MachineTokenProvider tokenProvider;

  private WorkspaceProbesFactory probesFactory;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(tokenProvider.getToken(IDENTITY.getOwnerId(), WORKSPACE_ID)).thenReturn(TOKEN);

    probesFactory = new WorkspaceProbesFactory(tokenProvider, SERVER_PING_SUCCESS_THRESHOLD);
  }

  @Test
  public void shouldNotCreateProbesFactoriesForOtherServers() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            IDENTITY,
            MACHINE_NAME,
            ImmutableMap.of("server1/http", SERVER, "terminal/http", SERVER, "terminal1", SERVER));

    assertTrue(wsProbes.getProbes().isEmpty());
  }

  @Test
  public void returnsProbesForAMachineForWsAgent() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            IDENTITY, MACHINE_NAME, singletonMap(SERVER_WS_AGENT_HTTP_REFERENCE, SERVER));

    verifyHttpProbeConfig(
        wsProbes,
        SERVER_WS_AGENT_HTTP_REFERENCE,
        3,
        1,
        10,
        10,
        120,
        "/path1/liveness",
        "localhost",
        4040,
        "https",
        singletonMap(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN));
  }

  @Test
  public void returnsProbesForAMachineForTerminal() throws Exception {
    WorkspaceProbes wsProbes =
        probesFactory.getProbes(
            IDENTITY,
            MACHINE_NAME,
            singletonMap(
                SERVER_TERMINAL_REFERENCE, new ServerImpl().withUrl("wss://localhost:4040/pty")));

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
            IDENTITY,
            MACHINE_NAME,
            singletonMap(
                SERVER_EXEC_AGENT_HTTP_REFERENCE,
                new ServerImpl().withUrl("https://localhost:4040/process")));

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
