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

import com.google.common.collect.EvictingQueue;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.eclipse.che.commons.schedule.ScheduleDelay;

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

  private final Map<String, Queue<DelayedMessage>> delayedMessageRegistry =
      new ConcurrentHashMap<>();

  @Inject
  public MessagesReSender(WebSocketSessionRegistry registry) {
    this.registry = registry;
  }

  @ScheduleDelay(initialDelay = 60, delay = 60)
  void cleanStaleMessages() {
    long currentTimeMillis = System.currentTimeMillis();

    delayedMessageRegistry
        .values()
        .forEach(it -> it.removeIf(m -> currentTimeMillis - m.timeMillis > 60_000));

    delayedMessageRegistry.values().removeIf(Queue::isEmpty);
  }

  public void add(String endpointId, String message) {

    delayedMessageRegistry
        .computeIfAbsent(endpointId, k -> EvictingQueue.create(MAX_MESSAGES))
        .offer(new DelayedMessage(message));
  }

  public void resend(String endpointId) {
    Queue<DelayedMessage> delayedMessages = delayedMessageRegistry.remove(endpointId);

    if (delayedMessages == null || delayedMessages.isEmpty()) {
      return;
    }

    Optional<Session> sessionOptional = registry.get(endpointId);

    if (!sessionOptional.isPresent()) {
      return;
    }

    Queue<DelayedMessage> backingQueue = EvictingQueue.create(delayedMessages.size());
    while (!delayedMessages.isEmpty()) {
      backingQueue.offer(delayedMessages.poll());
    }

    Session session = sessionOptional.get();
    for (DelayedMessage delayedMessage : backingQueue) {
      if (session.isOpen()) {
        session.getAsyncRemote().sendText(delayedMessage.message);
      } else {
        delayedMessages.add(delayedMessage);
      }
    }

    if (!delayedMessages.isEmpty()) {
      delayedMessageRegistry.put(endpointId, delayedMessages);
    }
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
