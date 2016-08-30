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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link BasicWebSocketTransmissionValidator}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class BasicWebSocketTransmissionValidatorTest {
    private static final String REGISTERED_PROTOCOL     = "registered-protocol";
    private static final String NOT_REGISTERED_PROTOCOL = "not-registered-protocol";
    private static final String VALID_JSON              = "{ \"name\": \"value\" }";
    private static final String NOT_VALID_JSON          = "not valid json";

    @Mock
    private Map<String, WebSocketMessageReceiver> receivers;

    private BasicWebSocketTransmissionValidator   validator;

    @Mock
    private WebSocketTransmission transmission;

    @BeforeMethod
    public void beforeMethod() {
        when(transmission.getProtocol()).thenReturn(REGISTERED_PROTOCOL);
        when(transmission.getMessage()).thenReturn(VALID_JSON);

        when(receivers.keySet()).thenReturn(Collections.singletonMap(REGISTERED_PROTOCOL, mock(WebSocketMessageReceiver.class)).keySet());
        validator = new BasicWebSocketTransmissionValidator(receivers);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void validateTransmissionShouldThrowExceptionIfProtocolIsNull() {
        when(transmission.getProtocol()).thenReturn(null);

        validator.validate(transmission);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfProtocolIsEmpty() {
        when(transmission.getProtocol()).thenReturn("");

        validator.validate(transmission);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNull() {
        when(transmission.getMessage()).thenReturn(null);

        validator.validate(transmission);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsEmpty() {
        when(transmission.getMessage()).thenReturn("");

        validator.validate(transmission);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfProtocolIsNotRegistered() {
        when(transmission.getProtocol()).thenReturn(NOT_REGISTERED_PROTOCOL);

        validator.validate(transmission);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNotValidJson() {
        when(transmission.getMessage()).thenReturn(NOT_VALID_JSON);

        validator.validate(transmission);
    }

    @Test
    public void validateReceptionShouldPassForValidWebSocketTransmission() {
        validator.validate(transmission);
    }
}
