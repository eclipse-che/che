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
package org.eclipse.che.plugin.maven.lsp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyManager;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LaunchingStrategy;
import org.eclipse.che.api.languageserver.launcher.PerWorkspaceLaunchingStrategy;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.reconcile.PomReconciler;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

@Singleton
public class MavenLanguageServerLauncher implements LanguageServerLauncher {

  @Inject
  public MavenLanguageServerLauncher(
      MavenProjectManager mavenProjectManager,
      EditorWorkingCopyManager editorWorkingCopyManager,
      EventService eventService) {
    this.mavenProjectManager = mavenProjectManager;
    this.editorWorkingCopyManager = editorWorkingCopyManager;
    this.eventService = eventService;
  }

  private MavenProjectManager mavenProjectManager;
  private EditorWorkingCopyManager editorWorkingCopyManager;
  private EventService eventService;

  public LanguageServer launch(String projectPath, LanguageClient client)
      throws LanguageServerException {
    PomReconciler reconciler =
        new PomReconciler(mavenProjectManager, editorWorkingCopyManager, eventService, client);
    return new MavenLanguageServer(client, reconciler);
  }

  public boolean isAbleToLaunch() {
    return true;
  }

  @Override
  public LanguageServerDescription getDescription() {
    return new LanguageServerDescription(
        "org.eclipse.che.plugin.maven", Collections.singletonList("pom"), Collections.emptyList());
  }

  @Override
  public LaunchingStrategy getLaunchingStrategy() {
    return PerWorkspaceLaunchingStrategy.INSTANCE;
  }
}
