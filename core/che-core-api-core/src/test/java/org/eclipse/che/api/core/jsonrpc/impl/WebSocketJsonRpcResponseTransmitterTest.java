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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketJsonRpcResponseTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketJsonRpcResponseTransmitterTest {
    private static final int    ENDPOINT_ID = 0;
    private static final String MESSAGE     = "message";
    private static final String TYPE        = "response";

    @Mock
    private WebSocketJsonRpcTransmitter         transmitter;
    @InjectMocks
    private WebSocketJsonRpcResponseTransmitter responseTransmitter;

    @Mock
    private JsonRpcResponse response;

    @BeforeMethod
    public void before() {
        when(response.toString()).thenReturn(MESSAGE);
    }


    @Test
    public void shouldRunWebSocketTransmitterWithEndpoint() {
        responseTransmitter.transmit(response, ENDPOINT_ID);

        verify(transmitter).transmit(TYPE, MESSAGE, ENDPOINT_ID);
    }

    @Test
    public void shouldRunWebSocketTransmitterWithoutEndpoint() {
        responseTransmitter.transmit(response);

        verify(transmitter).transmit(TYPE, MESSAGE);
    }
}
