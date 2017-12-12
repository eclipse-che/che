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

package org.eclipse.che.ide.eventbus;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.Disposable;
import org.eclipse.che.ide.api.eventbus.EventBus;
import org.eclipse.che.ide.api.eventbus.EventType;
import org.eclipse.che.ide.api.eventbus.Handler;
import org.eclipse.che.ide.util.ListenerManager;
import org.eclipse.che.ide.util.ListenerRegistrar.Remover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Yevhen Vydolob */
public class EventBusImpl implements EventBus {

  private static final Logger LOG = LoggerFactory.getLogger(EventBusImpl.class);

  private Map<String, ListenerManager<Handler>> handlers = new HashMap<>();

  @Override
  @SuppressWarnings("unchecked")
  public <E> EventBus fire(EventType<E> type, E message) {
    String eventType = type.type();
    if (handlers.containsKey(eventType)) {
      ListenerManager<Handler> listenerManager = handlers.get(eventType);
      try {
        listenerManager.dispatch(listener -> listener.handle(message));
      } catch (Throwable t) {
        LOG.error("Error when handle : " + eventType, t);
      }
    }
    return this;
  }


  private <E> Disposable registerHandler(String address, Handler<E> handler) {
    handlers.computeIfAbsent(address, (s) -> ListenerManager.create());
    ListenerManager<Handler> listenerManager = handlers.get(address);
    Remover remover = listenerManager.add(handler);
    return remover::remove;
  }

  @Override
  public <E> Disposable addHandler(EventType<E> eventType, Handler<E> handler) {
    return registerHandler(eventType.type(), handler);
  }
}
