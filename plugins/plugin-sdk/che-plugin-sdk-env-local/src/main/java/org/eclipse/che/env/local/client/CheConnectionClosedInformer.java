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
package org.eclipse.che.env.local.client;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.ConnectionClosedInformer;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_ABNORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_FAILURE_TLS_HANDSHAKE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_GOING_AWAY;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_INCONSISTENT_DATA;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NEGOTIATE_EXTENSION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NO_STATUS;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_PROTOCOL_ERROR;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_TOO_LARGE;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNEXPECTED_CONDITION;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_UNSUPPORTED;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_VIOLATE_POLICY;

/**
 * Notify that WebSocket connection was closed.
 *
 * @author Roman Nikitenko
 */
public class CheConnectionClosedInformer implements ConnectionClosedInformer {


    private DialogFactory        dialogFactory;
    private LocalizationConstant localizationConstant;

    @Inject
    CheConnectionClosedInformer(DialogFactory dialogFactory,
                                LocalizationConstant localizationConstant) {
        this.dialogFactory = dialogFactory;
        this.localizationConstant = localizationConstant;
    }

    @Override
    public void onConnectionClosed(WebSocketClosedEvent event) {
        switch (event.getCode()) {
            case CLOSE_ABNORMAL:
                String reason = event.getReason();
                if (reason == null || reason.isEmpty()) {
                    break;
                }
            case CLOSE_NORMAL:
            case CLOSE_GOING_AWAY:
            case CLOSE_PROTOCOL_ERROR:
            case CLOSE_UNSUPPORTED:
            case CLOSE_NO_STATUS:
            case CLOSE_INCONSISTENT_DATA:
            case CLOSE_VIOLATE_POLICY:
            case CLOSE_TOO_LARGE:
            case CLOSE_NEGOTIATE_EXTENSION:
            case CLOSE_UNEXPECTED_CONDITION:
            case CLOSE_FAILURE_TLS_HANDSHAKE:
                showMessageDialog(localizationConstant.connectionClosedDialogTitle(), localizationConstant.messagesServerFailure());
        }
    }

    /**
     * Displays dialog using title and message.
     */
    private void showMessageDialog(String title, String message) {
        dialogFactory.createMessageDialog(title, message, null).show();
    }
}
