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
package org.eclipse.che.api.languageserver;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.function.Consumer;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.LanguageServerFileWatcher;
import org.eclipse.che.api.languageserver.registry.ServerInitializer;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerFileWatcherTest {

  @Mock private LanguageServerLauncher launcher;
  @Mock private LanguageServer server;
  @Mock private FileWatcherManager watcherManager;
  @Mock private ServerInitializer initializer;
  @Captor private ArgumentCaptor<Consumer<String>> changedCaptor;

  private LanguageServerFileWatcher watcher;

  @AfterMethod
  public void tearDown() throws Exception {
    if (watcher != null) {
      watcher.removeAllWatchers();
    }
  }

  @Test
  public void testShouldAddObserver() throws Exception {
    watcher = new LanguageServerFileWatcher(watcherManager, initializer);
    verify(initializer).addObserver(any());
  }

  @Test
  public void testRegisterFileWatcher() throws Exception {
    ArgumentCaptor<ServerInitializerObserver> argumentCaptor =
        ArgumentCaptor.forClass(ServerInitializerObserver.class);
    watcher = new LanguageServerFileWatcher(watcherManager, initializer);
    verify(initializer).addObserver(argumentCaptor.capture());
    ServerInitializerObserver value = argumentCaptor.getValue();

    LanguageServerDescription description =
        new LanguageServerDescription(
            "foo",
            Collections.singletonList("bar"),
            Collections.emptyList(),
            Collections.singletonList("glob:*.foo"));
    when(launcher.getDescription()).thenReturn(description);
    value.onServerInitialized(launcher, server, null, null);

    ArgumentCaptor<PathMatcher> pathMatcherCaptor = ArgumentCaptor.forClass(PathMatcher.class);
    verify(watcherManager).registerByMatcher(pathMatcherCaptor.capture(), any(), any(), any());
    assertTrue(pathMatcherCaptor.getValue().matches(new File("bar.foo").toPath()));
  }

  @Test
  public void testSendNotification() throws Exception {
    ArgumentCaptor<ServerInitializerObserver> argumentCaptor =
        ArgumentCaptor.forClass(ServerInitializerObserver.class);
    watcher = new LanguageServerFileWatcher(watcherManager, initializer);
    verify(initializer).addObserver(argumentCaptor.capture());
    ServerInitializerObserver value = argumentCaptor.getValue();

    LanguageServerDescription description =
        new LanguageServerDescription(
            "foo",
            Collections.singletonList("bar"),
            Collections.emptyList(),
            Collections.singletonList("glob:*.foo"));
    when(launcher.getDescription()).thenReturn(description);

    WorkspaceService workspaceService = mock(WorkspaceService.class);
    when(server.getWorkspaceService()).thenReturn(workspaceService);

    value.onServerInitialized(launcher, server, null, null);

    verify(watcherManager).registerByMatcher(any(), any(), changedCaptor.capture(), any());

    changedCaptor.getValue().accept("/p/bar.foo");

    verify(workspaceService).didChangeWatchedFiles(any());
  }
}
