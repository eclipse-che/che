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
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Event server-side publisher with a very basic set of functionality.
 * It simply publishes the events DTO to an instance of {@link EventService}
 * <p>
 *     Note: DTO object is assumed to be stored in {@link HiEvent} instance
 *     passed to this broadcaster.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class HiEventServerPublisher implements HiEventBroadcaster {
    private static final Logger LOG = getLogger(HiEventClientBroadcaster.class);

    private final EventService eventService;

    @Inject
    public HiEventServerPublisher(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void broadcast(HiEvent event) {
        eventService.publish(event.getDto());
        LOG.debug("Publishing event to event service: {}.", event.getDto());
    }
}
