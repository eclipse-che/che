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

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PendingMessagesReSender}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class PendingMessagesReSenderTest {
    @Mock
    private WebSocketConnection     connection;
    @InjectMocks
    private PendingMessagesReSender reSender;

    @Test
    public void shouldResendAllMessages() {
        reSender.add(mock(WebSocketTransmission.class));
        reSender.add(mock(WebSocketTransmission.class));
        reSender.add(mock(WebSocketTransmission.class));

        when(connection.isOpen()).thenReturn(true);

        reSender.resend();
        verify(connection, times(3)).send(any(WebSocketTransmission.class));

        reSender.resend();
        verify(connection, times(3)).send(any(WebSocketTransmission.class));


    }

    @Test
    public void shouldStopSendingIfSessionIsClosed() {
        reSender.add(mock(WebSocketTransmission.class));
        reSender.add(mock(WebSocketTransmission.class));
        reSender.add(mock(WebSocketTransmission.class));

        final int[] i = {0};
        when(connection.isOpen()).thenAnswer(invocation -> (i[0]++ <= 1));
        reSender.resend();
        verify(connection, times(2)).send(any(WebSocketTransmission.class));

        when(connection.isOpen()).thenReturn(true);
        reSender.resend();

        verify(connection, times(3)).send(any(WebSocketTransmission.class));
    }
}
