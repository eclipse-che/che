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
package org.eclipse.che.api.project.server.notification;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.shared.dto.event.VfsWatchEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 *
 * Subscribes on VFS Watcher events and broadcasts them with websockets
 * @author gazarenkov
 */
public class VfsWatchBroadcaster implements EventSubscriber<VfsWatchEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(VfsWatchBroadcaster.class);

    private final EventService eventService;

    @Inject
    public VfsWatchBroadcaster(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void onEvent(VfsWatchEvent event) {

        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel(VfsWatchEvent.VFS_CHANNEL);
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
