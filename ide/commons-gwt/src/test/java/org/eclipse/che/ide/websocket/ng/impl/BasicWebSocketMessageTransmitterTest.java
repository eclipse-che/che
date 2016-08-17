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
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
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
    private static final String MESSAGE  = "message";
    private static final String PROTOCOL = "protocol";

    @Mock
    private WebSocketConnection              session;
    @Mock
    private PendingMessagesReSender          reSender;
    @Mock
    private WebSocketTransmissionValidator   validator;
    @Mock
    private DtoFactory                       dtoFactory;
    @InjectMocks
    private BasicWebSocketMessageTransmitter transmitter;

    @Mock
    private WebSocketTransmission transmission;

    @Before
    public void before() {
        when(dtoFactory.createDto(eq(WebSocketTransmission.class))).thenReturn(transmission);

        when(transmission.getMessage()).thenReturn(MESSAGE);
        when(transmission.withMessage(anyString())).thenReturn(transmission);
        when(transmission.withProtocol(anyString())).thenReturn(transmission);
    }

    @Test
    public void shouldValidateTransmission() {
        when(session.isOpen()).thenReturn(true);

        transmitter.transmit(PROTOCOL, MESSAGE);

        verify(validator).validate(transmission);
    }

    @Test
    public void shouldSendMessageIfSessionIsOpen() {
        when(session.isOpen()).thenReturn(true);

        transmitter.transmit(PROTOCOL, MESSAGE);

        verify(session).send(transmission);
        verify(reSender, never()).add(any(WebSocketTransmission.class));
    }

    @Test
    public void shouldAddMessageToPendingIfSessionIsNotOpened() {
        when(session.isOpen()).thenReturn(false);

        transmitter.transmit(PROTOCOL, MESSAGE);

        verify(session, never()).send(any(WebSocketTransmission.class));
        verify(reSender).add(transmission);
    }
}
