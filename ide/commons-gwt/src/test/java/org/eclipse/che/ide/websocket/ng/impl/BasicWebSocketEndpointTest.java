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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link BasicWebSocketEndpoint}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicWebSocketEndpointTest {
    private static final String MESSAGE = "message";

    @Mock
    private WebSocketConnectionSustainer    sustainer;
    @Mock
    private WebSocketTransmissionDispatcher dispatcher;
    @Mock
    private PendingMessagesReSender         reSender;
    @InjectMocks
    private BasicWebSocketEndpoint          endpoint;

    @Test
    public void shouldResetSustainerOnOpen() {
        endpoint.onOpen();

        verify(sustainer).reset();
    }

    @Test
    public void shouldResendMessagesOnOpen() {
        endpoint.onOpen();

        verify(reSender).resend();
    }

    @Test
    public void shouldSustainSessionOnClose() {
        endpoint.onClose();

        verify(sustainer).sustain();
    }

    @Test
    public void shouldRunReceiverOnMessage() {
        endpoint.onMessage(MESSAGE);

        verify(dispatcher).dispatch(eq(MESSAGE));
    }
}
