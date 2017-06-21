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
