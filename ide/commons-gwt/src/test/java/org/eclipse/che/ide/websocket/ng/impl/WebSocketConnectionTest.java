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
package org.eclipse.che.ide.websocket.ng.impl;

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketConnection}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionTest {
    private static final String URL     = "url";
    private static final int    DELAY   = 0;
    private static final String MESSAGE = "message";
    @Mock
    private WebSocketCreator    connector;
    @InjectMocks
    private WebSocketConnection connection;

    @Mock
    private WebSocket webSocket;

    @Before
    public void before() {
        when(connector.create(anyString(), any())).thenReturn(webSocket);

        connection.initialize(URL);
        connection.open(DELAY);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNoUrlIsSetWhenTryToOpen() {
        connection.initialize(null);
        connection.open(DELAY);
    }

    @Test
    public void shouldCreateConnectionOnOpen() {
        verify(connector).create(URL, DELAY);
    }

    @Test
    public void shouldOpenConnectionOnOpen() {
        verify(webSocket).open();
    }

    @Test
    public void shouldCloseConnectionOnClose() {
        connection.close();

        verify(webSocket).close();
    }

    @Test
    public void shouldSendMessageOnSend() {
        final WebSocketTransmission message = mock(WebSocketTransmission.class);
        when(message.toString()).thenReturn(MESSAGE);

        connection.send(message);

        verify(webSocket).send(MESSAGE);
    }


    @Test
    public void shouldBeOpenWhenConnectionIsOpen() {
        when(webSocket.isOpen()).thenReturn(true);

        assertTrue(connection.isOpen());

        verify(webSocket).isOpen();
    }

    @Test
    public void shouldBeNotOpenWhenConnectionIsNull() {
        when(connector.create(anyString(), any())).thenReturn(null);
        new WebSocketConnection(connector).isOpen();

        assertFalse(connection.isOpen());
    }

    @Test
    public void shouldBeNotOpenWhenConnectionIsNotOpen() {
        when(webSocket.isOpen()).thenReturn(false);

        assertFalse(connection.isOpen());

        verify(webSocket).isOpen();
    }
}
