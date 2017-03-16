/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.explorer.project.synchronize;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ProjectConfigSynchronizedTest {
    private final static String PROJECT_NAME         = "project name";
    private final static String PROJECT_LOCATION     = "/project/location";
    private final static String SYNCH_DIALOG_TITLE   = "Synchronize dialog title";
    private final static String SYNCH_DIALOG_CONTENT = "Synchronize dialog content";
    private final static String IMPORT_BUTTON        = "Import";
    private final static String REMOVE_BUTTON        = "Remove";

    @Mock
    private EventBus                 eventBus;
    @Mock
    private AppContext               appContext;
    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private CoreLocalizationConstant locale;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private ChangeLocationWidget     changeLocationWidget;

    @InjectMocks
    private ProjectConfigSynchronized projectConfigSynchronized;

    @Mock
    private Project                      rootProject;
    @Mock
    private Optional<Marker>             problemMarker;
    @Mock
    private Project.ProblemProjectMarker problemProjectMarker;
    @Mock
    private ConfirmDialog                confirmDialog;
    @Mock
    private ConfirmDialog                changeConfirmDialog;
    @Mock
    private Promise<Void>                deleteProjectPromise;
    @Mock
    private Promise<Project>             projectPromise;
    @Mock
    private SourceStorageDto             sourceStorage;
    @Mock
    private Container                    wsRoot;
    @Mock
    private Project.ProjectRequest       projectRequest;

    @Captor
    private ArgumentCaptor<ConfirmCallback> confirmCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<CancelCallback>  cancelCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<Operation<Void>> projectDeleted;

    Map<Integer, String> problems;

    @Before
    public void setUp() {
        when(appContext.getRootProject()).thenReturn(rootProject);
        when(rootProject.getMarker(Project.ProblemProjectMarker.PROBLEM_PROJECT)).thenReturn(problemMarker);
        when(rootProject.getName()).thenReturn(PROJECT_NAME);
        when(problemMarker.isPresent()).thenReturn(true);
        when(problemMarker.get()).thenReturn(problemProjectMarker);

        problems = new HashMap<>();
        problems.put(10, "Error project");
        when(problemProjectMarker.getProblems()).thenReturn(problems);

        when(locale.synchronizeDialogTitle()).thenReturn(SYNCH_DIALOG_TITLE);
        when(locale.existInWorkspaceDialogContent(PROJECT_NAME)).thenReturn(SYNCH_DIALOG_CONTENT);
        when(locale.buttonImport()).thenReturn(IMPORT_BUTTON);
        when(locale.buttonRemove()).thenReturn(REMOVE_BUTTON);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               anyString(),
                                               anyString(),
                                               anyObject(),
                                               anyObject())).thenReturn(confirmDialog);

        when(rootProject.getSource()).thenReturn(sourceStorage);
        when(sourceStorage.getLocation()).thenReturn(PROJECT_LOCATION);
        when(appContext.getWorkspaceRoot()).thenReturn(wsRoot);
        when(wsRoot.importProject()).thenReturn(projectRequest);
        when(projectRequest.withBody(anyObject())).thenReturn(projectRequest);
        when(projectRequest.send()).thenReturn(projectPromise);
    }

    @Test
    public void shouldSubscribeOnSelectionChangedEvent() throws Exception {
        verify(eventBus).addHandler(SelectionChangedEvent.TYPE, projectConfigSynchronized);
    }

    @Test
    public void dialogIsNotShownIfRootProjectIsNull() throws Exception {
        when(appContext.getRootProject()).thenReturn(null);

        projectConfigSynchronized.onSelectionChanged(null);

        verify(confirmDialog, never()).show();
    }

    @Test
    public void dialogIsNotShownIfProjectHasNotMarkers() throws Exception {
        when(problemMarker.isPresent()).thenReturn(false);

        projectConfigSynchronized.onSelectionChanged(null);

        verify(confirmDialog, never()).show();
    }

    @Test
    public void dialogIsNotShownIfNoProjectProblem() throws Exception {
        problems.clear();

        projectConfigSynchronized.onSelectionChanged(null);

        verify(confirmDialog, never()).show();
    }

    @Test
    public void dialogShouldBeShow() throws Exception {
        projectConfigSynchronized.onSelectionChanged(null);

        verify(confirmDialog).show();
    }

    @Test
    public void removeButtonIsClicked() throws Exception {
        String projectRemoved = "project removed";

        when(rootProject.delete()).thenReturn(deleteProjectPromise);
        when(deleteProjectPromise.then(Matchers.<Operation<Void>>any())).thenReturn(deleteProjectPromise);
        when(locale.projectRemoved(PROJECT_NAME)).thenReturn(projectRemoved);

        projectConfigSynchronized.onSelectionChanged(null);

        verify(dialogFactory).createConfirmDialog(eq(SYNCH_DIALOG_TITLE),
                                                  eq(SYNCH_DIALOG_CONTENT),
                                                  eq(IMPORT_BUTTON),
                                                  eq(REMOVE_BUTTON),
                                                  confirmCallbackArgumentCaptor.capture(),
                                                  cancelCallbackArgumentCaptor.capture());
        verify(confirmDialog).show();

        cancelCallbackArgumentCaptor.getValue().cancelled();
        verify(rootProject).delete();

        verify(deleteProjectPromise).then(projectDeleted.capture());
        projectDeleted.getValue().apply(null);
        verify(notificationManager).notify(projectRemoved, SUCCESS, EMERGE_MODE);
    }

    @Test
    public void importButtonIsClicked() throws Exception {
        projectConfigSynchronized.onSelectionChanged(null);

        verify(dialogFactory).createConfirmDialog(eq(SYNCH_DIALOG_TITLE),
                                                  eq(SYNCH_DIALOG_CONTENT),
                                                  eq(IMPORT_BUTTON),
                                                  eq(REMOVE_BUTTON),
                                                  confirmCallbackArgumentCaptor.capture(),
                                                  cancelCallbackArgumentCaptor.capture());
        verify(confirmDialog).show();

        confirmCallbackArgumentCaptor.getValue().accepted();
        verify(wsRoot).importProject();
        verify(projectRequest).withBody(rootProject);
        verify(projectRequest).send();
    }

    @Test
    public void changeLocationWindowShouldBeShown() throws Exception {
        String newLocation = "new/location";

        when(sourceStorage.getLocation()).thenReturn(null);
        when(changeLocationWidget.getText()).thenReturn(newLocation);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               Matchers.<IsWidget>any(),
                                               anyObject(),
                                               anyObject())).thenReturn(changeConfirmDialog);

        projectConfigSynchronized.onSelectionChanged(null);

        verify(dialogFactory).createConfirmDialog(eq(SYNCH_DIALOG_TITLE),
                                                  eq(SYNCH_DIALOG_CONTENT),
                                                  eq(IMPORT_BUTTON),
                                                  eq(REMOVE_BUTTON),
                                                  confirmCallbackArgumentCaptor.capture(),
                                                  cancelCallbackArgumentCaptor.capture());
        verify(confirmDialog).show();

        confirmCallbackArgumentCaptor.getValue().accepted();

        verify(dialogFactory).createConfirmDialog(eq(SYNCH_DIALOG_TITLE),
                                                  eq(changeLocationWidget),
                                                  confirmCallbackArgumentCaptor.capture(),
                                                  eq(null));
        verify(changeConfirmDialog).show();

        confirmCallbackArgumentCaptor.getValue().accepted();
        verify(sourceStorage).setLocation(newLocation);
        verify(sourceStorage).setType("github");

        verify(wsRoot).importProject();
        verify(projectRequest).withBody(rootProject);
        verify(projectRequest).send();
    }
}

