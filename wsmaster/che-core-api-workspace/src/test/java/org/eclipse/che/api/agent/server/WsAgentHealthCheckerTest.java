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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WS_AGENT_PORT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WsAgentHealthCheckerTest {
    private final static String WS_AGENT_SERVER_URL                 = "ws_agent";

    @Mock
    private WsAgentPingRequestFactory wsAgentPingRequestFactory;
    @Mock
    private Machine                devMachine;
    @Mock
    private MachineRuntimeInfo     machineRuntimeInfo;
    @Mock
    private Server                 server;
    @Mock
    private HttpJsonRequest        httpJsonRequest;
    @Mock
    private HttpJsonResponse       httpJsonResponse;

    private Map<String, Server> servers = new HashMap<>(1);

    private WsAgentHealthCheckerImpl checker;

    @BeforeMethod
    public void setUp() throws Exception {
        servers.put(WSAGENT_REFERENCE, server);
        servers.put(WS_AGENT_PORT, server);

        when(server.getRef()).thenReturn(WSAGENT_REFERENCE);
        when(server.getUrl()).thenReturn(WS_AGENT_SERVER_URL);
        when(wsAgentPingRequestFactory.createRequest(devMachine)).thenReturn(httpJsonRequest);

        checker = new WsAgentHealthCheckerImpl(wsAgentPingRequestFactory);

        when(httpJsonRequest.setMethod(any())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.setTimeout(anyInt())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);

        when(httpJsonResponse.getResponseCode()).thenReturn(200);
        when(httpJsonResponse.asString()).thenReturn("response");

        when(devMachine.getRuntime()).thenReturn(machineRuntimeInfo);
        doReturn(servers).when(machineRuntimeInfo).getServers();
    }

    @Test
    public void stateShouldBeReturnedWithStatusNotFoundIfWorkspaceAgentIsNotExist() throws Exception {
        when(machineRuntimeInfo.getServers()).thenReturn(emptyMap());

        WsAgentHealthStateDto result = checker.check(devMachine);

        assertEquals(NOT_FOUND.getStatusCode(), result.getCode());
    }

    @Test
    public void returnStateWithNotFoundCode() throws Exception {
        doReturn(emptyMap()).when(machineRuntimeInfo).getServers();

        final WsAgentHealthStateDto check = checker.check(devMachine);
        assertEquals(NOT_FOUND.getStatusCode(), check.getCode());
        assertEquals("Workspace Agent not available", check.getReason());
    }

    @Test
    public void pingRequestToWsAgentShouldBeSent() throws Exception {
        final WsAgentHealthStateDto result = checker.check(devMachine);

        verify(httpJsonRequest).request();

        assertEquals(200, result.getCode());
    }

    @Test
    public void returnResultWithUnavailableStateIfDoNotGetResponseFromWsAgent() throws Exception {
        doThrow(IOException.class).when(httpJsonRequest).request();
        final WsAgentHealthStateDto result = checker.check(devMachine);

        verify(httpJsonRequest).request();

        assertEquals(SERVICE_UNAVAILABLE.getStatusCode(), result.getCode());
    }

}
