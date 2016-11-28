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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MessagesReSender}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesReSenderTest {
    @Mock
    private WebSocketConnectionManager connectionManager;
    @InjectMocks
    private MessagesReSender           reSender;

    @Test
    public void shouldResendAllMessages() {
        reSender.add("url", "1");
        reSender.add("url", "2");
        reSender.add("url", "3");

        when(connectionManager.isConnectionOpen("url")).thenReturn(true);

        reSender.reSend("url");
        verify(connectionManager, times(3)).sendMessage(eq("url"), anyString());
    }

    @Test
    public void shouldStopSendingIfSessionIsClosed() {
        reSender.add("url", "1");
        reSender.add("url", "2");
        reSender.add("url", "3");

        final int[] i = {0};
        when(connectionManager.isConnectionOpen("url")).thenAnswer(invocation -> (i[0]++ <= 1));
        reSender.reSend("url");
        verify(connectionManager, times(2)).sendMessage(eq("url"), anyString());

        when(connectionManager.isConnectionOpen("url")).thenReturn(true);
        reSender.reSend("url");

        verify(connectionManager, times(3)).sendMessage(eq("url"), anyString());
    }
}
