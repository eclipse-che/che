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
package org.eclipse.che.api.core.jsonrpc;

import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests for {@link JsonRpcErrorTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcErrorTransmitterTest {
    static final String STRINGIFIED_RESPONSE = "response";
    static final String ENDPOINT_ID          = "endpointId";
    static final String ERROR_MESSAGE        = "message";
    static final String REQUEST_ID           = "0";
    static final int    ERROR_CODE           = 0;

    @Mock
    WebSocketMessageTransmitter transmitter;
    @Mock
    JsonRpcFactory              jsonRpcFactory;
    @InjectMocks
    JsonRpcErrorTransmitter     errorTransmitter;

    @Mock
    JsonRpcException             jsonRpcException;
    @Mock
    JsonRpcResponse              jsonRpcResponse;
    @Mock
    JsonRpcError                 jsonRpcError;
    @Captor
    ArgumentCaptor<JsonRpcError> jsonRpcErrorArgumentCaptor;

    @BeforeMethod
    public void setUp() {
        prepareError();
        prepareFactory();
        prepareException();
    }

    private void prepareFactory() {
        when(jsonRpcFactory.createResponse(anyString(), anyObject(), anyObject())).thenReturn(jsonRpcResponse);
        when(jsonRpcResponse.toString()).thenReturn(STRINGIFIED_RESPONSE);
    }

    private void prepareError() {
        when(jsonRpcFactory.createError(anyInt(), anyString())).thenReturn(jsonRpcError);
        when(jsonRpcError.getCode()).thenReturn(ERROR_CODE);
        when(jsonRpcError.getMessage()).thenReturn(ERROR_MESSAGE);
    }

    private void prepareException() {
        when(jsonRpcException.getCode()).thenReturn(ERROR_CODE);
        when(jsonRpcException.getId()).thenReturn(REQUEST_ID);
        when(jsonRpcException.getMessage()).thenReturn(ERROR_MESSAGE);
    }

    @Test
    public void shouldCreateError() throws Exception {
        errorTransmitter.transmit(ENDPOINT_ID, jsonRpcException);

        verify(jsonRpcFactory).createError(ERROR_CODE, ERROR_MESSAGE);
    }

    @Test
    public void shouldCreateResponse() throws Exception {
        errorTransmitter.transmit(ENDPOINT_ID, jsonRpcException);

        verify(jsonRpcFactory).createResponse(eq(REQUEST_ID), isNull(JsonRpcResult.class), jsonRpcErrorArgumentCaptor.capture());
        assertEquals(ERROR_CODE, jsonRpcErrorArgumentCaptor.getValue().getCode());
        assertEquals(ERROR_MESSAGE, jsonRpcErrorArgumentCaptor.getValue().getMessage());
    }

    @Test
    public void shouldTransmitResponse() throws Exception {
        errorTransmitter.transmit(ENDPOINT_ID, jsonRpcException);

        verify(transmitter).transmit(ENDPOINT_ID, STRINGIFIED_RESPONSE);
    }
}
