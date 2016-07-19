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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The main mission of this service is to manage event trees produced by {@link LoEventService}.
 * It is done with the help of {@link HiEventDetectorManager} and {@link HiEventBroadcasterManager}.
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class HiEventService extends VfsEventService {
    private final HiEventDetectorManager    hiEventDetectorManager;
    private final HiEventBroadcasterManager hiEventBroadcasterManager;
    private final EventTreeQueueHolder      eventTreeQueueHolder;

    @Inject
    public HiEventService(HiEventDetectorManager hiEventDetectorManager,
                          EventTreeQueueHolder eventTreeQueueHolder,
                          HiEventBroadcasterManager hiEventBroadcasterManager) {
        this.hiEventDetectorManager = hiEventDetectorManager;
        this.eventTreeQueueHolder = eventTreeQueueHolder;
        this.hiEventBroadcasterManager = hiEventBroadcasterManager;
    }

    protected void run() {
        final Optional<EventTreeNode> optional = eventTreeQueueHolder.take();
        if (optional.isPresent()) {
            final EventTreeNode treeRoot = optional.get();
            final List<HiEvent> events = hiEventDetectorManager.getDetectedEvents(treeRoot);
            hiEventBroadcasterManager.manageEvents(events);
        }
    }
}
