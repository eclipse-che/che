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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketConnectionManager}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionManagerTest {
    @Mock
    private WebSocketFactory           webSocketFactory;
    @InjectMocks
    private WebSocketConnectionManager connectionManager;

    @Mock
    private WebSocketConnection connection;

    @Before
    public void setUp() throws Exception {
        when(webSocketFactory.create("url")).thenReturn(connection);

        connectionManager.initializeConnection("url");
    }

    @Test
    public void shouldCreateConnectionOnInitialize() {
        verify(webSocketFactory).create("url");
    }

    @Test
    public void shouldOpenOnEstablishConnection(){
        connectionManager.establishConnection("url");

        verify(connection).open();
    }

    @Test
    public void shouldCloseOnCloseConnection(){
        connectionManager.closeConnection("url");

        verify(connection).close();
    }

    @Test
    public void shouldSendOnSendMessage(){
        connectionManager.sendMessage("url", "message");

        verify(connection).send("message");
    }

    @Test
    public void shouldReturnTrueWhenConnectionIsOpened(){
        when(connection.isOpen()).thenReturn(true);

        final boolean opened = connectionManager.isConnectionOpen("url");

        assertTrue(opened);

    }

    @Test
    public void shouldReturnFalseWhenConnectionIsClosed(){
        when(connection.isOpen()).thenReturn(false);

        final boolean opened = connectionManager.isConnectionOpen("url");

        assertFalse(opened);
    }
}
