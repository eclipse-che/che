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
package org.eclipse.che.plugin.maven.client.actions;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.GetEffectivePomParameters;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.client.MavenResources;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

/**
 * Action for generating effective pom.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class GetEffectivePomAction extends AbstractPerspectiveAction {

  private final EditorAgent editorAgent;
  private final NotificationManager notificationManager;
  private final JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  private final AppContext appContext;
  private final DtoFactory dtoFactory;

  @Inject
  public GetEffectivePomAction(
      MavenLocalizationConstant constant,
      MavenResources mavenResources,
      EditorAgent editorAgent,
      NotificationManager notificationManager,
      JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient,
      AppContext appContext,
      DtoFactory dtoFactory) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        constant.actionGetEffectivePomTitle(),
        constant.actionGetEffectivePomDescription(),
        mavenResources.maven());
    this.editorAgent = editorAgent;
    this.notificationManager = notificationManager;
    this.javaLanguageExtensionServiceClient = javaLanguageExtensionServiceClient;
    this.appContext = appContext;
    this.dtoFactory = dtoFactory;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {

    final Resource resource = appContext.getResource();
    if (resource == null) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Optional<Project> project = resource.getRelatedProject();
    if (!project.isPresent()) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    event.getPresentation().setEnabledAndVisible(project.get().isTypeOf(MAVEN_ID));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource[] resources = appContext.getResources();
    checkState(resources != null && resources.length == 1);

    final Project project = resources[0].getRelatedProject().get();
    checkState(MAVEN_ID.equals(project.getType()));

    GetEffectivePomParameters paramsDto =
        dtoFactory
            .createDto(GetEffectivePomParameters.class)
            .withProjectPath(project.getLocation().toString());

    javaLanguageExtensionServiceClient
        .effectivePom(paramsDto)
        .then(
            content -> {
              editorAgent.openEditor(
                  new SyntheticFile(
                      "pom.xml",
                      project.getAttributes().get(MavenAttributes.ARTIFACT_ID).get(0)
                          + " [effective pom]",
                      content));
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
}
