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
package org.eclipse.che.api.workspace.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Provides a single non-daemon {@link ExecutorService} instance for workspace components.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceSharedPool {

    private final ExecutorService executor;

    public WorkspaceSharedPool() {
        executor = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors(),
                                                new ThreadFactoryBuilder().setNameFormat("WorkspaceSharedPool-%d")
                                                                          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                                                          .setDaemon(false)
                                                                          .build());
    }

    /** Returns an {@link ExecutorService} managed by this pool instance. */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Delegates call to {@link ExecutorService#execute(Runnable)}
     * and propagates thread locals to it like defined by {@link ThreadLocalPropagateContext}.
     */
    public void execute(Runnable runnable) {
        executor.execute(ThreadLocalPropagateContext.wrap(runnable));
    }

    /**
     * Delegates call to {@link ExecutorService#submit(Callable)}
     * and propagates thread locals to it like defined by {@link ThreadLocalPropagateContext}.
     */
    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(ThreadLocalPropagateContext.wrap(callable));
    }

    /**
     * Terminates this pool, may be called multiple times,
     * waits until pool is terminated or timeout reached.
     *
     * @return true if executor successfully terminated and false if not
     * terminated(either await termination timeout is reached or thread was interrupted)
     */
    @PostConstruct
    public boolean terminateAndWait() {
        if (executor.isShutdown()) {
            return true;
        }
        Logger logger = LoggerFactory.getLogger(getClass());
        executor.shutdown();
        try {
            logger.info("Shutdown workspace threads pool, wait 30s to stop normally");
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                logger.info("Interrupt workspace threads pool, wait 60s to stop");
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("Couldn't terminate workspace threads pool");
                    return false;
                }
            }
        } catch (InterruptedException x) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}
