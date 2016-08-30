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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketJsonRpcTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcTransmitterTest {
    private static final String PROTOCOL = "jsonrpc-2.0";
    private static final String MESSAGE  = "message";
    private static final String TYPE     = "registered-type";
    @Mock
    private WebSocketMessageTransmitter transmitter;
    @Mock
    private DtoFactory                  dtoFactory;
    @Mock
    private JsonRpcObjectValidator      validator;
    @InjectMocks
    private WebSocketJsonRpcTransmitter jsonRpcTransmitter;

    @Mock
    private JsonRpcObject object;

    @Before
    public void before() {
        when(dtoFactory.createDto(eq(JsonRpcObject.class))).thenReturn(object);
        when(object.toString()).thenReturn(MESSAGE);
        when(object.withMessage(anyString())).thenReturn(object);
        when(object.withType(anyString())).thenReturn(object);
    }

    @Test
    public void shouldCreateJsonRpcObject() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE);

        verify(dtoFactory).createDto(JsonRpcObject.class);
    }

    @Test
    public void shouldProperlySetJsonRpcObjectProperties() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE);

        verify(object).withType(TYPE);
        verify(object).withMessage(MESSAGE);
    }

    @Test
    public void shouldValidateJsonRpcObject() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE);

        verify(validator).validate(object);
    }

    @Test
    public void shouldTransmitJsonRpcObject() {
        jsonRpcTransmitter.transmit(TYPE, MESSAGE);

        verify(transmitter).transmit(PROTOCOL, MESSAGE);
    }
}
