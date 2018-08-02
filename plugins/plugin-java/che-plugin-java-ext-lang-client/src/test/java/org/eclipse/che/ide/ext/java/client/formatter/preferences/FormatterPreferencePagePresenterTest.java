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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.formatter.JavaFormatterServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(GwtMockitoTestRunner.class)
public class FormatterPreferencePagePresenterTest {
  private static final String FORMATTER_CONTENT = "content";

  @Mock private JavaFormatterServiceClient javaFormatterServiceClient;
  @Mock private AppContext appContext;
  @Mock private JavaLocalizationConstant javaLocalizationConstant;
  @Mock private NotificationManager notificationManager;
  @Mock private FormatterPreferencePageView view;
  @Mock private Promise<Void> updatingResponce;

  private FormatterPreferencePagePresenter presenter;

  @Before
  public void setUp() throws Exception {
    when(javaLocalizationConstant.formatterPreferencesJavaTitle()).thenReturn("");
    when(javaLocalizationConstant.formatterPreferencesGroupTitle()).thenReturn("");
    when(view.getFileContent()).thenReturn(FORMATTER_CONTENT);
    when(javaFormatterServiceClient.updateProjectFormatter(anyString(), anyString()))
        .thenReturn(updatingResponce);
    when(javaFormatterServiceClient.updateRootFormatter(anyString())).thenReturn(updatingResponce);
    when(updatingResponce.then(org.mockito.ArgumentMatchers.<Operation<Void>>anyObject()))
        .thenReturn(updatingResponce);

    presenter =
        new FormatterPreferencePagePresenter(
            javaFormatterServiceClient,
            appContext,
            javaLocalizationConstant,
            notificationManager,
            view);
  }

  @Test
  public void presenterShouldBeInitialized() throws Exception {
    verify(javaLocalizationConstant).formatterPreferencesJavaTitle();
    verify(javaLocalizationConstant).formatterPreferencesGroupTitle();
    verify(view).setDelegate(presenter);
  }

  @Test
  public void formatterPageCanNotBeUnsaved() throws Exception {
    assertFalse(presenter.isDirty());
  }

  @Test
  public void pageSouldBeShown() throws Exception {
    AcceptsOneWidget acceptsOneWidget = mock(AcceptsOneWidget.class);

    presenter.go(acceptsOneWidget);

    verify(view).showDialog();
    verify(acceptsOneWidget).setWidget(view);
  }

  @Test
  public void projectFormatterIsNotImportedIfRootProjectIsNull() throws Exception {
    when(appContext.getRootProject()).thenReturn(null);
    when(view.isWorkspaceTarget()).thenReturn(false);

    presenter.onImportButtonClicked();

    verify(notificationManager).notify((Notification) anyObject());
    verify(javaFormatterServiceClient, never()).updateProjectFormatter(anyString(), anyString());
    verify(javaFormatterServiceClient, never()).updateRootFormatter(anyString());
  }

  @Test
  public void projectFormatterIsImported() throws Exception {
    String projectPath = "/path";
    Project rootProject = mock(Project.class);

    when(appContext.getRootProject()).thenReturn(rootProject);
    when(view.isWorkspaceTarget()).thenReturn(false);
    when(rootProject.getPath()).thenReturn(projectPath);

    presenter.onImportButtonClicked();

    verify(notificationManager).notify((Notification) anyObject());
    verify(view).getFileContent();
    verify(javaFormatterServiceClient).updateProjectFormatter(FORMATTER_CONTENT, projectPath);
    verify(javaFormatterServiceClient, never()).updateRootFormatter(anyString());
  }

  @Test
  public void workspaceFormatterIsImported() throws Exception {
    when(view.isWorkspaceTarget()).thenReturn(true);

    presenter.onImportButtonClicked();

    verify(notificationManager).notify((Notification) anyObject());
    verify(view).getFileContent();
    verify(javaFormatterServiceClient).updateRootFormatter(FORMATTER_CONTENT);
    verify(javaFormatterServiceClient, never()).updateProjectFormatter(anyString(), anyString());
  }

  @Test
  public void showErrorMessage() throws Exception {
    String errorMessage = "error";
    presenter.showErrorMessage(errorMessage);

    verify(notificationManager).notify(errorMessage);
  }
}
