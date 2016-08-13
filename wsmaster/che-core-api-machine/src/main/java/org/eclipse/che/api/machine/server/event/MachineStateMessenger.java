/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.event;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static java.lang.String.format;
import static org.eclipse.che.api.machine.shared.Constants.ENVIRONMENT_STATUS_CHANNEL_TEMPLATE;

/**
 * Send machine state events using websocket channel to the clients
 *
 * @author Alexander Garagatyi
 */
@Singleton // should be eager
public class MachineStateMessenger implements EventSubscriber<MachineStatusEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(MachineStateMessenger.class);

    private final EventService eventService;

    @Inject
    public MachineStateMessenger(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onEvent(MachineStatusEvent event) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel(format(ENVIRONMENT_STATUS_CHANNEL_TEMPLATE, event.getWorkspaceId()));
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
