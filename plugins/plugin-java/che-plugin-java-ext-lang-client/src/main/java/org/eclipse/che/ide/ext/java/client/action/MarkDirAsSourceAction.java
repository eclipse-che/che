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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;

/**
 * The action which marks a folder into the project as source folder.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MarkDirAsSourceAction extends AbstractPerspectiveAction {
  private final AppContext appContext;
  private final JavaLanguageExtensionServiceClient extensionService;
  private final ClasspathResolver classpathResolver;
  private final NotificationManager notificationManager;

  @Inject
  public MarkDirAsSourceAction(
      JavaResources javaResources,
      AppContext appContext,
      JavaLanguageExtensionServiceClient extensionService,
      ClasspathResolver classpathResolver,
      NotificationManager notificationManager,
      JavaLocalizationConstant locale) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.markDirectoryAsSourceAction(),
        locale.markDirectoryAsSourceDescription(),
        javaResources.sourceFolder());

    this.appContext = appContext;
    this.extensionService = extensionService;
    this.classpathResolver = classpathResolver;
    this.notificationManager = notificationManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource resource = appContext.getResource();

    checkState(resource instanceof Container, "Parent should be a container");

    final Optional<Project> project = resource.getRelatedProject();

    checkState(project.isPresent());

    extensionService
        .classpathTree(project.get().getLocation().toString())
        .then(
            classpathEntries -> {
              classpathResolver.resolveClasspathEntries(classpathEntries);
              classpathResolver.getSources().add(resource.getLocation().toString());
              classpathResolver.updateClasspath();
            })
        .catchError(
            error -> {
              notificationManager.notify(
                  "Can't get classpath", error.getMessage(), FAIL, EMERGE_MODE);
            });
  }

  @Override
  public void updateInPerspective(ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length != 1) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    Resource resource = resources[0];
    final boolean inJavaProject = isJavaProject(resource.getRelatedProject().get());

    e.getPresentation()
        .setEnabledAndVisible(
            inJavaProject
                && resource.isFolder()
                && !resource.getMarker(SourceFolderMarker.ID).isPresent());
  }
}
