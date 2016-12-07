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
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BasicWebSocketMessageTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicWebSocketMessageTransmitterTest {
    @Mock
    private WebSocketConnectionManager       connectionManager;
    @Mock
    private MessagesReSender                 reSender;
    @Mock
    private UrlResolver                      urlResolver;
    @InjectMocks
    private BasicWebSocketMessageTransmitter transmitter;

    @Before
    public void setUp() throws Exception {
        when(urlResolver.getUrl("endpointId")).thenReturn("url");
    }

    @Test
    public void shouldResolveUrlOnTransmit() {
        transmitter.transmit("endpointId", "message");

        verify(urlResolver).getUrl("endpointId");
    }

    @Test
    public void shouldCheckIfConnectionIsOpenOnTransmit() {
        transmitter.transmit("endpointId", "message");

        verify(connectionManager).isConnectionOpen(anyString());
    }

    @Test
    public void shouldSendMessageIfConnectionIsOpenOnTransmit() {
        when(connectionManager.isConnectionOpen(anyString())).thenReturn(true);

        transmitter.transmit("endpointId", "message");

        verify(connectionManager).sendMessage("url", "message");
        verify(reSender, never()).add("url", "message");
    }

    @Test
    public void shouldAddMessageToReSenderIfConnectionIsNotOpenOnTransmit() {
        when(connectionManager.isConnectionOpen(anyString())).thenReturn(false);

        transmitter.transmit("endpointId", "message");

        verify(connectionManager, never()).sendMessage("url", "message");
        verify(reSender).add("url", "message");
    }
}
