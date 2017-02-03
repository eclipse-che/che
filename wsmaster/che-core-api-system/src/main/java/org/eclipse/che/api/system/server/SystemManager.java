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
package org.eclipse.che.api.system.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.eclipse.che.api.system.shared.SystemStatus.PREPARING_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.READY_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.RUNNING;

/**
 * Facade for system operations.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class SystemManager {

    private static final Logger LOG = LoggerFactory.getLogger(SystemManager.class);

    private final AtomicReference<SystemStatus> statusRef;
    private final EventService                  eventService;
    private final ServiceTerminator             terminator;

    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    @Inject
    public SystemManager(ServiceTerminator terminator, EventService eventService) {
        this.terminator = terminator;
        this.eventService = eventService;
        this.statusRef = new AtomicReference<>(RUNNING);
    }

    /**
     * Stops some of the system services preparing system to lighter shutdown.
     * System status is changed from {@link SystemStatus#RUNNING} to
     * {@link SystemStatus#PREPARING_TO_SHUTDOWN}.
     *
     * @throws ConflictException
     *         when system status is different from running
     */
    public void stopServices() throws ConflictException {
        if (!statusRef.compareAndSet(RUNNING, PREPARING_TO_SHUTDOWN)) {
            throw new ConflictException("System shutdown has been already called, system status: " + statusRef.get());
        }
        ExecutorService exec = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setDaemon(false)
                                          .setNameFormat("ShutdownSystemServicesPool")
                                          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                          .build());
        exec.execute(ThreadLocalPropagateContext.wrap(this::doStopServices));
        exec.shutdown();
    }

    /**
     * Gets current system status.
     *
     * @see SystemStatus
     */
    public SystemStatus getSystemStatus() {
        return statusRef.get();
    }

    /** Synchronously stops corresponding services. */
    private void doStopServices() {
        LOG.info("Preparing system to shutdown");
        eventService.publish(new SystemStatusChangedEvent(RUNNING, PREPARING_TO_SHUTDOWN));
        try {
            terminator.terminateAll();
            statusRef.set(READY_TO_SHUTDOWN);
            eventService.publish(new SystemStatusChangedEvent(PREPARING_TO_SHUTDOWN, READY_TO_SHUTDOWN));
            LOG.info("System is ready to shutdown");
        } catch (InterruptedException x) {
            LOG.error("Interrupted while waiting for system service to shutdown components");
            Thread.currentThread().interrupt();
        } finally {
            shutdownLatch.countDown();
        }
    }

    @PreDestroy
    @VisibleForTesting
    void shutdown() throws InterruptedException {
        if (!statusRef.compareAndSet(RUNNING, PREPARING_TO_SHUTDOWN)) {
            shutdownLatch.await();
        } else {
            doStopServices();
        }
    }
}
