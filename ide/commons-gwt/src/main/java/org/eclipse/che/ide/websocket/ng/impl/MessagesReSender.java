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

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Caches messages that was transmitted when a web socket connection
 * was not opened and resends them when the connection is opened again.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class MessagesReSender {
    private static final int MAX_MESSAGES = 100;

    private final Map<String, List<String>> messageRegistry = new HashMap<>();

    private final WebSocketConnectionManager connectionManager;

    @Inject
    public MessagesReSender(WebSocketConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Add message that is to be sent when a connection defined be the URL
     * is opened again.
     *
     * @param url
     *         url of a web socket connection
     * @param message
     *         plain text message
     */
    public void add(String url, String message) {
        if (!messageRegistry.containsKey(url)) {
            final LinkedList<String> newList = new LinkedList<>();
            messageRegistry.put(url, newList);
        }

        final List<String> webSocketTransmissions = messageRegistry.get(url);
        if (webSocketTransmissions.size() <= MAX_MESSAGES) {
            webSocketTransmissions.add(message);
        }
    }

    public void reSend(String url) {
        if (!messageRegistry.containsKey(url)) {
            return;
        }

        final List<String> messages = messageRegistry.get(url);
        if (messages.isEmpty()) {
            return;
        }

        Log.info(getClass(), "Going to resend websocket messaged: " + messages);

        final List<String> backing = new ArrayList<>(messages);
        messages.clear();

        for (String message : backing) {
            if (connectionManager.isConnectionOpen(url)) {
                connectionManager.sendMessage(url, message);
            } else {
                messages.add(message);
            }
        }
    }
}
