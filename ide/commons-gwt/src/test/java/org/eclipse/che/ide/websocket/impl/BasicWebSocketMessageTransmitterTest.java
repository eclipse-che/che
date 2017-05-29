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
package org.eclipse.che.ide.websocket.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
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
    private static final String ENDPOINT_ID = "endpointId";
    private static final String URL         = "url";
    private static final String MESSAGE     = "message";

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
        when(urlResolver.getUrl(ENDPOINT_ID)).thenReturn(URL);
        when(urlResolver.resolve(URL)).thenReturn(ENDPOINT_ID);
    }

    @Test
    public void shouldResolveUrlOnTransmit() {
        transmitter.transmit(ENDPOINT_ID, MESSAGE);

        verify(urlResolver).getUrl(ENDPOINT_ID);
    }

    @Test
    public void shouldCheckIfConnectionIsOpenOnTransmit() {
        transmitter.transmit(ENDPOINT_ID, MESSAGE);

        verify(connectionManager).isConnectionOpen(anyString());
    }

    @Test
    public void shouldSendMessageIfConnectionIsOpenOnTransmit() {
        when(connectionManager.isConnectionOpen(anyString())).thenReturn(true);

        transmitter.transmit(ENDPOINT_ID, MESSAGE);

        verify(connectionManager).sendMessage(URL, MESSAGE);
        verify(reSender, never()).add(URL, MESSAGE);
    }

    @Test
    public void shouldAddMessageToReSenderIfConnectionIsNotOpenOnTransmit() {
        when(connectionManager.isConnectionOpen(anyString())).thenReturn(false);

        transmitter.transmit(ENDPOINT_ID, MESSAGE);

        verify(connectionManager, never()).sendMessage(URL, MESSAGE);
        verify(reSender).add(ENDPOINT_ID, MESSAGE);
    }
}
