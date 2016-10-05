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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.machine.ServerProperties;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.machine.shared.Constants.WS_AGENT_PORT;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class WsAgentPingRequestFactoryTest {
    private final static int    WS_AGENT_PING_CONNECTION_TIMEOUT_MS = 20;
    private final static String WS_AGENT_URL_IS_NOT_VALID           = "URL of Workspace Agent is null or empty.";
    private static final String WS_AGENT_SERVER_NOT_FOUND_ERROR     = "Workspace agent server not found in dev machine.";
    private final static String WS_AGENT_SERVER_URL                 = "ws_agent";

    @Mock
    private HttpJsonRequestFactory httpJsonRequestFactory;
    @Mock
    private HttpJsonRequest        httpJsonRequest;
    @Mock
    private Machine                devMachine;
    @Mock
    private Server                 server;
    @Mock
    private MachineRuntimeInfo     machineRuntimeInfo;
    @Mock
    private ServerProperties       serverProperties;

    private Map<String, Server> servers = new HashMap<>(1);

    private WsAgentPingRequestFactory factory;

    @BeforeMethod
    public void setUp() throws Exception {
        factory = new WsAgentPingRequestFactory(httpJsonRequestFactory, WS_AGENT_PING_CONNECTION_TIMEOUT_MS);

        servers.put(WS_AGENT_SERVER_URL, server);
        servers.put(WS_AGENT_PORT, server);

        when(httpJsonRequestFactory.fromUrl(anyString())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.setMethod(HttpMethod.GET)).thenReturn(httpJsonRequest);
        when(server.getProperties()).thenReturn(serverProperties);
        when(serverProperties.getInternalUrl()).thenReturn(WS_AGENT_SERVER_URL);
        when(devMachine.getRuntime()).thenReturn(machineRuntimeInfo);
        doReturn(servers).when(machineRuntimeInfo).getServers();
    }


    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = WS_AGENT_SERVER_NOT_FOUND_ERROR)
    public void throwsServerExceptionWhenWsAgentIsNull() throws Exception {
        servers.clear();

        factory.createRequest(devMachine);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = WS_AGENT_URL_IS_NOT_VALID)
    public void throwsServerExceptionWhenWsServerUrlIsNull() throws Exception {
        when(serverProperties.getInternalUrl()).thenReturn(null);

        factory.createRequest(devMachine);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = WS_AGENT_URL_IS_NOT_VALID)
    public void throwsServerExceptionWhenWsServerUrlIsEmpty() throws Exception {
        when(serverProperties.getInternalUrl()).thenReturn("");

        factory.createRequest(devMachine);
    }

    @Test
    public void pingRequestShouldBeCreated() throws Exception {
        factory.createRequest(devMachine);

        verify(httpJsonRequestFactory).fromUrl(WS_AGENT_SERVER_URL + '/');
        verify(httpJsonRequest).setMethod(javax.ws.rs.HttpMethod.GET);
        verify(httpJsonRequest).setTimeout(WS_AGENT_PING_CONNECTION_TIMEOUT_MS);
    }
}
