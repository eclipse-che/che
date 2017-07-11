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
