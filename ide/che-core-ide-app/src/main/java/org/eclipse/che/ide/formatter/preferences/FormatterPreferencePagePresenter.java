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
package org.eclipse.che.ide.formatter.preferences;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resource.Path;

/** Preference page describes functionality to import code formatter for the project. */
@Singleton
public class FormatterPreferencePagePresenter extends AbstractPreferencePagePresenter
    implements FormatterPreferencePageView.ActionDelegate {

  private ProjectServiceClient projectServiceClient;
  private NotificationManager notificationManager;
  private AppContext appContext;
  private FormatterPreferencePageView view;

  @Inject
  public FormatterPreferencePagePresenter(
      CoreLocalizationConstant localizationConstant,
      ProjectServiceClient projectServiceClient,
      NotificationManager notificationManager,
      AppContext appContext,
      FormatterPreferencePageView view) {
    super(localizationConstant.formatterPreferencesTitle());
    this.projectServiceClient = projectServiceClient;
    this.notificationManager = notificationManager;
    this.appContext = appContext;
    this.view = view;
    view.setDelegate(this);
  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public void storeChanges() {}

  @Override
  public void revertChanges() {}

  @Override
  public void go(AcceptsOneWidget container) {
    view.showDialog();
    container.setWidget(view);
  }

  @Override
  public void onDirtyChanged() {
    delegate.onDirtyChanged();
  }

  @Override
  public void onImportButtonClicked() {
    String pathToFormatter = "/.che/che-formatter.xml";
    Path filePath;
    if (view.isWorkspaceTarget()) {
      filePath = new Path(pathToFormatter);
    } else {
      Project rootProject = appContext.getRootProject();
      if (rootProject == null) {
        notificationManager.notify("No project in context");
        return;
      }
      String path = rootProject.getPath();
      filePath = new Path(path + pathToFormatter);
    }

    final StatusNotification notification =
        new StatusNotification("Importing Formatter...", PROGRESS, FLOAT_MODE);
    notificationManager.notify(notification);

    String fileContent = view.getFileContent();
    projectServiceClient
        .getItem(filePath)
        .then(
            arg -> {
              updateFormatter(filePath, fileContent, notification);
            })
        .catchError(
            arg -> {
              createFormatter(filePath, fileContent, notification);
            });
  }

  @Override
  public void showErrorMessage(String error) {
    notificationManager.notify(error);
  }

  private void createFormatter(Path filePath, String fileContent, StatusNotification notification) {
    projectServiceClient
        .createFile(filePath, fileContent)
        .then(
            arg -> {
              notification.setStatus(SUCCESS);
              notification.setTitle("Formatter imported successfully");
            })
        .catchError(
            arg -> {
              notification.setStatus(FAIL);
              notification.setTitle("Failed to import code formatter");
            });
  }

  private void updateFormatter(Path filePath, String fileContent, StatusNotification notification) {
    projectServiceClient
        .setFileContent(filePath, fileContent)
        .then(
            arg -> {
              notification.setStatus(SUCCESS);
              notification.setTitle("Code formatter imported successfully");
            })
        .catchError(
            arg -> {
              notification.setStatus(FAIL);
              notification.setTitle("Failed to import code formatter");
            });
  }
}
