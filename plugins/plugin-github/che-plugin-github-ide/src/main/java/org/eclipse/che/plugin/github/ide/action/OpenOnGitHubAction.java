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
package org.eclipse.che.plugin.github.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Collections;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.browser.BrowserUtils;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;
import org.eclipse.che.plugin.github.shared.GitHubUrlUtils;

/** @author Vitalii Parfonov */
@Singleton
public class OpenOnGitHubAction extends BaseAction implements ActivePartChangedHandler {

  private EditorAgent editorAgent;
  private GitServiceClient gitServiceClient;
  private final AppContext appContext;
  private NotificationManager notificationManager;
  private boolean editorInFocus;

  @Inject
  public OpenOnGitHubAction(
      EditorAgent editorAgent,
      GitHubLocalizationConstant locale,
      GitServiceClient gitServiceClient,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager) {
    super(locale.openOnGitHubAction());
    this.editorAgent = editorAgent;
    this.gitServiceClient = gitServiceClient;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  /** {@inheritDoc} */
  @Override
  public void update(ActionEvent event) {
    final Resource resources = appContext.getResource();
    String gitRepoUrl = resources.getProject().getAttribute("git.repository.remotes");
    if (GitHubUrlUtils.isGitHubUrl(gitRepoUrl)) {
      event.getPresentation().setEnabledAndVisible(true);
    } else {
      event.getPresentation().setEnabledAndVisible(false);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    if (editorInFocus) {
      final EditorPartPresenter editorPart = editorAgent.getActiveEditor();
      if (editorPart == null || !(editorPart instanceof TextEditor)) {
        return;
      }
      Document document = ((TextEditor) editorPart).getDocument();
      openOnGitHubUrl(document);
    } else {
      Resource resource = appContext.getResource();
      openOnGitHubUrl(resource);
    }
  }

  private void openOnGitHubUrl(Document document) {
    Resource file = (Resource) document.getFile();
    Project project = file.getProject();
    Path projectPath = project.getLocation();
    String httpsUrl = getGitHubRepositoryUrl(project);
    String filePath = file.getLocation().makeRelativeTo(projectPath).toString();
    int lineStart;
    int lineEnd;
    if (document.getSelectedTextRange() != null) {
      lineStart = document.getSelectedTextRange().getFrom().getLine() + 1;
      lineEnd = document.getSelectedTextRange().getTo().getLine() + 1;
    } else {
      lineStart = lineEnd = document.getCursorPosition().getLine() + 1;
    }

    gitServiceClient
        .getStatus(projectPath, Collections.emptyList())
        .then(
            status -> {
              String refName = status.getRefName();
              String blobUrl =
                  GitHubUrlUtils.getBlobUrl(httpsUrl, refName, filePath, lineStart, lineEnd);
              BrowserUtils.openInNewTab(blobUrl);
            })
        .catchError(
            error -> {
              notificationManager.notify("", error.getMessage());
            });
  }

  private void openOnGitHubUrl(Resource resource) {
    final Project project = resource.getProject();
    Path projectPath = project.getLocation();
    String httpsUrl = getGitHubRepositoryUrl(project);
    String path = resource.getLocation().makeRelativeTo(projectPath).toString();
    gitServiceClient
        .getStatus(projectPath, Collections.emptyList())
        .then(
            status -> {
              String url;
              if (resource.isFile()) {
                url = GitHubUrlUtils.getBlobUrl(httpsUrl, status.getRefName(), path);
              } else {
                url = GitHubUrlUtils.getTreeUrl(httpsUrl, status.getRefName(), path);
              }
              BrowserUtils.openInNewTab(url);
            })
        .catchError(
            error -> {
              notificationManager.notify("", error.getMessage());
            });
  }

  private String getGitHubRepositoryUrl(Project project) {
    String gitRepoUrl = project.getAttribute("git.repository.remotes");
    return GitHubUrlUtils.toHttpsIfNeed(gitRepoUrl);
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    editorInFocus = event.getActivePart() instanceof EditorPartPresenter;
  }
}
