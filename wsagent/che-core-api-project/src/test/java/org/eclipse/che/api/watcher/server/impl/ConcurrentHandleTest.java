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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

public class ConcurrentHandleTest {
  private static final String PROJECT_FILE = "/project/file";

  @Rule public TemporaryFolder rootFolder = new TemporaryFolder();

  FileWatcherEventHandler handler;

  @Mock Consumer<String> create;
  @Mock Consumer<String> modify;
  @Mock Consumer<String> delete;

  Path root;

  @Before
  public void setUp() throws Exception {
    root = rootFolder.getRoot().toPath();
    handler = new FileWatcherEventHandler(new DummyRootProvider(rootFolder.getRoot()));
  }

  @Test
  public void testConcurrentHandle() throws Exception {
    class HandleTask implements Callable<Void> {

      WatchEvent.Kind<Path> eventType;

      public HandleTask(WatchEvent.Kind<Path> eventType) {
        this.eventType = eventType;
      }

      @Override
      public Void call() {
        final Path path = root.resolve(PROJECT_FILE);
        handler.register(path, create, modify, delete);
        handler.handle(path, eventType);
        return null;
      }
    }
    final int n = 50;
    final ExecutorService executor = newFixedThreadPool(5);
    final ArrayList<Callable<Void>> tasks = new ArrayList<>(n);
    final ImmutableList<WatchEvent.Kind<Path>> operations =
        ImmutableList.of(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
    for (int i = 0; i < n; i++) {
      tasks.add(
          new HandleTask(
              operations.get(ThreadLocalRandom.current().nextInt(0, operations.size()))));
    }
    final List<Future<Void>> futures = executor.invokeAll(tasks, n, SECONDS);
    long count =
        futures
            .stream()
            .filter(
                future -> {
                  try {
                    future.get();
                    return false;
                  } catch (ExecutionException ex) {
                    System.out.println(ex.getMessage());
                    return ex.getCause() instanceof ConcurrentModificationException;
                  } catch (Exception ignored) {
                    return false;
                  }
                })
            .count();
    assertEquals(count, 0);
  }

  private static class DummyRootProvider extends RootDirPathProvider {

    public DummyRootProvider(File folder) {
      this.rootFile = folder;
    }
  }
}
