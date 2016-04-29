/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.project.classpath;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.service.ClasspathServiceClient;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDTO;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectClasspathPresenterTest {
    private static final String TEXT = "to be or not to be";

    @Mock
    private ProjectClasspathView     view;
    @Mock
    private ClasspathServiceClient   service;
    @Mock
    private AppContext               appContext;
    @Mock
    private JavaLocalizationConstant locale;
    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private ClasspathResolver        classpathResolve;
    @Mock
    private ClasspathPagePresenter   page1;
    @Mock
    private ClasspathPagePresenter   page2;
    @Mock
    private CurrentProject           currentProject;
    @Mock
    private ProjectConfigDto         projectConfigDto;

    @Mock
    private Promise<List<ClasspathEntryDTO>> promise;
    @Mock
    private Promise<Void>                    updatePromise;

    @Captor
    private ArgumentCaptor<Operation<Void>>                    updateCapture;
    @Captor
    private ArgumentCaptor<Operation<List<ClasspathEntryDTO>>> acceptCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>            errorCaptor;
    @Captor
    private ArgumentCaptor<ConfirmCallback>                    confirmCallbackArgumentCaptor;


    private ProjectClasspathPresenter presenter;

    @Before
    public void setUp() throws Exception {
        Set<ClasspathPagePresenter> classpathPages = new HashSet<>();
        classpathPages.add(page1);
        classpathPages.add(page2);

        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfigDto);
        when(classpathResolve.updateClasspath()).thenReturn(updatePromise);
        when(updatePromise.then(Matchers.<Operation<Void>>anyObject())).thenReturn(updatePromise);
        when(projectConfigDto.getType()).thenReturn(TEXT);

        presenter = new ProjectClasspathPresenter(view,
                                                  classpathPages,
                                                  service,
                                                  appContext,
                                                  locale,
                                                  dialogFactory,
                                                  notificationManager,
                                                  classpathResolve);


    }

    @Test
    public void delegateShouldBeSetToEachPage() throws Exception {
        verify(page1).setUpdateDelegate(presenter);
        verify(page2).setUpdateDelegate(presenter);
    }

    @Test
    public void showConfirmDialogIfHaveUnsavedChanges() throws Exception {
        when(page1.isDirty()).thenReturn(true);

        ConfirmDialog confirmDialog = Mockito.mock(ConfirmDialog.class);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject()))
                .thenReturn(confirmDialog);

        presenter.onCloseClicked();

        verify(confirmDialog).show();
        verify(locale).unsavedChangesTitle();
        verify(locale).messagesPromptSaveChanges();
        verify(locale).buttonSave();
    }

    @Test
    public void clearDataInEachPagesWheWindowIsClosing() throws Exception {
        when(page1.isDirty()).thenReturn(true);
        when(page2.isDirty()).thenReturn(true);

        presenter.onDoneClicked();

        verify(page1).storeChanges();
        verify(page2).storeChanges();
        verify(classpathResolve).updateClasspath();
        verify(updatePromise).then(updateCapture.capture());
        updateCapture.getValue().apply(null);

        verify(view).hideWindow();
        verify(page1).clearData();
        verify(page2).clearData();
    }

    @Test
    public void saveAllChangesWhenSaveButtonIsClicked() throws Exception {
        when(page1.isDirty()).thenReturn(true);
        when(page2.isDirty()).thenReturn(true);

        presenter.onDoneClicked();

        verify(page1).storeChanges();
        verify(page2).storeChanges();
    }

    @Test
    public void pageShouldBeShowedIfConfigurationWasSelected() throws Exception {
        AcceptsOneWidget acceptsOneWidget = Mockito.mock(AcceptsOneWidget.class);
        when(view.getConfigurationsContainer()).thenReturn(acceptsOneWidget);

        presenter.onConfigurationSelected(page1);

        verify(view).getConfigurationsContainer();
        verify(page1).go(acceptsOneWidget);
    }

    @Test
    public void windowShouldBeShowedWithoutErrors() throws Exception {
        List<ClasspathEntryDTO> arg = new ArrayList<>();
        when(projectConfigDto.getPath()).thenReturn("path");
        when(service.getClasspath(anyString())).thenReturn(promise);
        when(promise.then(Matchers.<Operation<List<ClasspathEntryDTO>>>anyObject())).thenReturn(promise);
        when(page1.getCategory()).thenReturn(TEXT);
        when(page2.getCategory()).thenReturn(TEXT);

        presenter.show();

        verify(service).getClasspath("path");
        verify(promise).then(acceptCaptor.capture());
        acceptCaptor.getValue().apply(arg);

        verify(classpathResolve).resolveClasspathEntries(arg);
        verify(view).setPages(Matchers.<Map<String, Set<ClasspathPagePresenter>>>anyObject());
        verify(view).selectPage(Matchers.<ClasspathPagePresenter>anyObject());
    }

    @Test
    public void showErrorNotificationIfReadingClasspathHasError() throws Exception {
        PromiseError promiseError = mock(PromiseError.class);
        when(promiseError.getMessage()).thenReturn(TEXT);
        when(projectConfigDto.getPath()).thenReturn("path");
        when(service.getClasspath(anyString())).thenReturn(promise);
        when(promise.then(Matchers.<Operation<List<ClasspathEntryDTO>>>anyObject())).thenReturn(promise);
        when(page1.getCategory()).thenReturn(TEXT);
        when(page2.getCategory()).thenReturn(TEXT);

        presenter.show();

        verify(service).getClasspath("path");
        verify(promise).catchError(errorCaptor.capture());
        errorCaptor.getValue().apply(promiseError);
        verify(notificationManager).notify("Problems with getting classpath", TEXT, FAIL, EMERGE_MODE);
    }
}
