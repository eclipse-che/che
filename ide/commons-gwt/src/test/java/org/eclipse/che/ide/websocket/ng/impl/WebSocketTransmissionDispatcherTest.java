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
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link WebSocketTransmissionDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketTransmissionDispatcherTest {
    private static final String REGISTERED_PROTOCOL     = "registered-protocol";
    private static final String NOT_REGISTERED_PROTOCOL = "not-registered-protocol";
    private static final String MESSAGE                 = "message";
    private static final String RAW_TRANSMISSION        = "raw_transmission";
    @Mock
    private Map<String, WebSocketMessageReceiver> receivers;
    @Mock
    private WebSocketTransmissionValidator        validator;
    @Mock
    private DtoFactory                            dtoFactory;
    @InjectMocks
    private WebSocketTransmissionDispatcher       dispatcher;

    @Mock
    private WebSocketTransmission    transmission;
    @Mock
    private WebSocketMessageReceiver receiver;

    @Before
    public void before() {
        when(dtoFactory.createDtoFromJson(any(), eq(WebSocketTransmission.class))).thenReturn(transmission);

        when(transmission.getProtocol()).thenReturn(REGISTERED_PROTOCOL);
        when(transmission.getMessage()).thenReturn(MESSAGE);

        when(receivers.entrySet()).thenReturn(singletonMap(REGISTERED_PROTOCOL, receiver).entrySet());
    }

    @Test
    public void shouldCreateWebSocketTransmission() {
        dispatcher.dispatch(RAW_TRANSMISSION);

        verify(dtoFactory).createDtoFromJson(RAW_TRANSMISSION, WebSocketTransmission.class);
    }

    @Test
    public void shouldValidateWebSocketTransmission() {
        dispatcher.dispatch(RAW_TRANSMISSION);

        verify(validator).validate(transmission);
    }

    @Test
    public void shouldRunReceiverOnMatch() {
        dispatcher.dispatch(RAW_TRANSMISSION);

        verify(receiver).receive(MESSAGE);
    }

    @Test
    public void shouldNotRunReceiverOnNoMatch() {
        when(receivers.entrySet()).thenReturn(singletonMap(NOT_REGISTERED_PROTOCOL, receiver).entrySet());

        dispatcher.dispatch(RAW_TRANSMISSION);

        verify(receiver, never()).receive(MESSAGE);
    }
}
