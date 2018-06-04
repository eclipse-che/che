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
import static org.eclipse.che.api.watcher.server.impl.FileWatcherUtils.toInternalPath;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link FileWatcherEventHandler} */
@RunWith(MockitoJUnitRunner.class)
public class FileWatcherEventHandlerTest {
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
  public void shouldHandleRegisteredPathWhenCreate() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    handler.register(path, create, modify, delete);

    handler.handle(path, ENTRY_CREATE);

    verify(create).accept(toInternalPath(root, path));
  }

  @Test
  public void shouldHandleRegisteredPathWhenModify() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    handler.register(path, create, modify, delete);

    handler.handle(path, ENTRY_MODIFY);

    verify(modify).accept(toInternalPath(root, path));
  }

  @Test
  public void shouldHandleRegisteredPathWhenDelete() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    handler.register(path, create, modify, delete);

    handler.handle(path, ENTRY_DELETE);

    verify(delete).accept(toInternalPath(root, path));
  }

  @Test
  public void shouldHandleRegisteredPathWhenCreateFileForFileAndForParent() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    handler.register(path, create, modify, delete);
    handler.register(path.getParent(), create, modify, delete);

    handler.handle(path, ENTRY_CREATE);

    verify(create, times(2)).accept(toInternalPath(root, path));
  }

  @Test
  public void shouldNotHandleNotRegisteredPath() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    handler.register(path.resolve("one"), create, modify, delete);
    handler.register(path.resolve("two"), create, modify, delete);

    handler.handle(path, ENTRY_CREATE);

    verify(create, never()).accept(toInternalPath(root, path));
  }

  @Test
  public void shouldNotHandleUnRegisteredPath() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    int id = handler.register(path, create, modify, delete);

    handler.unRegister(id);

    handler.handle(path, ENTRY_CREATE);

    verify(create, never()).accept(toInternalPath(root, path));
  }

  @Test
  public void shouldNotHandleUnRegisteredFileButShouldHandleFilesParent() throws Exception {
    Path path = root.resolve(PROJECT_FILE);
    handler.register(path.getParent(), create, modify, delete);
    int id = handler.register(path, create, modify, delete);
    handler.unRegister(id);

    handler.handle(path, ENTRY_CREATE);

    verify(create).accept(toInternalPath(root, path));
  }

  private static class DummyRootProvider extends RootDirPathProvider {

    public DummyRootProvider(File folder) {
      this.rootFile = folder;
    }
  }
}
