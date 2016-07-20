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

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.eclipse.che.api.vfs.impl.file.event.EventTreeHelper.addEventAndCreatePrecedingNodes;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeNode.newRootInstance;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Main task of event low event service is to take low level events and process
 * them into event tree according to their locations in a file system. The event
 * tree is passed further to a event detectors and broadcasters managed by upper
 * {@link HiEventService}.
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class LoEventService extends VfsEventService {
    private static final Logger LOG = getLogger(LoEventService.class);

    /**
     * Maximal time interval between events that we consider them
     * to belong a common event segment.
     */
    static final         long   MAX_EVENT_INTERVAL_MILLIS    = 300L;
    /**
     * Maximal time interval that an event segment can last. This is used
     * to avoid long going events sequences. We are splitting them into several
     * lesser.
     */
    static final         long   MAX_TIME_SEGMENT_SIZE_MILLIS = 5 * MAX_EVENT_INTERVAL_MILLIS;

    /**
     * This constant is used to set undefined timestamp in case if a new event
     * segment is not started but the event interval is big enough to finish
     * previous event segment and to enqueue an old tree.
     */
    private static final long   UNDEFINED                    = -1L;

    private final LoEventQueueHolder   loEventQueueHolder;
    private final EventTreeQueueHolder eventTreeQueueHolder;

    private EventTreeNode vfsEventTreeRoot;
    private long          eventSegmentStartTime;

    @Inject
    public LoEventService(LoEventQueueHolder loEventQueueHolder,
                          EventTreeQueueHolder eventTreeQueueHolder) {
        this.loEventQueueHolder = loEventQueueHolder;
        this.eventTreeQueueHolder = eventTreeQueueHolder;

        this.vfsEventTreeRoot = newRootInstance();
        this.eventSegmentStartTime = UNDEFINED;

    }

    @Override
    protected void run() {
        Optional<LoEvent> optional = loEventQueueHolder.poll(MAX_EVENT_INTERVAL_MILLIS);

        if (optional.isPresent()) {
            final LoEvent loEvent = optional.get();
            final long eventTime = loEvent.getTime();

            if (eventSegmentStartTime == UNDEFINED || eventTime - eventSegmentStartTime >= MAX_TIME_SEGMENT_SIZE_MILLIS) {
                LOG.trace("Starting new event segment.");
                LOG.trace("Old event segment start time: {} ", eventSegmentStartTime);
                LOG.trace("New event segment start time: {} ", eventTime);

                flushOldTreeAndStartNew();
                eventSegmentStartTime = eventTime;
            }

            addEventAndCreatePrecedingNodes(vfsEventTreeRoot, loEvent);
        } else {
            flushOldTreeAndStartNew();
            eventSegmentStartTime = UNDEFINED;
        }
    }

    private void flushOldTreeAndStartNew() {
        if (vfsEventTreeRoot.getChildren().isEmpty()) {
            return;
        }

        eventTreeQueueHolder.put(vfsEventTreeRoot);
        LOG.trace("Flushing old event tree {}.", vfsEventTreeRoot);

        vfsEventTreeRoot = newRootInstance();
        LOG.trace("Starting new event tree {}.", vfsEventTreeRoot);
    }
}
