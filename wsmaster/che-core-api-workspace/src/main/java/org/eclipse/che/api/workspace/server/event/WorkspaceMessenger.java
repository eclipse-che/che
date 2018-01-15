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
package org.eclipse.che.api.workspace.server.event;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send workspace events using websocket channel to the clients
 *
 * @author Alexander Garagatyi
 */
@Singleton // should be eager
public class WorkspaceMessenger implements EventSubscriber<WorkspaceStatusEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceMessenger.class);

  private final EventService eventService;

  @Inject
  public WorkspaceMessenger(EventService eventService) {
    this.eventService = eventService;
  }

  @Override
  public void onEvent(WorkspaceStatusEvent event) {
    try {
      final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
      bm.setChannel("workspace:" + event.getWorkspaceId());
      bm.setBody(DtoFactory.getInstance().toJson(event));
      WSConnectionContext.sendMessage(bm);
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(this);
  }
}
