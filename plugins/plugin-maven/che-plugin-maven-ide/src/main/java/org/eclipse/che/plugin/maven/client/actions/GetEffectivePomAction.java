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
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.client.MavenResources;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;
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
  private final MavenServerServiceClient mavenServerServiceClient;
  private final AppContext appContext;

  @Inject
  public GetEffectivePomAction(
      MavenLocalizationConstant constant,
      MavenResources mavenResources,
      EditorAgent editorAgent,
      NotificationManager notificationManager,
      MavenServerServiceClient mavenServerServiceClient,
      AppContext appContext) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        constant.actionGetEffectivePomTitle(),
        constant.actionGetEffectivePomDescription(),
        mavenResources.maven());
    this.editorAgent = editorAgent;
    this.notificationManager = notificationManager;
    this.mavenServerServiceClient = mavenServerServiceClient;
    this.appContext = appContext;
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

    mavenServerServiceClient
        .getEffectivePom(project.getLocation().toString())
        .then(
            new Operation<String>() {
              @Override
              public void apply(String content) throws OperationException {
                editorAgent.openEditor(
                    new SyntheticFile(
                        "pom.xml",
                        project.getAttributes().get(MavenAttributes.ARTIFACT_ID).get(0)
                            + " [effective pom]",
                        content));
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    "Problem with generating effective pom file",
                    arg.getMessage(),
                    FAIL,
                    EMERGE_MODE);
              }
            });
  }
}
