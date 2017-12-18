/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import static java.nio.file.Files.exists;

import com.google.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileWatcherByPathMatcher implements Consumer<Path> {

  private static final Logger LOG = LoggerFactory.getLogger(FileWatcherByPathMatcher.class);

  private final AtomicInteger operationIdCounter = new AtomicInteger();

  private final FileWatcherByPathValue watcher;

  /** Operation ID -> Operation (create, modify, delete) */
  private final Map<Integer, Operation> operations = new ConcurrentHashMap<>();
  /** Operation ID -> Registered paths */
  private final Map<Integer, Set<Path>> paths = new ConcurrentHashMap<>();
  /** Matcher -> Operation IDs */
  private final Map<PathMatcher, Set<Integer>> matchers = new ConcurrentHashMap<>();
  /** Registered path -> Path watch operation IDs */
  private final Map<Path, Set<Integer>> pathWatchRegistrations = new ConcurrentHashMap<>();

  private final File root;

  private PathTransformer pathTransformer;

  @Inject
  public FileWatcherByPathMatcher(
      @Named("che.user.workspaces.storage") File root,
      FileWatcherByPathValue watcher,
      PathTransformer pathTransformer) {
    this.root = root;
    this.watcher = watcher;
    this.pathTransformer = pathTransformer;
  }

  @Override
  public void accept(Path path) {
    if (!exists(path)) {
      if (pathWatchRegistrations.containsKey(path)) {
        pathWatchRegistrations.remove(path).forEach(watcher::unwatch);
      }
      paths.values().forEach(it -> it.remove(path));
      paths.entrySet().removeIf(it -> it.getValue().isEmpty());
      return;
    }

    for (PathMatcher matcher : matchers.keySet()) {
      if (matcher.matches(path)) {
        for (int operationId : matchers.get(matcher)) {
          paths.putIfAbsent(operationId, newConcurrentHashSet());
          if (paths.get(operationId).contains(path)) {
            return;
          }

          paths.get(operationId).add(path);

          Operation operation = operations.get(operationId);
          int pathWatcherOperationId =
              watcher.watch(path, operation.create, operation.modify, operation.delete);
          pathWatchRegistrations.putIfAbsent(path, newConcurrentHashSet());
          pathWatchRegistrations.get(path).add(pathWatcherOperationId);
          operation.create.accept(pathTransformer.transform(path));
        }
      }
    }
  }

  int watch(
      PathMatcher matcher,
      Consumer<String> create,
      Consumer<String> modify,
      Consumer<String> delete) {
    LOG.debug("Watching matcher '{}'", matcher);
    int operationId = operationIdCounter.getAndIncrement();

    matchers.putIfAbsent(matcher, newConcurrentHashSet());
    matchers.get(matcher).add(operationId);

    operations.put(operationId, new Operation(create, modify, delete));

    LOG.debug("Registered matcher operation set with id '{}'", operationId);
    return operationId;
  }

  void unwatch(int operationId) {
    LOG.debug("Unwatching matcher operation set with id '{}'", operationId);
    for (Entry<PathMatcher, Set<Integer>> entry : matchers.entrySet()) {
      PathMatcher matcher = entry.getKey();
      Set<Integer> operationsIdList = entry.getValue();
      Iterator<Integer> iterator = operationsIdList.iterator();
      while (iterator.hasNext()) {
        if (iterator.next() == operationId) {
          pathWatchRegistrations
              .keySet()
              .stream()
              .filter(matcher::matches)
              .flatMap(it -> pathWatchRegistrations.remove(it).stream())
              .forEach(watcher::unwatch);

          paths.values().forEach(it -> it.removeIf(matcher::matches));
          paths.entrySet().removeIf(it -> it.getValue().isEmpty());
          iterator.remove();
          operations.remove(operationId);

          break;
        }
      }

      if (matchers.get(matcher) == null || matchers.get(matcher).isEmpty()) {
        matchers.remove(matcher);
      }
    }
  }

  private static class Operation {

    final Consumer<String> create;
    final Consumer<String> modify;
    final Consumer<String> delete;

    private Operation(Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
      this.create = create;
      this.modify = modify;
      this.delete = delete;
    }
  }
}
