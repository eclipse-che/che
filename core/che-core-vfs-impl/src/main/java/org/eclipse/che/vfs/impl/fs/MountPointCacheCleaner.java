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
package org.eclipse.che.vfs.impl.fs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Helps to reset data cached in {@code MountPoint}.
 *
 * @author andrew00x
 */
public class MountPointCacheCleaner {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointCacheCleaner.class);

    private static final String CACHE_RESET_PATH =
            FSMountPoint.SERVICE_DIR + java.io.File.separatorChar + "cache" + java.io.File.separatorChar + "reset";

    private static ScheduledExecutorService exec = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("MountPointCacheCleaner-%d").setDaemon(true).build());

    private static Map<java.io.File, Entry> watched = new ConcurrentHashMap<>();

    static {
        exec.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        for (Entry entry : watched.values()) {
                            if (Files.exists(entry.resetFilePath)) {
                                entry.mountPoint.reset();
                                LOG.info("Reset cache for VFS mounted at {}", entry.mountPoint.getRoot().getIoFile());
                                try {
                                    Files.delete(entry.resetFilePath);
                                } catch (IOException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                },
                10,
                10,
                TimeUnit.SECONDS);
    }

    static void add(FSMountPoint mountPoint) {
        final java.io.File ioPath = mountPoint.getRoot().getIoFile();
        final java.nio.file.Path resetFilePath = new java.io.File(ioPath, CACHE_RESET_PATH).toPath();
        watched.put(ioPath, new Entry(mountPoint, resetFilePath));
    }

    static void remove(FSMountPoint mountPoint) {
        watched.remove(mountPoint.getRoot().getIoFile());
    }

    public static class Finalizer {
        @PreDestroy
        void stop() {
            exec.shutdownNow();
            watched.clear();
            LOG.info("VFS cache cleaner stopped.");
        }
    }

    private static class Entry {
        final FSMountPoint       mountPoint;
        final java.nio.file.Path resetFilePath;

        Entry(FSMountPoint mountPoint, java.nio.file.Path resetFilePath) {
            this.mountPoint = mountPoint;
            this.resetFilePath = resetFilePath;
        }
    }
}
