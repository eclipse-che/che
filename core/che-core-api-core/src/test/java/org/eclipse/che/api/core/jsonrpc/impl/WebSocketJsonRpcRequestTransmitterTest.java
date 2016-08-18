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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketJsonRpcRequestTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketJsonRpcRequestTransmitterTest {
    private static final int    ENDPOINT_ID = 0;
    private static final int    REQUEST_ID  = 1;
    private static final String MESSAGE     = "message";
    private static final String TYPE        = "request";
    private static final String METHOD_NAME = "method-name";

    @Mock
    private WebSocketJsonRpcTransmitter        transmitter;
    @Mock
    private JsonRpcRequestRegistry             registry;
    @InjectMocks
    private WebSocketJsonRpcRequestTransmitter requestTransmitter;

    @Mock
    private JsonRpcRequest request;

    @BeforeMethod
    public void before() {
        when(request.toString()).thenReturn(MESSAGE);
        when(request.getMethod()).thenReturn(METHOD_NAME);
        when(request.getId()).thenReturn(REQUEST_ID);
    }

    @Test
    public void shouldRunWebSocketTransmitterWithEndpoint() {
        requestTransmitter.transmit(request, ENDPOINT_ID);


        verify(registry).add(eq(REQUEST_ID), eq(METHOD_NAME));
        verify(transmitter).transmit(TYPE, MESSAGE, ENDPOINT_ID);
    }

    @Test
    public void shouldRunWebSocketTransmitterWithoutEndpoint() {
        requestTransmitter.transmit(request);


        verify(registry).add(REQUEST_ID, METHOD_NAME);
        verify(transmitter).transmit(TYPE, MESSAGE);
    }
}
