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
package org.eclipse.che.ide.ext.java.client.formatter.preferences;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.formatter.JavaFormatterServiceClient;

/** Preference page describes functionality to import code formatter for the project. */
@Singleton
public class FormatterPreferencePagePresenter extends AbstractPreferencePagePresenter
    implements FormatterPreferencePageView.ActionDelegate {

  private JavaFormatterServiceClient javaFormatterServiceClient;
  private AppContext appContext;
  private NotificationManager notificationManager;
  private FormatterPreferencePageView view;

  @Inject
  public FormatterPreferencePagePresenter(
      JavaFormatterServiceClient javaFormatterServiceClient,
      AppContext appContext,
      JavaLocalizationConstant javaLocalizationConstant,
      NotificationManager notificationManager,
      FormatterPreferencePageView view) {
    super(
        javaLocalizationConstant.formatterPreferencesJavaTitle(),
        javaLocalizationConstant.formatterPreferencesGroupTitle());
    this.javaFormatterServiceClient = javaFormatterServiceClient;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
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
    final StatusNotification notification =
        new StatusNotification("Importing Formatter...", PROGRESS, FLOAT_MODE);
    notificationManager.notify(notification);
    if (view.isWorkspaceTarget()) {
      importForWorkspace(notification);
    } else {
      importForProject(notification);
    }
  }

  private void importForProject(StatusNotification notification) {
    Project rootProject = appContext.getRootProject();
    if (rootProject == null) {
      notification.setStatus(FAIL);
      notification.setTitle("Failed to import code formatter. No project selected.");
      return;
    }
    javaFormatterServiceClient
        .updateProjectFormatter(view.getFileContent(), rootProject.getPath())
        .then(
            content -> {
              onSuccessImported(notification);
            })
        .catchError(
            error -> {
              onFailedImported(notification);
            });
  }

  private void importForWorkspace(StatusNotification notification) {
    javaFormatterServiceClient
        .updateRootFormatter(view.getFileContent())
        .then(
            content -> {
              onSuccessImported(notification);
            })
        .catchError(
            error -> {
              onFailedImported(notification);
            });
  }

  private void onFailedImported(StatusNotification notification) {
    notification.setStatus(FAIL);
    notification.setTitle("Failed to import code formatter");
  }

  private void onSuccessImported(StatusNotification notification) {
    notification.setStatus(SUCCESS);
    notification.setTitle("Formatter imported successfully");
  }

  @Override
  public void showErrorMessage(String error) {
    notificationManager.notify(error);
  }
}
