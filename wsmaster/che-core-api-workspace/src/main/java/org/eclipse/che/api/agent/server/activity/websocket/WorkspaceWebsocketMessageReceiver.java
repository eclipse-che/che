/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.eclipse.che.api.agent.server.activity.websocket;

import org.eclipse.che.api.agent.server.activity.WorkspaceActivityNotifier;
import org.everrest.websockets.WSMessageReceiver;
import org.everrest.websockets.message.InputMessage;
import org.everrest.websockets.message.Pair;
import org.everrest.websockets.message.RestInputMessage;

import java.util.Arrays;

/**
 * Updates workspace activity on message receival by websocket.
 *
 * @author Mihail Kuznyetsov
 * @author Anton Korneta
 */
public class WorkspaceWebsocketMessageReceiver implements WSMessageReceiver {

    private final WorkspaceActivityNotifier workspaceActivityNotifier;

    public WorkspaceWebsocketMessageReceiver(WorkspaceActivityNotifier workspaceActivityNotifier) {
        this.workspaceActivityNotifier = workspaceActivityNotifier;
    }

    @Override
    public void onMessage(InputMessage input) {
        // only user activity matters
        if (input instanceof RestInputMessage) {
            final Pair[] headers = ((RestInputMessage)input).getHeaders();
            final boolean containsPingHeader = Arrays.stream(headers)
                                                     .anyMatch(pair -> "x-everrest-websocket-message-type".equals(pair.getName())
                                                                       && "ping".equals(pair.getValue()));

            if (!containsPingHeader) {
                workspaceActivityNotifier.onActivity();
            }
        } else {
            workspaceActivityNotifier.onActivity();
        }
    }

    @Override
    public void onError(Exception error) {
        workspaceActivityNotifier.onActivity();
    }
}
