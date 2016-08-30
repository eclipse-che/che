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

import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketTransmissionValidator;
import org.eclipse.che.api.core.websocket.impl.WebSocketTransmissionValidator;
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;


/**
 * Tests for {@link BasicWebSocketTransmissionValidator}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
class BasicWebSocketTransmissionValidatorTest {
    private static final String VALID_JSON              = "{ \"name\": \"value\" }";
    private static final String NOT_VALID_JSON          = "not valid json";
    private static final String REGISTERED_PROTOCOL     = "registered-protocol";
    private static final String NOT_REGISTERED_PROTOCOL = "not-registered-protocol";
    @Mock
    private Map<String, WebSocketMessageReceiver> receivers;
    @InjectMocks
    private WebSocketTransmissionValidator        validator;

    @Mock
    private WebSocketTransmission message;

    @Before
    public void before() {
        when(receivers.keySet()).thenReturn(Collections.singleton(REGISTERED_PROTOCOL));
    }

    @Before
    public void beforeMethod() {
        when(message.getProtocol()).thenReturn(REGISTERED_PROTOCOL);
        when(message.getMessage()).thenReturn(VALID_JSON);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfProtocolIsNull() {
        when(message.getProtocol()).thenReturn(null);

        validator.validate(message);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfProtocolIsEmpty() {
        when(message.getProtocol()).thenReturn("");

        validator.validate(message);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNull() {
        when(message.getMessage()).thenReturn(null);

        validator.validate(message);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsEmpty() {
        when(message.getMessage()).thenReturn("");

        validator.validate(message);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfProtocolIsNotRegistered() {
        when(message.getProtocol()).thenReturn(NOT_REGISTERED_PROTOCOL);

        validator.validate(message);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNotValidJson() {
        when(message.getMessage()).thenReturn(NOT_VALID_JSON);

        validator.validate(message);
    }

    @Test
    public void validateTransmissionShouldPassForValidWebSocketMessage() {
        validator.validate(message);
    }
}
