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
package org.eclipse.che.api.workspace.server;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Provides a single non-daemon {@link ExecutorService} instance for workspace components.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceSharedPool {

    private final ExecutorService executor;

    @Inject
    public WorkspaceSharedPool(@Named("che.workspace.pool.type") String poolType,
                               @Named("che.workspace.pool.exact_size") @Nullable String exactSizeProp,
                               @Named("che.workspace.pool.cores_multiplier") @Nullable String coresMultiplierProp) {
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("WorkspaceSharedPool-%d")
                                                          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                                          .setDaemon(false)
                                                          .build();
        switch (poolType.toLowerCase()) {
            case "cached":
                executor = Executors.newCachedThreadPool(factory);
                break;
            case "fixed":
                Integer exactSize = exactSizeProp == null ? null : Ints.tryParse(exactSizeProp);
                int size;
                if (exactSize != null && exactSize > 0) {
                    size = exactSize;
                } else {
                    size = Runtime.getRuntime().availableProcessors();
                    Integer coresMultiplier = coresMultiplierProp == null ? null : Ints.tryParse(coresMultiplierProp);
                    if (coresMultiplier != null && coresMultiplier > 0) {
                        size *= coresMultiplier;
                    }
                }
                executor = Executors.newFixedThreadPool(size, factory);
                break;
            default:
                throw new IllegalArgumentException("The type of the pool '" + poolType + "' is not supported");
        }
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
     * waits until pool is terminated or timeout is reached.
     *
     * <p>Note that the method is not designed to be used from
     * different threads, but the other components may use it in their
     * post construct methods to ensure that all the tasks finished their execution.
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
