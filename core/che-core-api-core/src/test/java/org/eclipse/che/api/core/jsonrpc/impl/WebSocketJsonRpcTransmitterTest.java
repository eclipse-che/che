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
package org.eclipse.che.api.core.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link WebSocketJsonRpcTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketJsonRpcTransmitterTest {
    private static final String PROTOCOL    = "jsonrpc-2.0";
    private static final String MESSAGE     = "message";
    private static final String TYPE        = "registered-type";
    private static final int    ENDPOINT_ID = 0;

    @Mock
    private WebSocketMessageTransmitter transmitter;
    @Mock
    private JsonRpcObjectValidator      validator;
    @InjectMocks
    private WebSocketJsonRpcTransmitter jsonRpcTransmitter;

    private JsonRpcObject object;

    @BeforeMethod
    public void before() {
        object = newDto(JsonRpcObject.class).withType(TYPE).withMessage(MESSAGE);
    }

    @Test
    public void shouldValidateJsonRpcObjectWithoutEndpointSet() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE);

        verify(validator).validate(object);
    }

    @Test
    public void shouldTransmitJsonRpcObjectWithoutEndpointSet() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE);

        verify(transmitter).transmit(PROTOCOL, object.toString());
    }

    @Test
    public void shouldValidateJsonRpcObjectWithEndpoint() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE, ENDPOINT_ID);

        verify(validator).validate(object);
    }

    @Test
    public void shouldTransmitJsonRpcObjectWithEndpoint() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE, ENDPOINT_ID);

        verify(transmitter).transmit(PROTOCOL, object.toString(), ENDPOINT_ID);
    }
}
