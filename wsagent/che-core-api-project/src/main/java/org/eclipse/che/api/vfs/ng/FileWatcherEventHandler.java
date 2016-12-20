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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.google.common.collect.Sets.newHashSet;
import static java.nio.file.Files.isDirectory;
import static org.eclipse.che.api.vfs.ng.FileWatcherUtils.toInternalPath;

@Singleton
public class FileWatcherEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FileWatcherManager.class);

    private final AtomicInteger idCounter = new AtomicInteger();

    private final Map<Path, Set<FileWatcherOperation>> operations = new ConcurrentHashMap<>();

    private final File root;

    @Inject
    public FileWatcherEventHandler(@Named("che.user.workspaces.storage") File root) {
        this.root = root;
    }

    public int register(Path path, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
        int id = idCounter.incrementAndGet();
        FileWatcherOperation operation = new FileWatcherOperation(id, create, modify, delete);

        operations.putIfAbsent(path, newHashSet());
        operations.get(path).add(operation);

        return id;
    }

    public Path unRegister(int id) {
        Path dir = null;
        for (Entry<Path, Set<FileWatcherOperation>> entry : operations.entrySet()) {
            Path path = entry.getKey();
            Set<FileWatcherOperation> fileWatcherOperations = entry.getValue();

            Iterator<FileWatcherOperation> iterator = fileWatcherOperations.iterator();
            while (iterator.hasNext()) {
                int candidateId = iterator.next().getId();
                if (Objects.equals(id, candidateId)) {
                    dir = isDirectory(path) ? path : path.getParent();
                    iterator.remove();
                    break;
                }
            }

            if (fileWatcherOperations.isEmpty()) {
                operations.remove(path);
            }
        }

        return dir;
    }

    public void handle(Path path, WatchEvent.Kind<?> kind) {
        Path dir = path.getParent();
        String internalPath = toInternalPath(root.toPath(), path);
        Set<FileWatcherOperation> dirOperations = operations.get(dir);
        Set<FileWatcherOperation> itemOperations = operations.get(path);

        if (dirOperations != null) {
            dirOperations.stream()
                         .map(it -> it.get(kind))
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .forEach(it -> it.accept(internalPath));
        }

        if (itemOperations != null) {
            itemOperations.stream()
                          .map(it -> it.get(kind))
                          .filter(Optional::isPresent)
                          .map(Optional::get)
                          .forEach(it -> it.accept(internalPath));
        }
    }
}
