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
package org.eclipse.che.api.git;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.git.shared.event.GitCheckoutEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anton Korneta. */
@Singleton
public class GitWebSocketMessenger implements EventSubscriber<GitCheckoutEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(GitWebSocketMessenger.class);
  private static final String CHANNEL = "git:checkout:%s:%s";

  private final EventService eventService;

  @Inject
  public GitWebSocketMessenger(EventService eventService) {
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
  public void onEvent(GitCheckoutEvent event) {
    try {
      final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
      final String channel = String.format(CHANNEL, event.getWorkspaceId(), event.getProjectName());
      bm.setChannel(channel);
      bm.setBody(DtoFactory.getInstance().toJson(event));
      WSConnectionContext.sendMessage(bm);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
