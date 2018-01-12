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
package org.eclipse.che.multiuser.organization.api.listener;

import static org.eclipse.che.multiuser.organization.shared.event.EventType.MEMBER_ADDED;
import static org.eclipse.che.multiuser.organization.shared.event.EventType.MEMBER_REMOVED;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.organization.api.DtoConverter;
import org.eclipse.che.multiuser.organization.shared.event.MemberEvent;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Broadcasts organization events through websocket connection.
 *
 * @author Anton Korneta
 */
@Singleton
public class OrganizationEventsWebsocketBroadcaster implements EventSubscriber<OrganizationEvent> {

  public static final String ORGANIZATION_CHANNEL_NAME = "organization:%s";
  public static final String ORGANIZATION_MEMBER_CHANNEL_NAME = "organization:member:%s";

  private static final Logger LOG =
      LoggerFactory.getLogger(OrganizationEventsWebsocketBroadcaster.class);

  @Inject
  private void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(OrganizationEvent event) {
    try {
      final ChannelBroadcastMessage msg = new ChannelBroadcastMessage();
      if (MEMBER_ADDED == event.getType() || MEMBER_REMOVED == event.getType()) {
        final String userId = ((MemberEvent) event).getMember().getId();
        msg.setChannel(String.format(ORGANIZATION_MEMBER_CHANNEL_NAME, userId));
      } else {
        msg.setChannel(String.format(ORGANIZATION_CHANNEL_NAME, event.getOrganization().getId()));
      }
      msg.setBody(DtoFactory.getInstance().toJson(DtoConverter.asDto(event)));
      WSConnectionContext.sendMessage(msg);
    } catch (Exception x) {
      LOG.error(x.getMessage(), x);
    }
  }
}
