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
package org.eclipse.che.api.machine.server.event;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Send machine process events using websocket channel to the clients
 *
 * @author Alexander Garagatyi
 */
@Singleton // should be eager
public class MachineProcessMessenger implements EventSubscriber<MachineProcessEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(MachineProcessMessenger.class);

    private final EventService eventService;

    @Inject
    public MachineProcessMessenger(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onEvent(MachineProcessEvent event) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel("machine:process:" + event.getMachineId());
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
