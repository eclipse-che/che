/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.websocket.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.eclipse.che.commons.schedule.ScheduleRate;

/**
 * Instance is responsible for re-sending messages that were not sent during the period when WEB
 * SOCKET session was closed. If session is closed during re-send process it stops and left messages
 * will be re-sent as WEB SOCKET session becomes open again.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class MessagesReSender {

  private static final int MAX_MESSAGES = 100;

  private final WebSocketSessionRegistry registry;

  private final Map<String, List<DelayedMessage>> delayedMessageRegistry = new HashMap<>();

  @Inject
  public MessagesReSender(WebSocketSessionRegistry registry) {
    this.registry = registry;
  }

  @ScheduleRate(period = 60)
  void cleanStaleMessages() {
    long currentTimeMillis = System.currentTimeMillis();

    delayedMessageRegistry
        .values()
        .forEach(it -> it.removeIf(m -> currentTimeMillis - m.timeMillis > 60_000));

    delayedMessageRegistry.values().removeIf(List::isEmpty);
  }

  public void add(String endpointId, String message) {
    List<DelayedMessage> delayedMessages =
        delayedMessageRegistry.computeIfAbsent(endpointId, k -> new LinkedList<>());

    if (delayedMessages.size() <= MAX_MESSAGES) {
      delayedMessages.add(new DelayedMessage(message));
    }
  }

  public void resend(String endpointId) {
    final List<DelayedMessage> delayedMessages = delayedMessageRegistry.remove(endpointId);

    if (delayedMessages == null || delayedMessages.isEmpty()) {
      return;
    }

    final Optional<Session> sessionOptional = registry.get(endpointId);

    if (!sessionOptional.isPresent()) {
      return;
    }

    final Session session = sessionOptional.get();

    final List<DelayedMessage> backing = new ArrayList<>(delayedMessages);
    delayedMessages.clear();

    for (DelayedMessage delayedMessage : backing) {

      if (session.isOpen()) {
        session.getAsyncRemote().sendText(delayedMessage.message);
      } else {
        delayedMessages.add(delayedMessage);
      }
    }

    delayedMessageRegistry.put(endpointId, delayedMessages);
  }

  private static class DelayedMessage {

    private final long timeMillis;
    private final String message;

    private DelayedMessage(String message) {
      this.message = message;
      this.timeMillis = System.currentTimeMillis();
    }
  }
}
