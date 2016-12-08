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

import org.slf4j.Logger;

import javax.inject.Singleton;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Binds WEB SOCKET session to a specific endpoint form which it was opened.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketSessionRegistry {
    private static final Logger LOG = getLogger(WebSocketSessionRegistry.class);

    private final Map<String, Session> sessionsMap = new ConcurrentHashMap<>();

    public void add(String endpointId, Session session) {
        LOG.debug("Registering session with endpoint {}", session.getId(), endpointId);

        sessionsMap.put(endpointId, session);
    }

    public void remove(String endpointId) {
        LOG.debug("Cancelling registration for session with endpoint {}", endpointId);

        sessionsMap.remove(endpointId);
    }

    public Optional<Session> get(String endpointId) {
        return Optional.ofNullable(sessionsMap.get(endpointId));
    }

    public Set<Session> getSessions() {
        return sessionsMap.values().stream().collect(toSet());
    }
}
