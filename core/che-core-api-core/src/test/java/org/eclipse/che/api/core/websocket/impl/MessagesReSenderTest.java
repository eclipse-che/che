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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MessagesReSender}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class MessagesReSenderTest {
    private static final String MESSAGE     = "message";
    private static final String ENDPOINT_ID = "id";

    @Mock
    private WebSocketSessionRegistry sessionRegistry;
    @InjectMocks
    private MessagesReSender         reSender;

    @Mock
    private Session               session;
    @Mock
    private RemoteEndpoint.Async  endpoint;

    @BeforeMethod
    public void beforeMethod() {
        when(sessionRegistry.get(anyString())).thenReturn(Optional.of(session));
        when(session.getAsyncRemote()).thenReturn(endpoint);
        when(session.isOpen()).thenReturn(true);
    }

    @BeforeMethod
    public void before() {
        reSender = new MessagesReSender(sessionRegistry);
    }

    @Test
    public void shouldStopIfSessionIsNotRegistered() {
        when(sessionRegistry.get(anyString())).thenReturn(Optional.empty());

        reSender.add(ENDPOINT_ID, MESSAGE);

        reSender.resend(ENDPOINT_ID);

        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(session, never()).getAsyncRemote();
        verify(endpoint, never()).sendText(MESSAGE);
    }

    @Test
    public void shouldKeepMessagesIfSessionIsClosed() {
        reSender.add(ENDPOINT_ID, MESSAGE);

        when(session.isOpen()).thenReturn(false);
        reSender.resend(ENDPOINT_ID);

        verify(session, never()).getAsyncRemote();
        verify(endpoint, never()).sendText(MESSAGE);

        when(session.isOpen()).thenReturn(true);
        reSender.resend(ENDPOINT_ID);

        verify(session).getAsyncRemote();
        verify(endpoint).sendText(MESSAGE);
    }

    @Test
    public void shouldProperlyAddForSingleEndpoint() {
        reSender.add(ENDPOINT_ID, MESSAGE);

        reSender.resend(ENDPOINT_ID);

        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(session).getAsyncRemote();
        verify(endpoint).sendText(MESSAGE);
    }

    @Test
    public void shouldProperlyAddForSeveralEndpoints() {
        reSender.add(ENDPOINT_ID, MESSAGE);
        reSender.add("1", MESSAGE);

        reSender.resend(ENDPOINT_ID);
        reSender.resend("1");

        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(sessionRegistry).get("1");
        verify(session, times(2)).getAsyncRemote();
        verify(endpoint, times(2)).sendText(MESSAGE);
    }

    @Test
    public void shouldClearOnExtractionForSingleEndpoint() {
        reSender.add(ENDPOINT_ID, MESSAGE);

        reSender.resend(ENDPOINT_ID);
        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(session).getAsyncRemote();
        verify(endpoint).sendText(MESSAGE);

        reSender.resend(ENDPOINT_ID);
        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(session).getAsyncRemote();
        verify(endpoint).sendText(MESSAGE);
    }

    @Test
    public void shouldClearOnExtractionForSeveralEndpoint() {
        reSender.add(ENDPOINT_ID, MESSAGE);
        reSender.add("1", MESSAGE);

        reSender.resend(ENDPOINT_ID);
        reSender.resend("1");

        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(sessionRegistry).get("1");
        verify(session, times(2)).getAsyncRemote();
        verify(endpoint, times(2)).sendText(MESSAGE);

        reSender.resend(ENDPOINT_ID);
        reSender.resend("1");

        verify(sessionRegistry).get(ENDPOINT_ID);
        verify(sessionRegistry).get("1");
        verify(session, times(2)).getAsyncRemote();
        verify(endpoint, times(2)).sendText(MESSAGE);
    }
}
