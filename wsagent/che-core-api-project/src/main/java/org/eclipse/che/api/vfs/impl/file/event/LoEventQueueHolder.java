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

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
class LoEventQueueHolder {
    private static final Logger LOG = getLogger(LoEventQueueHolder.class);

    private final BlockingQueue<LoEvent> loEventQueue;

    public LoEventQueueHolder() {
        this.loEventQueue = new LinkedBlockingQueue<>();
    }

    void put(LoEvent loEvent) {
        try {
            loEventQueue.put(loEvent);
        } catch (InterruptedException e) {
            LOG.error("Error trying to put an event to an event queue: {}", loEvent, e);
        }
    }

    Optional<LoEvent> poll(long timeout) {
        try {
            return Optional.ofNullable(loEventQueue.poll(timeout, MILLISECONDS));
        } catch (InterruptedException e) {
            LOG.error("Error trying to poll an event out of an event queue", e);
        }
        return empty();
    }
}
