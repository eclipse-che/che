/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.system.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.shared.dto.SystemEventDto;
import org.eclipse.che.api.system.shared.dto.SystemStatusChangedEventDto;
import org.eclipse.che.api.system.shared.event.SystemEvent;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Broadcasts system status events to the websocket channel.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class SystemEventsWebsocketBroadcaster implements EventSubscriber<SystemEvent> {

    public static final String SYSTEM_STATE_CHANNEL_NAME = "system:state";

    @Inject
    public void subscribe(EventService eventService) {
        eventService.subscribe(this);
    }

    @Override
    public void onEvent(SystemEvent event) {
        ChannelBroadcastMessage message = new ChannelBroadcastMessage();
        message.setBody(DtoFactory.getInstance().toJson(asDto(event)));
        message.setChannel(SYSTEM_STATE_CHANNEL_NAME);
        try {
            WSConnectionContext.sendMessage(message);
        } catch (Exception x) {
            LoggerFactory.getLogger(getClass()).error(x.getMessage(), x);
        }
    }

    private static SystemEventDto asDto(SystemEvent event) {
        switch (event.getType()) {
            case STATUS_CHANGED:
                SystemStatusChangedEvent statusChanged = (SystemStatusChangedEvent)event;
                return DtoFactory.newDto(SystemStatusChangedEventDto.class)
                                 .withStatus(statusChanged.getStatus())
                                 .withPrevStatus(statusChanged.getPrevStatus())
                                 .withType(statusChanged.getType());
            default:
                throw new IllegalArgumentException("Can't convert event of type '" + event.getType() + "' to DTO");
        }
    }
}
