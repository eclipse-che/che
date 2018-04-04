/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.websocket.impl;

import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.slf4j.Logger;

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

  public Optional<Session> remove(String endpointId) {
    LOG.debug("Cancelling registration for session with endpoint {}", endpointId);

    return Optional.ofNullable(sessionsMap.remove(endpointId));
  }

  public Optional<Session> remove(Session session) {
    return get(session).flatMap(id -> Optional.ofNullable(sessionsMap.remove(id)));
  }

  public Optional<Session> get(String endpointId) {
    return Optional.ofNullable(sessionsMap.get(endpointId));
  }

  public Set<Session> getByPartialMatch(String partialEndpointId) {
    return sessionsMap
        .entrySet()
        .stream()
        .filter(it -> it.getKey().contains(partialEndpointId))
        .map(Map.Entry::getValue)
        .collect(toSet());
  }

  public Optional<String> get(Session session) {
    return sessionsMap
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().equals(session))
        .map(Map.Entry::getKey)
        .findAny();
  }

  public Set<Session> getSessions() {
    return new HashSet<>(sessionsMap.values());
  }
}
