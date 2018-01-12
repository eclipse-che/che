/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.watcher.server.impl;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.nio.file.Files.isDirectory;
import static org.eclipse.che.api.watcher.server.impl.FileWatcherUtils.toInternalPath;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /**
   * Registers create, modify and delete operations when item defined by path parameter is
   * correspondingly being created, modified or deleted. If path parameter denotes directory than
   * listed operations are considered to correspond any directory entry related event. Return value
   * corresponds to identifier that is used to distinguish different operation sets registered for
   * the same path and can be later used to unregister those operations.
   *
   * @param path path
   * @param create consumer for entry create event
   * @param modify consumer for entry modify event
   * @param delete consumer for entry delete event
   * @return number identifier of operations set
   */
  int register(
      Path path, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
    LOG.debug("Registering operations for path '{}'");
    int id = idCounter.incrementAndGet();
    FileWatcherOperation operation = new FileWatcherOperation(id, create, modify, delete);

    operations.putIfAbsent(path, newConcurrentHashSet());
    operations.get(path).add(operation);

    return id;
  }

  /**
   * Cancels registration of operations identified by parameter. Identifier is unique and generated
   * during registration phase. If there left no operation sets registered for a path it is also
   * removed.
   *
   * @param id identifier
   * @return path that corresponds to operations set identified by parameter
   */
  Path unRegister(int id) {
    Path dir = null;
    for (Entry<Path, Set<FileWatcherOperation>> entry : operations.entrySet()) {
      Path path = entry.getKey();
      Set<FileWatcherOperation> operationsSet = entry.getValue();
      Predicate<FileWatcherOperation> predicate = it -> Objects.equals(id, it.getId());

      if (operationsSet.removeIf(predicate)) {
        dir = isDirectory(path) ? path : path.getParent();
      }

      if (operationsSet.isEmpty()) {
        operations.remove(path);
      }
    }

    return dir;
  }

  /**
   * Handles event passed form file watcher system. Path parameter is expected to be passed in a
   * normal operation system file system form and is transformed into internal virtual file system
   * format before further processing.
   *
   * @param path path that the event is originated from
   * @param kind kind of event (e.g. created, modified, removed)
   */
  void handle(Path path, WatchEvent.Kind<?> kind) {
    Path dir = path.getParent();
    String internalPath = toInternalPath(root.toPath(), path);
    Set<FileWatcherOperation> dirOperations = operations.get(dir);
    Set<FileWatcherOperation> itemOperations = operations.get(path);

    if (dirOperations != null) {
      dirOperations
          .stream()
          .map(it -> it.get(kind))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(it -> it.accept(internalPath));
    }

    if (itemOperations != null) {
      itemOperations
          .stream()
          .map(it -> it.get(kind))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(it -> it.accept(internalPath));
    }
  }
}
