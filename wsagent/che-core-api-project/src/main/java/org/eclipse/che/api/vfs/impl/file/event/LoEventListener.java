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
package org.eclipse.che.api.vfs.impl.file.event;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class LoEventListener implements EventSubscriber<LoEvent> {
    private static final Logger LOG = getLogger(LoEventListener.class);

    private final EventService       eventService;
    private final LoEventQueueHolder loEventQueueHolder;

    @Inject
    public LoEventListener(EventService eventService, LoEventQueueHolder loEventQueueHolder) {
        this.eventService = eventService;
        this.loEventQueueHolder = loEventQueueHolder;
    }

    @PostConstruct
    void postConstruct() {
        eventService.subscribe(this);
        LOG.info("Subscribing to event service: {}", this.getClass());
    }

    @PreDestroy
    void preDestroy() {
        eventService.unsubscribe(this);
        LOG.info("Unsubscribing to event service: {}", this.getClass());
    }

    @Override
    public void onEvent(LoEvent event) {
        loEventQueueHolder.put(event);
        LOG.trace("Putting event {} to {}", event, LoEventQueueHolder.class);
    }
}
