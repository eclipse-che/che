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
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketTransmissionDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketTransmissionDispatcherTest {
    private static final int    ENDPOINT_ID             = 0;
    private static final String REGISTERED_PROTOCOL     = "registered-protocol";
    private static final String NOT_REGISTERED_PROTOCOL = "not-registered-protocol";
    private static final String MESSAGE                 = "message";
    @Mock
    private Map<String, WebSocketMessageReceiver> receivers;
    @Mock
    private WebSocketTransmissionValidator        validator;
    @InjectMocks
    private WebSocketTransmissionDispatcher       dispatcher;

    @Mock
    private WebSocketMessageReceiver receiver;

    private WebSocketTransmission transmission;

    @BeforeMethod
    public void before() {
        when(receivers.entrySet()).thenReturn(Collections.singletonMap(REGISTERED_PROTOCOL, receiver).entrySet());

        transmission = newDto(WebSocketTransmission.class).withProtocol(REGISTERED_PROTOCOL).withMessage(MESSAGE);
    }

    @Test
    public void shouldValidateWebSocketTransmission() {
        dispatcher.dispatch(transmission.toString(), ENDPOINT_ID);

        verify(validator).validate(transmission);
    }

    @Test
    public void shouldRunReceiverOnMatch() {
        dispatcher.dispatch(transmission.toString(), ENDPOINT_ID);

        verify(receiver).receive(MESSAGE, ENDPOINT_ID);
    }

    @Test
    public void shouldNotRunReceiverOnNoMatch() {
        transmission.withProtocol(NOT_REGISTERED_PROTOCOL);

        dispatcher.dispatch(transmission.toString(), ENDPOINT_ID);

        verify(receiver, never()).receive(MESSAGE, ENDPOINT_ID);
    }
}
