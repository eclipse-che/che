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
package org.eclipse.che.api.system.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.WebsocketLineConsumer;
import org.eclipse.che.api.system.shared.event.SystemEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.LoggerFactory;

/**
 * Broadcasts system status events to the websocket channel.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class SystemEventsWebsocketBroadcaster implements EventSubscriber<SystemEvent> {

  public static final String SYSTEM_STATE_CHANNEL_NAME = "system:state";

  private final LineConsumer messageConsumer;

  public SystemEventsWebsocketBroadcaster() {
    this(new WebsocketLineConsumer(SYSTEM_STATE_CHANNEL_NAME));
  }

  public SystemEventsWebsocketBroadcaster(WebsocketLineConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(SystemEvent event) {
    try {
      messageConsumer.writeLine(DtoFactory.getInstance().toJson(DtoConverter.asDto(event)));
    } catch (Exception x) {
      LoggerFactory.getLogger(getClass()).error(x.getMessage(), x);
    }
  }
}
