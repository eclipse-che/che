/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.watcher.server.impl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link FileWatcherByPathValue} */
@RunWith(MockitoJUnitRunner.class)
public class FileWatcherByPathValueTest {
  private static final int OPERATION_ID = 0;
  private static final String FILE_NAME = "name.ext";

  @Rule public TemporaryFolder rootFolder = new TemporaryFolder();

  @Mock FileWatcherEventHandler handler;
  @Mock FileWatcherService service;
  @InjectMocks FileWatcherByPathValue watcher;

  @Mock Consumer<String> create;
  @Mock Consumer<String> modify;
  @Mock Consumer<String> delete;

  Path root;

  @Before
  public void setUp() throws Exception {
    root = rootFolder.getRoot().toPath();
  }

  @Test
  public void shouldRegisterInServiceWhenWatchFile() throws Exception {
    Path path = root.resolve(FILE_NAME);

    watcher.watch(path, create, modify, delete);

    verify(service).register(path.getParent());
  }

  @Test
  public void shouldRegisterInServiceWhenWatchDirectory() throws Exception {
    Path path = root.resolve(FILE_NAME);

    watcher.watch(path.getParent(), create, modify, delete);

    verify(service).register(path.getParent());
  }

  @Test
  public void shouldRegisterInHandlerWhenWatch() throws Exception {
    Path path = root.resolve(FILE_NAME);

    watcher.watch(path, create, modify, delete);

    verify(handler).register(path, create, modify, delete);
  }

  @Test
  public void shouldUnRegisterInServiceWhenUnWatch() throws Exception {
    Path path = mock(Path.class);
    when(handler.unRegister(anyInt())).thenReturn(path);

    watcher.unwatch(OPERATION_ID);

    verify(service).unRegister(path);
  }

  @Test
  public void shouldUnRegisterInHandlerWhenUnWatch() throws Exception {
    Path path = mock(Path.class);
    when(handler.unRegister(anyInt())).thenReturn(path);

    watcher.unwatch(OPERATION_ID);

    verify(handler).unRegister(OPERATION_ID);
  }
}
