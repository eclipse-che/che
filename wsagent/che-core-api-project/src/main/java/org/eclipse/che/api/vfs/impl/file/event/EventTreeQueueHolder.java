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

import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Simple holder to benefit from Guice DI routines.
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
class EventTreeQueueHolder {
    private static final Logger LOG = getLogger(EventTreeQueueHolder.class);

    private final BlockingQueue<EventTreeNode> loVfsEventQueue;

    public EventTreeQueueHolder() {
        this.loVfsEventQueue = new LinkedBlockingQueue<>();
    }

    public void put(EventTreeNode loVfsEventTreeRoot) {
        try {
            loVfsEventQueue.put(loVfsEventTreeRoot);
        } catch (InterruptedException e) {
            LOG.error("Error trying to put an event tree to an event tree queue: {}", loVfsEventTreeRoot, e);
        }
    }

    public Optional<EventTreeNode> take() {
        try {
            return Optional.ofNullable(loVfsEventQueue.take());
        } catch (InterruptedException e) {
            LOG.error("Error trying to take an event tree out of an event tree queue", e);
        }
        return empty();
    }
}
