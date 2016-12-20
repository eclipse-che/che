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
import java.nio.file.FileSystems;
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
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.vfs.ng.FileWatcherUtils.isExcluded;

@Singleton
public class FileWatcherService implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FileWatcherService.class);

    private final AtomicBoolean suspended = new AtomicBoolean(false);
    private final AtomicBoolean running   = new AtomicBoolean(true);

    private final Map<WatchKey, Path> keys          = new ConcurrentHashMap<>();
    private final Map<Path, Integer>  registrations = new ConcurrentHashMap<>();

    private final Set<PathMatcher>        excludes;
    private final FileWatcherEventHandler handler;

    private ExecutorService executor;
    private WatchService    service;

    @Inject
    public FileWatcherService(@Named("che.user.workspaces.storage.excludes") Set<PathMatcher> excludes,
                              FileWatcherEventHandler handler) {
        this.excludes = excludes;
        this.handler = handler;
    }


    @SuppressWarnings("unchecked")
    static private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    @PostConstruct
    public void start() throws IOException {
        service = FileSystems.getDefault().newWatchService();

        executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                                                             .setDaemon(true)
                                                             .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                                             .setNameFormat("DynamicFileWatcherService-%d")
                                                             .build());
        executor.execute(this);
    }

    @PreDestroy
    public void shutdown() {
        boolean interrupted = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(3, SECONDS)) {
                    LOG.error("Unable terminate Executor");
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


    public void register(Path dir) {
        if (keys.values().contains(dir)) {
            int previous = registrations.get(dir);
            registrations.put(dir, previous + 1);
        } else {
            try {
                WatchKey watchKey = dir.register(service, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE);
                keys.put(watchKey, dir);
                registrations.put(dir, 1);
            } catch (IOException e) {
                LOG.error("Can't register dir {} in file watch service", dir, e);
            }
        }
    }

    public void unRegister(Path dir) {
        int previous = registrations.get(dir);
        if (previous == 1) {
            registrations.remove(dir);
            for (Entry<WatchKey, Path> entry : keys.entrySet()) {
                WatchKey watchKey = entry.getKey();
                Path value = entry.getValue();

                if (value.equals(dir)) {
                    watchKey.cancel();
                    keys.remove(watchKey);
                    break;
                }
            }
        } else {
            registrations.put(dir, previous - 1);
        }
    }

    public void resume() {
        suspended.set(false);
    }

    public void suspend() {
        suspended.set(true);
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                WatchKey watchKey = service.take();
                Path dir = keys.get(watchKey);

                if (suspended.get()) {
                    reset(watchKey, dir);

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

                    if (isExcluded(excludes, path)){
                        LOG.debug("Path is within exclude list, skipping...");
                        continue;
                    }

                    handler.handle(path, kind);
                }

                reset(watchKey, dir);
            } catch (InterruptedException e) {
                running.compareAndSet(true, false);
                LOG.error("Error during running file watcher or taking watch key instance", e);
            }
        }
    }

    private void reset(WatchKey watchKey, Path dir) {
        if (!watchKey.reset()) {
            registrations.remove(dir);
            keys.remove(watchKey);
        }
    }
}
