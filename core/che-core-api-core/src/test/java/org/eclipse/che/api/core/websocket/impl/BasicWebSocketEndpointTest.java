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
package org.eclipse.che.api.core.websocket.impl;

import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link BasicWebSocketEndpoint}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class BasicWebSocketEndpointTest {
    @Mock
    private WebSocketSessionRegistry registry;
    @Mock
    private MessagesReSender         reSender;
    @Mock
    private WebSocketMessageReceiver receiver;
    @InjectMocks
    private BasicWebSocketEndpoint   endpoint;

    @Mock
    private Session session;
    @Mock
    private CloseReason closeReason;

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldAddToRegistryOnOpen(){
        endpoint.onOpen(session, "id");

        verify(registry).add("id", session);
    }

    @Test
    public void shouldResendOnOpen(){
        endpoint.onOpen(session, "id");

        verify(reSender).resend("id");
    }

    @Test
    public void shouldRunReceiveOnMessage(){
        endpoint.onMessage("message", "id");

        verify(receiver).receive("id", "message");
    }

    @Test
    public void shouldRunRemoveOnClose(){
        endpoint.onClose(closeReason, "id");

        verify(registry).remove("id");
    }
}
