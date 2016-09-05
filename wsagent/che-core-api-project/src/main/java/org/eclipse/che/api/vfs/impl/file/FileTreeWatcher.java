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
package org.eclipse.che.api.vfs.impl.file;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;

@Singleton
public class FileTreeWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(FileTreeWatcher.class);

    private static final long EVENT_PROCESS_TIMEOUT_SEC = 2;

    private final File                           watchRoot;
    private final Path                           watchRootPath;
    private final Map<Path, WatchedDirectory>    watchedDirectories;
    private final List<PathMatcher>              excludePatterns;
    private final FileWatcherNotificationHandler fileWatcherNotificationHandler;
    private final ExecutorService                executor;
    private final AtomicBoolean                  running;
    private       WatchService                   watchService;
    private       WatchEvent.Modifier[]          watchEventModifiers;

    @Inject
    public FileTreeWatcher(@Named("che.user.workspaces.storage") File watchRoot,
                           @Named("vfs.index_filter_matcher") Set<PathMatcher> excludePatterns,
                           FileWatcherNotificationHandler fileWatcherNotificationHandler) {
        watchEventModifiers = new WatchEvent.Modifier[0];
        this.watchRoot = toCanonicalFile(watchRoot);
        this.watchRootPath = this.watchRoot.toPath();
        this.excludePatterns = newArrayList(excludePatterns);
        this.fileWatcherNotificationHandler = fileWatcherNotificationHandler;

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("FileTreeWatcher-%d").build();
        executor = Executors.newSingleThreadExecutor(threadFactory);
        running = new AtomicBoolean();
        watchedDirectories = newHashMap();
    }

    private static File toCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public void startup() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        if (isPollingWatchService(watchService)) {
            watchEventModifiers = new WatchEvent.Modifier[]{createSensitivityWatchEventModifier()};
        }
        running.set(true);
        walkTreeAndSetupWatches(watchRootPath);
        executor.execute(new WatchEventTask());
        fileWatcherNotificationHandler.started(watchRoot);
    }

    private boolean isPollingWatchService(WatchService watchService) {
        return "sun.nio.fs.PollingWatchService".equals(watchService.getClass().getName());
    }

    private WatchEvent.Modifier createSensitivityWatchEventModifier() {
        try {
            Class<?> aModifierEnum = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
            Object[] sensitivityEnumConstants = aModifierEnum.getEnumConstants();
            return (WatchEvent.Modifier)sensitivityEnumConstants[0];
        } catch (Exception e) {
            LOG.warn("Can't create 'com.sun.nio.file.SensitivityWatchEventModifier'", e);
        }
        return null;
    }

    public void shutdown() {
        boolean interrupted = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(3, SECONDS)) {
                    LOG.warn("Unable terminate Executor");
                }
            }
        } catch (InterruptedException e) {
            interrupted = true;
            executor.shutdownNow();
        }

        try {
            walkTreeAndRemoveWatches(watchRootPath);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }

        try {
            watchService.close();
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public void addExcludeMatcher(PathMatcher exclude) {
        this.excludePatterns.add(exclude);
    }

    public void removeExcludeMatcher(PathMatcher exclude) {
        this.excludePatterns.remove(exclude);
    }

    private void walkTreeAndSetupWatches(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = watchRootPath.relativize(dir);
                if (shouldNotify(relativePath)) {
                    setupDirectoryWatcher(dir);
                }
                return CONTINUE;
            }
        });
    }

    private boolean shouldNotify(Path subPath) {
        for (PathMatcher excludePattern : excludePatterns) {
            if (excludePattern.matches(subPath)) {
                return false;
            }
        }
        return true;
    }

    private void walkTreeAndRemoveWatches(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                cancelDirectoryWatcher(dir);
                return CONTINUE;
            }
        });
    }

    private void walkTreeAndFireCreatedEvents(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(root)) {
                    fireWatchEvent(CREATED, dir, true);
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fireWatchEvent(CREATED, file, false);
                return CONTINUE;
            }
        });
    }

    private void setupDirectoryWatcher(Path directory) throws IOException {
        if (watchedDirectories.get(directory) == null) {
            WatchKey watchKey = directory.register(watchService,
                                                   new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW},
                                                   watchEventModifiers);
            WatchedDirectory watchedDirectory = new WatchedDirectory(directory, watchKey);
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
                for (Path entry : entries) {
                    watchedDirectory
                            .addItem(new DirectoryItem(entry.getFileName(), Files.isDirectory(entry), getLastModifiedInMillis(entry)));

                    if (Files.isDirectory(entry)) {
                        setupDirectoryWatcher(entry);
                    }
                }
            }
            watchedDirectories.put(directory, watchedDirectory);
        }
    }

    private void cancelDirectoryWatcher(Path path) {
        WatchedDirectory watchedDirectory = watchedDirectories.remove(path);
        if (watchedDirectory != null) {
            watchedDirectory.getWatchKey().cancel();
        }
    }

    private class WatchEventTask implements Runnable {
        final Set<PendingEvent> pendingEvents = newLinkedHashSet();

        @Override
        public void run() {
            while (running.get()) {
                try {
                    WatchKey watchKey;
                    if (pendingEvents.isEmpty()) {
                        watchKey = watchService.take();
                    } else {
                        watchKey = watchService.poll(EVENT_PROCESS_TIMEOUT_SEC, SECONDS);
                        if (watchKey == null) {
                            processPendingEvents(pendingEvents);
                            pendingEvents.clear();
                        }
                    }
                    if (watchKey != null) {
                        pendingEvents.add(new PendingEvent((Path)watchKey.watchable()));
                        watchKey.pollEvents();
                        watchKey.reset();
                    }
                } catch (InterruptedException | ClosedWatchServiceException e) {
                    running.set(false);
                } catch (Throwable e) {
                    running.set(false);
                    fileWatcherNotificationHandler.errorOccurred(watchRoot, e);
                }
            }
        }
    }

    private void processPendingEvents(Collection<PendingEvent> pendingEvents) throws IOException {
        for (PendingEvent pendingEvent : pendingEvents) {
            Path eventDirectoryPath = pendingEvent.getPath();
            WatchedDirectory watchedDirectory = watchedDirectories.get(eventDirectoryPath);
            if (Files.exists(eventDirectoryPath)) {
                boolean isModifiedNotYetReported = true;

                final int hitCounter = watchedDirectory.incrementHitCounter();
                try (DirectoryStream<Path> entries = Files.newDirectoryStream(eventDirectoryPath)) {
                    for (Path fsItem : entries) {
                        DirectoryItem directoryItem = watchedDirectory.getItem(fsItem.getFileName());
                        if (directoryItem == null) {
                            try {
                                boolean directory = Files.isDirectory(fsItem);
                                directoryItem = new DirectoryItem(fsItem.getFileName(), directory, getLastModifiedInMillis(fsItem));
                                watchedDirectory.addItem(directoryItem);
                                if (isModifiedNotYetReported){
                                    isModifiedNotYetReported = false;
                                    fireWatchEvent(MODIFIED, eventDirectoryPath, true);
                                }
                                fireWatchEvent(CREATED, fsItem, directoryItem.isDirectory());
                                if (directory) {
                                    walkTreeAndFireCreatedEvents(fsItem);
                                    setupDirectoryWatcher(fsItem);
                                }
                            } catch (IOException ignored) {
                            }
                        } else {
                            long lastModified;
                            try {
                                lastModified = getLastModifiedInMillis(fsItem);
                            } catch (IOException ignored) {
                                continue;
                            }
                            if (lastModified != directoryItem.getLastModified() && Files.isRegularFile(fsItem)) {
                                fireWatchEvent(MODIFIED, fsItem, false);
                            }
                            directoryItem.touch(lastModified);
                            directoryItem.updateHitCounter(hitCounter);
                        }
                    }
                }

                for (Iterator<DirectoryItem> iterator = watchedDirectory.getItems().iterator(); iterator.hasNext(); ) {
                    DirectoryItem directoryItem = iterator.next();
                    if (hitCounter != directoryItem.getHitCount()) {
                        iterator.remove();
                        if (isModifiedNotYetReported){
                            isModifiedNotYetReported = false;
                            fireWatchEvent(MODIFIED, eventDirectoryPath, true);
                        }
                        fireWatchEvent(DELETED, eventDirectoryPath.resolve(directoryItem.getName()), directoryItem.isDirectory());
                    }
                }
            } else {
                for (DirectoryItem directoryItem : watchedDirectory.getItems()) {
                    fireWatchEvent(DELETED, eventDirectoryPath.resolve(directoryItem.getName()), directoryItem.isDirectory());
                }
                watchedDirectories.remove(eventDirectoryPath);
            }
        }
    }

    private void fireWatchEvent(FileWatcherEventType eventType, Path eventPath, boolean isDirectory) {
        Path relativePath = watchRootPath.relativize(eventPath);
        if (shouldNotify(relativePath)) {
            fileWatcherNotificationHandler.handleFileWatcherEvent(eventType, watchRoot, relativePath.toString(), isDirectory);
        }
    }

    private long getLastModifiedInMillis(Path path) throws IOException {
        return getLastModifiedTime(path, NOFOLLOW_LINKS).toMillis();
    }

    static class PendingEvent {
        final Path path;

        PendingEvent(Path path) {
            this.path = path;
        }

        Path getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof PendingEvent) {
                PendingEvent other = (PendingEvent)o;
                return Objects.equals(path, other.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(path);
        }
    }

    static class WatchedDirectory {
        final Path                path;
        final WatchKey            watchKey;
        final List<DirectoryItem> items;
        int hitCounter;

        WatchedDirectory(Path path, WatchKey watchKey) {
            this.path = path;
            this.watchKey = watchKey;
            items = newArrayList();
        }

        WatchKey getWatchKey() {
            return watchKey;
        }

        Path getPath() {
            return path;
        }

        DirectoryItem getItem(Path name) {
            for (DirectoryItem item : items) {
                if (item.getName().equals(name)) {
                    return item;
                }
            }
            return null;
        }

        void addItem(DirectoryItem item) {
            item.updateHitCounter(this.hitCounter);
            items.add(item);
        }

        List<DirectoryItem> getItems() {
            return items;
        }

        int incrementHitCounter() {
            return ++hitCounter;
        }
    }

    static class DirectoryItem {
        final Path    name;
        final boolean directory;
        long lastModified;
        int  hitCounter;

        DirectoryItem(Path name, boolean directory, long lastModified) {
            this.name = name;
            this.directory = directory;
            this.lastModified = lastModified;
        }

        long getLastModified() {
            return lastModified;
        }

        Path getName() {
            return name;
        }

        boolean isDirectory() {
            return directory;
        }

        void touch(long lastModified) {
            this.lastModified = lastModified;
        }

        int getHitCount() {
            return hitCounter;
        }

        void updateHitCounter(int hitCounter) {
            this.hitCounter = hitCounter;
        }
    }
}
