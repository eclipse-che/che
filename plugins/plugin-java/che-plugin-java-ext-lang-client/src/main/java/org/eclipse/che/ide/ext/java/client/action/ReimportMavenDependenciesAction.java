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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

/**
 * Action for reimport maven dependencies.
 *
 * @author Roman Nikitenko
 * @author Mykola Morhun
 */
@Singleton
public class ReimportMavenDependenciesAction extends AbstractPerspectiveAction {

  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  private final DtoFactory dtoFactory;

  @Inject
  public ReimportMavenDependenciesAction(
      AppContext appContext,
      NotificationManager notificationManager,
      Resources resources,
      JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient,
      DtoFactory dtoFactory,
      JavaLocalizationConstant localization) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        localization.actionReimportDependenciesTitle(),
        localization.actionReimportDependenciesDescription(),
        resources.refresh());
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.javaLanguageExtensionServiceClient = javaLanguageExtensionServiceClient;
    this.dtoFactory = dtoFactory;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(isMavenProjectSelected());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ReImportMavenProjectsCommandParameters paramsDto =
        dtoFactory
            .createDto(ReImportMavenProjectsCommandParameters.class)
            .withProjectsToUpdate(getPathsToSelectedMavenProject());

    javaLanguageExtensionServiceClient
        .reImportMavenProjects(paramsDto)
        .catchError(
            error -> {
              notificationManager.notify(
                  "Problem with reimporting maven dependencies",
                  error.getMessage(),
                  FAIL,
                  EMERGE_MODE);
            });
  }

  private boolean isMavenProjectSelected() {
    return !getPathsToSelectedMavenProject().isEmpty();
  }

  private List<String> getPathsToSelectedMavenProject() {

    final Resource[] resources = appContext.getResources();

    if (resources == null) {
      return Collections.emptyList();
    }

    Set<String> paths = new HashSet<>();

    for (Resource resource : resources) {
      final Project project = resource.getProject();

      if (project != null && project.isTypeOf(MavenAttributes.MAVEN_ID)) {
        paths.add(project.getLocation().toString());
      }
    }

    return new ArrayList<>(paths);
  }
}
