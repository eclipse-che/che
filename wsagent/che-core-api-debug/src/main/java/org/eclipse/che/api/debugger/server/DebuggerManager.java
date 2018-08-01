/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debugger.server;

import static java.lang.Long.parseLong;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerNotFoundException;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatoliy Bazko */
@Singleton
public class DebuggerManager {

  private static final Logger LOG = LoggerFactory.getLogger(DebuggerManager.class);

  private final EventService eventService;

  /** Registered factories by debug type. */
  private final Map<String, DebuggerFactory> factories;

  /** Active debug sessions. */
  private final Map<Long, Pair<String, Debugger>> debuggers;

  private final AtomicLong sessionId;

  @Inject
  public DebuggerManager(Set<DebuggerFactory> factories, EventService eventService) {
    this.eventService = eventService;
    this.factories = new ConcurrentHashMap<>();
    this.debuggers = new ConcurrentHashMap<>();
    this.sessionId = new AtomicLong();

    factories.stream().forEach(factory -> this.factories.put(factory.getType(), factory));
  }

  /**
   * Instantiates a new debugger of given type.
   *
   * @see DebuggerFactory#create(Map, Debugger.DebuggerCallback)
   * @return session identifier
   */
  public String create(String debuggerType, Map<String, String> properties)
      throws DebuggerException {
    DebuggerFactory factory = factories.get(debuggerType);
    if (factory == null) {
      throw new DebuggerNotFoundException(
          "Debugger factory type '" + debuggerType + "' is not registered");
    }

    final long id = sessionId.incrementAndGet();
    Debugger debugger =
        factory.create(
            properties,
            event -> {
              if (DebuggerEvent.TYPE.DISCONNECT == event.getType()) {
                debuggers.remove(id);
              }

              eventService.publish(new DebuggerMessage(event, debuggerType));
            });
    debuggers.put(id, Pair.of(debuggerType, debugger));

    return String.valueOf(id);
  }

  /**
   * @return active debugger by session id
   * @throws DebuggerNotFoundException if no debugger found
   */
  public Debugger getDebugger(String sessionId) throws DebuggerNotFoundException {
    try {
      Pair<String, Debugger> debugger = debuggers.get(parseLong(sessionId));
      if (debugger == null) {
        throw new DebuggerNotFoundException("Debug session " + sessionId + " not found");
      }
      return debugger.second;
    } catch (NumberFormatException e) {
      throw new DebuggerNotFoundException("Illegal session id " + sessionId);
    }
  }

  /**
   * @return debugger type by session id
   * @throws DebuggerNotFoundException if no debugger found
   */
  public String getDebuggerType(String sessionId) throws DebuggerNotFoundException {
    try {
      Pair<String, Debugger> debugger = debuggers.get(parseLong(sessionId));
      if (debugger == null) {
        throw new DebuggerNotFoundException("Debug session " + sessionId + " not found");
      }
      return debugger.first;
    } catch (NumberFormatException e) {
      throw new DebuggerNotFoundException("Illegal session id " + sessionId);
    }
  }
}
