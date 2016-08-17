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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Instance is responsible for resending messages that were sent during the period
 * when web socket session was closed.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class PendingMessagesReSender {
    private static final int MAX_MESSAGES = 100;

    private final List<WebSocketTransmission> messages = new LinkedList<>();

    private final WebSocketConnection connection;

    @Inject
    public PendingMessagesReSender(WebSocketConnection connection) {
        this.connection = connection;
    }

    public void add(WebSocketTransmission message) {
        if (messages.size() <= MAX_MESSAGES) {
            messages.add(message);
        }
    }

    public void resend() {
        if (messages.isEmpty()) {
            return;
        }

        final List<WebSocketTransmission> backing = new ArrayList<>(messages);
        messages.clear();

        for (WebSocketTransmission message : backing) {
            if (connection.isOpen()) {
                connection.send(message);
            } else {
                messages.add(message);
            }
        }
    }
}
