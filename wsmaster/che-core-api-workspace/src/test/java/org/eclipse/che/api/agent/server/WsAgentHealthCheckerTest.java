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

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * @author Valeriy Svydenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WsAgentHealthCheckerTest {
//    private final static String WS_AGENT_SERVER_URL                 = "ws_agent";
//
//    @Mock
//    private WsAgentPingRequestFactory wsAgentPingRequestFactory;
//    @Mock
//    private OldMachine                   devMachine;
//    @Mock
//    private Machine            machineRuntimeInfo;
//    @Mock
//    private OldServer                    server;
//    @Mock
//    private HttpJsonRequest           httpJsonRequest;
//    @Mock
//    private HttpJsonResponse          httpJsonResponse;
//
//    private Map<String, OldServer> servers = new HashMap<>(1);
//
//    private WsAgentHealthCheckerImpl checker;
//
//    @BeforeMethod
//    public void setUp() throws Exception {
//        servers.put(WSAGENT_REFERENCE, server);
//        servers.put(WS_AGENT_PORT, server);
//
//        when(server.getRef()).thenReturn(WSAGENT_REFERENCE);
//        when(server.getUrl()).thenReturn(WS_AGENT_SERVER_URL);
//        when(wsAgentPingRequestFactory.createRequest(devMachine)).thenReturn(httpJsonRequest);
//
//        checker = new WsAgentHealthCheckerImpl(wsAgentPingRequestFactory);
//
//        when(httpJsonRequest.setMethod(any())).thenReturn(httpJsonRequest);
//        when(httpJsonRequest.setTimeout(anyInt())).thenReturn(httpJsonRequest);
//        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
//
//        when(httpJsonResponse.getResponseCode()).thenReturn(200);
//        when(httpJsonResponse.asString()).thenReturn("response");
//
//        when(devMachine.getRuntime()).thenReturn(machineRuntimeInfo);
//        doReturn(servers).when(machineRuntimeInfo).getServers();
//    }
//
//    @Test
//    public void stateShouldBeReturnedWithStatusNotFoundIfWorkspaceAgentIsNotExist() throws Exception {
//        when(machineRuntimeInfo.getServers()).thenReturn(emptyMap());
//
//        WsAgentHealthStateDto result = checker.check(devMachine);
//
//        assertEquals(NOT_FOUND.getStatusCode(), result.getCode());
//    }
//
//    @Test
//    public void returnStateWithNotFoundCode() throws Exception {
//        doReturn(emptyMap()).when(machineRuntimeInfo).getServers();
//
//        final WsAgentHealthStateDto check = checker.check(devMachine);
//        assertEquals(NOT_FOUND.getStatusCode(), check.getCode());
//        assertEquals("Workspace Agent not available", check.getReason());
//    }
//
//    @Test
//    public void pingRequestToWsAgentShouldBeSent() throws Exception {
//        final WsAgentHealthStateDto result = checker.check(devMachine);
//
//        verify(httpJsonRequest).request();
//
//        assertEquals(200, result.getCode());
//    }
//
//    @Test
//    public void returnResultWithUnavailableStateIfDoNotGetResponseFromWsAgent() throws Exception {
//        doThrow(IOException.class).when(httpJsonRequest).request();
//        final WsAgentHealthStateDto result = checker.check(devMachine);
//
//        verify(httpJsonRequest).request();
//
//        assertEquals(SERVICE_UNAVAILABLE.getStatusCode(), result.getCode());
//    }

}
