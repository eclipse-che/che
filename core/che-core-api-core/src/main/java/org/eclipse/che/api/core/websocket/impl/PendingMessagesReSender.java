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
package org.eclipse.che.api.core.websocket.impl;

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Instance is responsible for re-sending messages that were not sent during the period
 * when WEB SOCKET session was closed. If session is closed during re-send process it
 * stops and left messages will be re-sent as WEB SOCKET session becomes open again.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class PendingMessagesReSender {
    private static final int MAX_MESSAGES = 100;

    private final WebSocketSessionRegistry registry;

    private final Map<Integer, List<WebSocketTransmission>> messagesMap = new HashMap<>();

    @Inject
    public PendingMessagesReSender(WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    public void add(Integer endpointId, WebSocketTransmission message) {
        List<WebSocketTransmission> messages = messagesMap.get(endpointId);

        if (messages == null) {
            messages = new LinkedList<>();
            messagesMap.put(endpointId, messages);
        }

        if (messages.size() <= MAX_MESSAGES) {
            messages.add(message);
        }
    }

    public void resend(Integer endpointId) {
        final List<WebSocketTransmission> messages = messagesMap.remove(endpointId);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        final Optional<Session> sessionOptional = registry.get(endpointId);

        if (!sessionOptional.isPresent()) {
            return;
        }

        final Session session = sessionOptional.get();

        final List<WebSocketTransmission> backing = new ArrayList<>(messages);
        messages.clear();

        for (WebSocketTransmission message : backing) {

            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message.toString());
            } else {
                messages.add(message);
            }
        }

        messagesMap.put(endpointId, messages);
    }
}
