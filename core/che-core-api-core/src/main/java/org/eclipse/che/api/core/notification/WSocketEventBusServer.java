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
package org.eclipse.che.api.core.notification;

import org.everrest.websockets.WSConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.che.commons.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author andrew00x
 */
@Singleton
@Path("event-bus")
public final class WSocketEventBusServer {
    private static final Logger LOG = LoggerFactory.getLogger(WSocketEventBusServer.class);

    private final EventService                 eventService;
    private final ServerEventPropagationPolicy policy;
    private final AtomicBoolean                start;

    @Inject
    public WSocketEventBusServer(EventService eventService, @Nullable ServerEventPropagationPolicy policy) {
        this.eventService = eventService;
        this.policy = policy;

        start = new AtomicBoolean(false);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void event(String message) {
        try {
            final Object event = Messages.restoreEventFromClientMessage(message);
            if (event != null) {
                eventService.publish(event);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @PostConstruct
    void start() {
        if (start.compareAndSet(false, true)) {
            if (policy != null) {
                eventService.subscribe(new EventSubscriber<Object>() {
                    @Override
                    public void onEvent(Object event) {
                        propagate(event);
                    }
                });
            }
        }
    }

    protected void propagate(Object event) {
        if (policy.shouldPropagated(event)) {
            try {
                WSConnectionContext.sendMessage(Messages.broadcastMessage(resolveChannelName(event), event));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    protected String resolveChannelName(Object event) {
        final EventOrigin eventOrigin = event.getClass().getAnnotation(EventOrigin.class);
        if (eventOrigin == null) {
            throw new RuntimeException(String.format("Unable get channel name for %s", event));
        }
        return eventOrigin.value();
    }
}
