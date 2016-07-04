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
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
abstract class VfsEventService {
    private static final Logger LOG = getLogger(VfsEventService.class);

    private final ExecutorService executor;
    private final AtomicBoolean   running;

    VfsEventService() {
        final String threadName = getClass().getSimpleName().concat("Thread-%d");
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadName)
                                                                      .setDaemon(TRUE)
                                                                      .build();

        this.executor = newSingleThreadExecutor(threadFactory);
        this.running = new AtomicBoolean(FALSE);
    }

    @PostConstruct
    protected void postConstruct() {
        running.compareAndSet(FALSE, TRUE);
        executor.execute(this::runAsASingleThread);
        LOG.info("Starting virtual file system event service: {}", this.getClass().getSimpleName());
    }

    @PreDestroy
    protected void preDestroy() {
        running.compareAndSet(TRUE, FALSE);
        executor.shutdown();
        LOG.info("Stopping virtual file system event service: {}", this.getClass().getSimpleName());
    }

    private void runAsASingleThread() {
        while (running.get()) {
            run();
        }
    }

    protected abstract void run();
}
