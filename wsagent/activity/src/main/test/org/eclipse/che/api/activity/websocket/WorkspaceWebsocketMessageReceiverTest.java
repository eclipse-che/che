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
package org.eclipse.che.api.activity.websocket;

import org.eclipse.che.api.activity.WorkspaceActivityNotifier;
import org.everrest.websockets.message.InputMessage;
import org.everrest.websockets.message.Message;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RestInputMessage;
import org.everrest.websockets.message.RestOutputMessage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link WorkspaceWebsocketMessageReceiver}
 *
 * @author Mihail Kuznyetsov
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceWebsocketMessageReceiverTest {

    @Mock
    WorkspaceActivityNotifier workspaceActivityNotifier;

    @Mock
    RestInputMessage inputMessage;

    @InjectMocks
    WorkspaceWebsocketMessageReceiver receiver;

    @Test
    public void shouldNotifyWorkspaceActivityOnInputMessage() {
        doReturn(new Pair[]{}).when(inputMessage).getHeaders();
        receiver.onMessage(inputMessage);

        verify(workspaceActivityNotifier).onActivity();
    }

    @Test
    public void shouldNotNotifyWorkspaceActivityOnInputMessageWithPingHeaders() {
        doReturn(new Pair[]{new Pair("x-everrest-websocket-message-type", "ping")}).when(inputMessage).getHeaders();
        receiver.onMessage(inputMessage);

        verifyZeroInteractions(workspaceActivityNotifier);
    }
}
