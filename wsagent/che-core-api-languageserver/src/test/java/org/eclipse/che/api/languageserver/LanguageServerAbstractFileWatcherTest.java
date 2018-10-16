/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link LanguageServerUpdateFileWatcher}, {@link LanguageServerCreateFileWatcher}, {@link
 * LanguageServerDeleteFileWatcher}
 */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerAbstractFileWatcherTest {

  private static final String ID = "id";

  private EventService eventService;
  private RegistryContainer registryContainer;
  private LanguageServerAbstractFileWatcher fileWatcher;

  @Mock private LanguageServer languageServer;
  @Mock private WorkspaceService workspaceService;
  @Mock private LanguageServerInitializedEvent event;
  @Mock private PathMatcher pathMatcher;

  private Path tmpFilePath;

  @BeforeMethod
  public void setUp() throws IOException {
    tmpFilePath = Files.createTempFile(null, null);

    registryContainer = new RegistryContainer();
    eventService = new EventService();

    fileWatcher = new LanguageServerCreateFileWatcher(eventService, registryContainer);
    fileWatcher.subscribe();

    when(event.getId()).thenReturn(ID);
    when(event.getLanguageServer()).thenReturn(languageServer);
    when(pathMatcher.matches(any(Path.class)))
        .thenAnswer(
            invocation -> {
              Object argument = invocation.getArguments()[0];
              if (argument.equals(tmpFilePath)) {
                return true;
              } else {
                return false;
              }
            });
  }

  @Test
  public void shouldSubscribeForEvent() throws Exception {
    eventService.publish(event);

    verify(event).getId();
    verify(event).getLanguageServer();
  }

  @Test
  public void shouldSendChangesWhenCalledAcceptMethodIfPathMatches() {
    registryContainer.pathMatcherRegistry.add(ID, ImmutableSet.of(pathMatcher));
    eventService.publish(event);
    when(languageServer.getWorkspaceService()).thenReturn(workspaceService);
    fileWatcher.accept(tmpFilePath);

    verify(languageServer).getWorkspaceService();
    verify(workspaceService).didChangeWatchedFiles(any());
  }

  @Test
  public void shouldNotSendChangesWhenCalledAcceptMethodIfPathDoesNotMatches() {
    registryContainer.pathMatcherRegistry.add(ID, ImmutableSet.of(pathMatcher));
    eventService.publish(event);

    fileWatcher.accept(tmpFilePath.resolve("other"));

    verify(languageServer, never()).getWorkspaceService();
    verify(workspaceService, never()).didChangeWatchedFiles(any());
  }
}
