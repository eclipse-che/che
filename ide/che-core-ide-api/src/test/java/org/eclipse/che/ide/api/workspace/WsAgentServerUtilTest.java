/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.workspace;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_WEBSOCKET_REFERENCE;
import static org.eclipse.che.ide.api.workspace.WsAgentServerUtil.WSAGENT_SERVER_REF_PREFIX_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.ide.QueryParameters;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for the {@link WsAgentServerUtil}. */
@RunWith(MockitoJUnitRunner.class)
public class WsAgentServerUtilTest {

  static final String REF_PREFIX = "dev-";

  @Mock AppContext appContext;
  @Mock QueryParameters queryParameters;

  @Mock RuntimeImpl runtime;
  @Mock WorkspaceImpl workspace;
  @Mock MachineImpl machine;
  @Mock ServerImpl serverWsAgentHTTP;
  @Mock ServerImpl serverWsAgentWebSocket;

  @InjectMocks WsAgentServerUtil util;

  @Test
  public void shouldReturnWsAgentServerMachine() throws Exception {
    mockRuntime();

    Optional<MachineImpl> machineOpt = util.getWsAgentServerMachine();

    assertTrue(machineOpt.isPresent());
    assertEquals(machine, machineOpt.get());
  }

  @Test
  public void shouldReturnServerByRef() throws Exception {
    mockRuntime();

    Optional<ServerImpl> serverOpt = util.getServerByRef(SERVER_WS_AGENT_HTTP_REFERENCE);

    assertTrue(serverOpt.isPresent());
    assertEquals(serverWsAgentHTTP, serverOpt.get());
  }

  @Test
  public void shouldNotReturnServerByWrongRef() throws Exception {
    mockRuntime();

    Optional<ServerImpl> serverOpt = util.getServerByRef("wrong-ref");

    assertFalse(serverOpt.isPresent());
  }

  @Test
  public void shouldReturnWsAgentHttpServerReferenceWithPrefix() throws Exception {
    when(queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM)).thenReturn(REF_PREFIX);

    String serverRef = util.getWsAgentHttpServerReference();

    verify(queryParameters).getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);
    assertEquals(REF_PREFIX + SERVER_WS_AGENT_HTTP_REFERENCE, serverRef);
  }

  @Test
  public void shouldReturnWsAgentHttpServerReferenceWithoutPrefix() throws Exception {
    when(queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM)).thenReturn("");

    String serverRef = util.getWsAgentHttpServerReference();

    verify(queryParameters).getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);
    assertEquals(SERVER_WS_AGENT_HTTP_REFERENCE, serverRef);
  }

  @Test
  public void shouldReturnWsAgentWebSocketServerReferenceWithPrefix() throws Exception {
    when(queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM)).thenReturn(REF_PREFIX);

    String serverRef = util.getWsAgentWebSocketServerReference();

    verify(queryParameters).getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);
    assertEquals(REF_PREFIX + SERVER_WS_AGENT_WEBSOCKET_REFERENCE, serverRef);
  }

  @Test
  public void shouldReturnWsAgentWebSocketServerReferenceWithoutPrefix() throws Exception {
    when(queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM)).thenReturn("");

    String serverRef = util.getWsAgentWebSocketServerReference();

    verify(queryParameters).getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);
    assertEquals(SERVER_WS_AGENT_WEBSOCKET_REFERENCE, serverRef);
  }

  @Test
  public void shouldReturnWorkspaceRuntime() throws Exception {
    mockRuntime();

    Optional<RuntimeImpl> runtimeOpt = util.getWorkspaceRuntime();

    assertTrue(runtimeOpt.isPresent());
    assertEquals(runtime, runtimeOpt.get());
  }

  private void mockRuntime() {
    Map<String, ServerImpl> servers = new HashMap<>();
    servers.put(SERVER_WS_AGENT_HTTP_REFERENCE, serverWsAgentHTTP);
    servers.put(SERVER_WS_AGENT_WEBSOCKET_REFERENCE, serverWsAgentWebSocket);

    when(appContext.getWorkspace()).thenReturn(workspace);
    when(workspace.getRuntime()).thenReturn(runtime);
    when(runtime.getMachines()).thenReturn(singletonMap("dev-machine", machine));
    when(machine.getServers()).thenReturn(servers);
  }
}
