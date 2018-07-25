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
package org.eclipse.che.api.languageserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.nio.file.PathMatcher;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.lsp4j.services.LanguageServer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerFileWatcherTest {

  private static final String ID = "id";

  @Mock private FileWatcherManager watcherManager;

  private EventService eventService;
  private RegistryContainer registryContainer;

  @Mock private LanguageServer languageServer;
  @Mock private LanguageServerInitializedEvent languageServerInitializedEvent;
  @Mock private PathMatcher pathMatcher;

  @BeforeMethod
  public void setUp() {
    registryContainer = new RegistryContainer();
    eventService = new EventService();

    new LanguageServerFileWatcher(watcherManager, eventService, registryContainer).subscribe();

    when(languageServerInitializedEvent.getId()).thenReturn(ID);
    when(languageServerInitializedEvent.getLanguageServer()).thenReturn(languageServer);
  }

  @Test
  public void shouldSubscribeForEvent() throws Exception {
    eventService.publish(languageServerInitializedEvent);

    verify(languageServerInitializedEvent, times(2)).getId();
    verify(languageServerInitializedEvent).getLanguageServer();
  }

  @Test
  public void shouldRegisterOperationsForFileWatcher() {
    registryContainer.pathMatcherRegistry.add(ID, ImmutableSet.of(pathMatcher));
    eventService.publish(languageServerInitializedEvent);

    verify(watcherManager).registerByMatcher(eq(pathMatcher), any(), any(), any());
  }
}
