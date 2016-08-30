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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
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
 * Tests for {@link WebSocketJsonRpcResponseTransmitter}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcResponseTransmitterTest {
    private static final String MESSAGE = "message";
    private static final String TYPE    = "response";

    @Mock
    private WebSocketJsonRpcTransmitter         transmitter;
    @Mock
    private DtoFactory                          dtoFactory;
    @InjectMocks
    private WebSocketJsonRpcResponseTransmitter jsonRpcTransmitter;

    @Mock
    private WebSocketTransmission transmission;
    @Mock
    private JsonRpcResponse       response;

    @Before
    public void before() {
        when(response.toString()).thenReturn(MESSAGE);

        when(transmission.withProtocol(anyString())).thenReturn(transmission);
        when(transmission.withMessage(anyString())).thenReturn(transmission);

        when(dtoFactory.createDto(WebSocketTransmission.class)).thenReturn(transmission);
    }

    @Test
    public void shouldRunWebSocketTransmitter() {
        jsonRpcTransmitter.transmit(response);

        verify(transmitter).transmit(TYPE, MESSAGE);
    }
}
