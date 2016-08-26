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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketJsonRpcRequestTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcRequestTransmitterTest {
    private static final String TYPE        = "request";
    private static final int    ID          = 0;
    private static final String METHOD_NAME = "method-name";
    private static final String MESSAGE     = "message";
    @Mock
    private WebSocketJsonRpcTransmitter        transmitter;
    @Mock
    private DtoFactory                         dtoFactory;
    @Mock
    private JsonRpcRequestRegistry             requestRegistry;
    @InjectMocks
    private WebSocketJsonRpcRequestTransmitter jsonRpcTransmitter;

    @Mock
    private WebSocketTransmission transmission;
    @Mock
    private JsonRpcRequest        request;

    @Before
    public void before() {
        when(request.getId()).thenReturn(ID);
        when(request.getMethod()).thenReturn(METHOD_NAME);
        when(request.toString()).thenReturn(MESSAGE);

        when(transmission.withProtocol(anyString())).thenReturn(transmission);
        when(transmission.withMessage(anyString())).thenReturn(transmission);

        when(dtoFactory.createDto(WebSocketTransmission.class)).thenReturn(transmission);
    }

    @Test
    public void shouldAddMethodToRequestRegistry() {
        jsonRpcTransmitter.transmit(request);

        verify(requestRegistry).add(ID, METHOD_NAME);
    }

    @Test
    public void shouldRunWebSocketTransmitter() {
        jsonRpcTransmitter.transmit(request);

        verify(transmitter).transmit(TYPE, MESSAGE);
    }
}
