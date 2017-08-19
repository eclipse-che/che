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
package org.eclipse.che.api.debugger.server;

import static org.eclipse.che.api.debugger.server.DtoConverter.asDto;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @deprecated As of release 5.8.0, replaced by {@link DebuggerJsonRpcMessenger} */
@Singleton
@Deprecated
public class DebuggerWebSocketMessenger implements EventSubscriber<DebuggerMessage> {
  private static final Logger LOG = LoggerFactory.getLogger(DebuggerWebSocketMessenger.class);
  private static final String CHANNEL = "%s:events:";

  private final EventService eventService;

  @Inject
  public DebuggerWebSocketMessenger(EventService eventService) {
    this.eventService = eventService;
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(this);
  }

  @Override
  public void onEvent(DebuggerMessage msg) {
    try {
      final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
      final String channel = String.format(CHANNEL, msg.getDebuggerType());
      bm.setChannel(channel);
      bm.setBody(DtoFactory.getInstance().toJson(asDto(msg.getDebuggerEvent())));
      WSConnectionContext.sendMessage(bm);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
