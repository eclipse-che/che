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
package org.eclipse.che.api.vfs.ng;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.vfs.ng.FileWatcherUtils.isExcluded;

/**
 * Watches directories for interactions with their entries. Based on
 * {@link WatchService} that uses underlying filesystem implementations. Does
 * not perform any data modification (including filesystem items) except for
 * tracking and notification the upper layers. Service operates with ordinary
 * java file system paths in counter to che virtual file system which may have
 * custom root element and structure. Transforming one we of path representation
 * into another and backwards is the responsibility of upper services.
 */
@Singleton
public class FileWatcherService {
    private static final Logger LOG = LoggerFactory.getLogger(FileWatcherService.class);

    private final AtomicBoolean suspended = new AtomicBoolean(false);
    private final AtomicBoolean running   = new AtomicBoolean(true);

    private final Map<WatchKey, Path> keys          = new ConcurrentHashMap<>();
    private final Map<Path, Integer>  registrations = new ConcurrentHashMap<>();

    private final Set<PathMatcher>        excludes;
    private final FileWatcherEventHandler handler;
    private final WatchService            service;

    private ExecutorService executor;

    @Inject
    public FileWatcherService(@Named("che.user.workspaces.storage.excludes") Set<PathMatcher> excludes,
                              FileWatcherEventHandler handler, WatchService service) {
        this.excludes = excludes;
        this.handler = handler;
        this.service = service;
    }


    @SuppressWarnings("unchecked")
    static private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    @PostConstruct
    public void start() throws IOException {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        ThreadFactory factory = builder.setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                       .setNameFormat(FileWatcherService.class.getSimpleName())
                                       .setDaemon(true)
                                       .build();
        executor = newSingleThreadExecutor(factory);
        executor.execute(this::run);
    }

    @PreDestroy
    public void shutdown() {
        boolean interrupted = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3L, SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(3L, SECONDS)) {
                    LOG.error("Unable to terminate executor");
                }
            }
        } catch (InterruptedException e) {
            interrupted = true;
            executor.shutdownNow();
        }

        try {
            service.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Registers a directory for tracking of corresponding entry creation,
     * modification or deletion events. Each call of this method increase
     * by one registration counter that corresponds to each folder being
     * watched. Any event related to such directory entry is passed further to
     * the specific handler only if registration counter related to the
     * directory is above zero, otherwise registration watch key is canceled
     * and no further directory watching is being performed.
     *
     * @param dir
     *         directory
     */
    public void register(Path dir) {
        LOG.debug("Registering directory '{}'", dir);
        if (keys.values().contains(dir)) {
            int previous = registrations.get(dir);
            LOG.info("Directory is already being watched, increasing watch counter, previous value: {}", previous);
            registrations.put(dir, previous + 1);
        } else {
            try {
                LOG.debug("Starting watching directory '{}'", dir);
                WatchKey watchKey = dir.register(service, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE);
                keys.put(watchKey, dir);
                registrations.put(dir, 1);
            } catch (IOException e) {
                LOG.error("Can't register dir {} in file watch service", dir, e);
            }
        }
    }

    /**
     * Cancels registration of a directory for being watched. Each call of this
     * method decreases by one registration counter that corresponds to
     * directory specified by the argument. If registration counter comes to
     * zero directory watching is totally cancelled.
     *
     * @param dir
     *         directory
     */
    public void unRegister(Path dir) {
        LOG.debug("Canceling directory '{}' registration", dir);

        int previous = registrations.get(dir);
        if (previous == 1) {
            LOG.debug("Stopping watching directory '{}'", dir);
            registrations.remove(dir);

            Predicate<Entry<WatchKey, Path>> equalsDir = it -> it.getValue().equals(dir);

            keys.entrySet().stream().filter(equalsDir).map(Entry::getKey).forEach(WatchKey::cancel);
            keys.entrySet().removeIf(equalsDir);
        } else {
            LOG.debug("Directory is being watched by someone else, decreasing watch counter, previous value: {}", previous);
            registrations.put(dir, previous - 1);
        }
    }

    /**
     * Resumes service after it was in suspended state. If method is called
     * when the service is already not in a suspended state nothing happens.
     */
    public void resume() {
        if (suspended.compareAndSet(true, false)) {
            LOG.debug("Resuming service.");
        }
    }

    /**
     * Temporary suspends service of generating any events. Events received by
     * service in suspended state are totally skipped. If method is called when
     * the service is already in a suspended state nothing happens.
     */
    public void suspend() {
        if (suspended.compareAndSet(false, true)) {
            LOG.debug("Suspending service.");
        }
    }

    private void run() {
        while (running.get()) {
            try {
                WatchKey watchKey = service.take();
                Path dir = keys.get(watchKey);

                if (suspended.get()) {
                    resetAndRemove(watchKey, dir);

                    LOG.debug("File watchers are running in suspended mode - skipping.");
                    continue;
                }

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        LOG.warn("Detected file system events overflowing");
                        continue;
                    }

                    WatchEvent<Path> ev = cast(event);
                    Path item = ev.context();
                    Path path = dir.resolve(item).toAbsolutePath();

                    if (isExcluded(excludes, path)) {
                        LOG.debug("Path is within exclude list, skipping...");
                        continue;
                    }

                    handler.handle(path, kind);
                }

                resetAndRemove(watchKey, dir);
            } catch (InterruptedException e) {
                running.compareAndSet(true, false);
                LOG.error("Error during running file watcher or taking watch key instance", e);
            }
        }
    }

    private void resetAndRemove(WatchKey watchKey, Path dir) {
        if (!watchKey.reset()) {
            registrations.remove(dir);

            keys.remove(watchKey);
        }
    }
}
