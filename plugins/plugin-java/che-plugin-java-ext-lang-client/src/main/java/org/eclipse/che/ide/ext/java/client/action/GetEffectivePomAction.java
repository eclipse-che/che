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
package org.eclipse.che.ide.ext.java.client.action;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.MavenResources;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

/**
 * Action for generating effective pom.
 *
 * @author Valeriy Svydenko
 * @author Mykola Morhun
 */
@Singleton
public class GetEffectivePomAction extends AbstractPerspectiveAction {

  private final EditorAgent editorAgent;
  private final NotificationManager notificationManager;
  private final JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  private final AppContext appContext;

  @Inject
  public GetEffectivePomAction(
      JavaLocalizationConstant localization,
      MavenResources mavenResources,
      EditorAgent editorAgent,
      NotificationManager notificationManager,
      JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient,
      AppContext appContext) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        localization.actionGetEffectivePomTitle(),
        localization.actionGetEffectivePomDescription(),
        mavenResources.maven());
    this.editorAgent = editorAgent;
    this.notificationManager = notificationManager;
    this.javaLanguageExtensionServiceClient = javaLanguageExtensionServiceClient;
    this.appContext = appContext;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    final Resource resource = appContext.getResource();
    if (resource == null) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Project project = resource.getProject();
    if (project == null) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    event.getPresentation().setEnabledAndVisible(project.isTypeOf(MavenAttributes.MAVEN_ID));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource resource = appContext.getResource();
    checkNotNull(resource);

    final Project project = resource.getProject();
    checkState(MavenAttributes.MAVEN_ID.equals(project.getType()));

    javaLanguageExtensionServiceClient
        .effectivePom(project.getLocation().toString())
        .then(
            content -> {
              showEffectivePomInEditor(project, content);
            })
        .catchError(
            error -> {
              notificationManager.notify(
                  "Problem with generating effective pom file",
                  error.getMessage(),
                  FAIL,
                  EMERGE_MODE);
            });
  }

  /**
   * Shows or updates effective pom editor tab for specified project.
   *
   * @param project project for which effective pom was requested
   * @param content effective pom
   */
  private void showEffectivePomInEditor(Project project, String content) {
    final String artifactId = project.getAttributes().get(MavenAttributes.ARTIFACT_ID).get(0);
    final String effectivePomPath = "synthetic-file-" + artifactId + "-pom.xml";

    EditorPartPresenter effectivePomTab =
        editorAgent.getOpenedEditor(Path.valueOf(effectivePomPath));

    if (effectivePomTab == null) {
      // open new editor tab
      editorAgent.openEditor(
          new SyntheticFile(effectivePomPath, artifactId + " [effective pom]", content));
    } else {
      // update opened tab
      if (effectivePomTab instanceof TextEditor) {
        Document document = ((TextEditor) effectivePomTab).getDocument();
        document.replace(0, document.getContents().length(), content);
        editorAgent.activateEditor(effectivePomTab);
      } else {
        editorAgent.closeEditor(effectivePomTab);
        editorAgent.openEditor(
            new SyntheticFile(effectivePomPath, artifactId + " [effective pom]", content));
      }
    }
  }
}
