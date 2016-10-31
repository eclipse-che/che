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
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link BasicWebSocketMessageTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class BasicWebSocketTransmissionTransmitterTest {
    private static final String PROTOCOL    = "protocol";
    private static final String MESSAGE     = "message";
    private static final int    ENDPOINT_ID = 0;

    @Mock
    private WebSocketSessionRegistry         registry;
    @Mock
    private PendingMessagesReSender          reSender;
    @Mock
    private WebSocketTransmissionValidator   validator;
    @InjectMocks
    private BasicWebSocketMessageTransmitter transmitter;

    @Mock
    private Session              session;
    @Mock
    private RemoteEndpoint.Basic remote;

    private WebSocketTransmission transmission;


    @BeforeMethod
    public void setUp() throws Exception {
        when(session.getBasicRemote()).thenReturn(remote);
        when(session.isOpen()).thenReturn(true);

        when(registry.get(eq(ENDPOINT_ID))).thenReturn(Optional.of(session));
        when(registry.getSessions()).thenReturn(emptySet());

        transmission = DtoFactory.newDto(WebSocketTransmission.class).withProtocol(PROTOCOL).withMessage(MESSAGE);
    }

    @Test
    public void shouldValidateDirectMessage() {
        transmitter.transmit(PROTOCOL, MESSAGE, ENDPOINT_ID);

        verify(validator).validate(any(WebSocketTransmission.class));
    }

    @Test
    public void shouldValidateBroadcastMessage() {
        transmitter.transmit(PROTOCOL, MESSAGE, ENDPOINT_ID);

        verify(validator).validate(any(WebSocketTransmission.class));
    }

    @Test
    public void shouldSendDirectMessageIfSessionIsOpenAndEndpointIsSet() throws IOException {
        transmitter.transmit(PROTOCOL, MESSAGE, ENDPOINT_ID);

        verify(session).getBasicRemote();
        verify(remote).sendText(transmission.toString());
        verify(reSender, never()).add(eq(ENDPOINT_ID), any(WebSocketTransmission.class));
    }

    @Test
    public void shouldSendBroadcastingMessageIfSessionIsOpen() throws IOException {
        transmitter.transmit(PROTOCOL, MESSAGE);

        verify(session, never()).getBasicRemote();
        verify(remote, never()).sendText(transmission.toString());
        verify(reSender, never()).add(any(), any(WebSocketTransmission.class));

        verify(registry).getSessions();
    }

    @Test
    public void shouldAddMessageToPendingIfSessionIsNotOpenedAndEndpointIsSet() throws IOException {
        when(session.isOpen()).thenReturn(false);

        transmitter.transmit(PROTOCOL, MESSAGE, ENDPOINT_ID);

        verify(session, never()).getBasicRemote();
        verify(remote, never()).sendText(transmission.toString());
        verify(reSender).add(eq(ENDPOINT_ID), any(WebSocketTransmission.class));
    }
}
