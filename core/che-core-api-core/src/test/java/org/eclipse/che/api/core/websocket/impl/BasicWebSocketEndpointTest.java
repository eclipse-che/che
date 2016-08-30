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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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
    private static final int    ENDPOINT_ID      = 0;
    private static final String MESSAGE          = "message";
    private static final int    MAX_IDLE_TIMEOUT = 0;

    @Mock
    private WebSocketSessionRegistry        registry;
    @Mock
    private PendingMessagesReSender         reSender;
    @Mock
    private WebSocketTransmissionDispatcher dispatcher;
    @Mock
    private WebSocketTransmissionValidator  validator;
    @InjectMocks
    private BasicWebSocketEndpoint          endpoint;

    @Mock
    private Session     session;
    @Mock
    private CloseReason closeReason;
    @Mock
    private Throwable   throwable;

    @Test
    public void shouldSetSessionMaxIdleTimeoutOnOpen() {
        endpoint.onOpen(session, ENDPOINT_ID);

        verify(session).setMaxIdleTimeout(MAX_IDLE_TIMEOUT);
    }

    @Test
    public void shouldRegisterSessionOnOpen() {
        endpoint.onOpen(session, ENDPOINT_ID);

        verify(registry).add(ENDPOINT_ID, session);
    }

    @Test
    public void shouldSendPendingMessagesSessionOnOpen() {
        endpoint.onOpen(session, ENDPOINT_ID);

        verify(reSender).resend(ENDPOINT_ID);
    }

    @Test
    public void shouldRemoveSessionFromRegistryOnClose() {
        endpoint.onClose(closeReason, ENDPOINT_ID);

        verify(registry).remove(ENDPOINT_ID);
    }

    @Test
    public void shouldRunReceiverOnMessage() {
        endpoint.onMessage(MESSAGE, ENDPOINT_ID);

        verify(dispatcher).dispatch(MESSAGE, ENDPOINT_ID);
    }

}
