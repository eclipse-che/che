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

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PendingMessagesReSender}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class PendingMessagesReSenderTest {
    private static final String MESSAGE     = "message";
    private static final int    ENDPOINT_ID = 0;

    @Mock
    private WebSocketSessionRegistry sessionRegistry;
    @InjectMocks
    private PendingMessagesReSender  resender;

    @Mock
    private Session               session;
    @Mock
    private RemoteEndpoint.Async  endpoint;
    @Mock
    private WebSocketTransmission transmission;

    @BeforeMethod
    public void beforeMethod() {
        when(sessionRegistry.get(any(Integer.class))).thenReturn(Optional.of(session));
        when(session.getAsyncRemote()).thenReturn(endpoint);
        when(session.isOpen()).thenReturn(true);
        when(transmission.toString()).thenReturn(MESSAGE);
    }

    @BeforeMethod
    public void before() {
        resender = new PendingMessagesReSender(sessionRegistry);
    }

    @Test
    public void shouldStopIfSessionIsNotRegistered() {
        when(sessionRegistry.get(any(Integer.class))).thenReturn(Optional.empty());

        resender.add(ENDPOINT_ID, transmission);

        resender.resend(ENDPOINT_ID);

        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(session, never()).getAsyncRemote();
        verify(endpoint, never()).sendText(eq(MESSAGE));
    }

    @Test
    public void shouldKeepMessagesIfSessionIsClosed() {
        resender.add(ENDPOINT_ID, transmission);

        when(session.isOpen()).thenReturn(false);
        resender.resend(ENDPOINT_ID);

        verify(session, never()).getAsyncRemote();
        verify(endpoint, never()).sendText(eq(MESSAGE));

        when(session.isOpen()).thenReturn(true);
        resender.resend(ENDPOINT_ID);

        verify(session).getAsyncRemote();
        verify(endpoint).sendText(eq(MESSAGE));
    }

    @Test
    public void shouldProperlyAddForSingleEndpoint() {
        resender.add(ENDPOINT_ID, transmission);

        resender.resend(ENDPOINT_ID);

        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(session).getAsyncRemote();
        verify(endpoint).sendText(eq(MESSAGE));
    }

    @Test
    public void shouldProperlyAddForSeveralEndpoints() {
        resender.add(ENDPOINT_ID, transmission);
        resender.add(1, transmission);

        resender.resend(ENDPOINT_ID);
        resender.resend(1);

        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(sessionRegistry).get(eq(1));
        verify(session, times(2)).getAsyncRemote();
        verify(endpoint, times(2)).sendText(eq(MESSAGE));
    }

    @Test
    public void shouldClearOnExtractionForSingleEndpoint() {
        resender.add(ENDPOINT_ID, transmission);

        resender.resend(ENDPOINT_ID);
        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(session).getAsyncRemote();
        verify(endpoint).sendText(eq(MESSAGE));

        resender.resend(ENDPOINT_ID);
        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(session).getAsyncRemote();
        verify(endpoint).sendText(eq(MESSAGE));
    }

    @Test
    public void shouldClearOnExtractionForSeveralEndpoint() {
        resender.add(ENDPOINT_ID, transmission);
        resender.add(1, transmission);

        resender.resend(ENDPOINT_ID);
        resender.resend(1);

        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(sessionRegistry).get(eq(1));
        verify(session, times(2)).getAsyncRemote();
        verify(endpoint, times(2)).sendText(eq(MESSAGE));

        resender.resend(ENDPOINT_ID);
        resender.resend(1);

        verify(sessionRegistry).get(eq(ENDPOINT_ID));
        verify(sessionRegistry).get(eq(1));
        verify(session, times(2)).getAsyncRemote();
        verify(endpoint, times(2)).sendText(eq(MESSAGE));
    }
}
