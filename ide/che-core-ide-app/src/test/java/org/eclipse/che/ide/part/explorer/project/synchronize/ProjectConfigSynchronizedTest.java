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
package org.eclipse.che.ide.part.explorer.project.synchronize;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

@RunWith(GwtMockitoTestRunner.class)
public class ProjectConfigSynchronizedTest {
  private static final String PROJECT_NAME = "project name";
  private static final String PROJECT_LOCATION = "/project/location";
  private static final String SYNCH_DIALOG_TITLE = "Synchronize dialog title";
  private static final String SYNCH_DIALOG_CONTENT = "Synchronize dialog content";
  private static final String IMPORT_BUTTON = "Import";
  private static final String REMOVE_BUTTON = "Remove";

  @Mock private AppContext appContext;
  @Mock private ProjectExplorerPresenter projectExplorerPresenter;
  @Mock private DialogFactory dialogFactory;
  @Mock private CoreLocalizationConstant locale;
  @Mock private NotificationManager notificationManager;
  @Mock private ChangeLocationWidget changeLocationWidget;

  @Mock private Optional<Marker> problemMarker;
  @Mock private Project.ProblemProjectMarker problemProjectMarker;
  @Mock private ConfirmDialog confirmDialog;
  @Mock private ConfirmDialog changeConfirmDialog;
  @Mock private Promise<Void> deleteProjectPromise;
  @Mock private Promise<Project> projectPromise;
  @Mock private SourceStorageDto sourceStorage;
  @Mock private BeforeLoadEvent beforeLoadEvent;
  @Mock private Container wsRoot;
  @Mock private Project.ProjectRequest projectRequest;
  @Mock private Tree tree;
  @Mock private NodeLoader nodeLoader;
  @Mock private ResourceNode requestedNode;
  @Mock private Project resource;

  @Captor private ArgumentCaptor<ConfirmCallback> confirmCallbackArgumentCaptor;
  @Captor private ArgumentCaptor<CancelCallback> cancelCallbackArgumentCaptor;
  @Captor private ArgumentCaptor<Operation<Void>> projectDeleted;
  @Captor private ArgumentCaptor<BeforeLoadEvent.BeforeLoadHandler> beforeLoadHandlerArgumentCaptor;

  private Map<Integer, String> problems;

  @Before
  public void setUp() {
    when(resource.getMarker(Project.ProblemProjectMarker.PROBLEM_PROJECT))
        .thenReturn(problemMarker);
    when(resource.getName()).thenReturn(PROJECT_NAME);
    when(problemMarker.isPresent()).thenReturn(true);
    when(problemMarker.get()).thenReturn(problemProjectMarker);
    when(projectExplorerPresenter.getTree()).thenReturn(tree);
    when(tree.getNodeLoader()).thenReturn(nodeLoader);
    when(beforeLoadEvent.getRequestedNode()).thenReturn(requestedNode);
    when(requestedNode.getData()).thenReturn(resource);
    when(resource.isProject()).thenReturn(true);

    problems = new HashMap<>();
    problems.put(10, "Error project");
    when(problemProjectMarker.getProblems()).thenReturn(problems);

    when(locale.synchronizeDialogTitle()).thenReturn(SYNCH_DIALOG_TITLE);
    when(locale.existInWorkspaceDialogContent(PROJECT_NAME)).thenReturn(SYNCH_DIALOG_CONTENT);
    when(locale.buttonImport()).thenReturn(IMPORT_BUTTON);
    when(locale.buttonRemove()).thenReturn(REMOVE_BUTTON);
    when(dialogFactory.createConfirmDialog(
            anyString(), anyString(), anyString(), anyString(), anyObject(), anyObject()))
        .thenReturn(confirmDialog);

    when(resource.getSource()).thenReturn(sourceStorage);
    when(sourceStorage.getLocation()).thenReturn(PROJECT_LOCATION);
    when(appContext.getWorkspaceRoot()).thenReturn(wsRoot);
    when(wsRoot.importProject()).thenReturn(projectRequest);
    when(projectRequest.withBody(anyObject())).thenReturn(projectRequest);
    when(projectRequest.send()).thenReturn(projectPromise);

    ProjectConfigSynchronized projectConfigSynchronized =
        new ProjectConfigSynchronized(
            appContext,
            projectExplorerPresenter,
            dialogFactory,
            locale,
            notificationManager,
            changeLocationWidget);
  }

  private void subscribeToOnBeforeLoadNodeEvent() throws Exception {
    verify(nodeLoader).addBeforeLoadHandler(beforeLoadHandlerArgumentCaptor.capture());
    beforeLoadHandlerArgumentCaptor.getValue().onBeforeLoad(beforeLoadEvent);
  }

  @Test
  public void dialogIsNotShownIfProjectHasNotMarkers() throws Exception {
    when(problemMarker.isPresent()).thenReturn(false);

    subscribeToOnBeforeLoadNodeEvent();

    verify(confirmDialog, never()).show();
  }

  @Test
  public void dialogIsNotShownIfNoProjectProblem() throws Exception {
    problems.clear();

    subscribeToOnBeforeLoadNodeEvent();

    verify(confirmDialog, never()).show();
  }

  @Test
  public void removeButtonIsClicked() throws Exception {
    String projectRemoved = "project removed";

    when(resource.delete()).thenReturn(deleteProjectPromise);
    when(deleteProjectPromise.then(org.mockito.ArgumentMatchers.<Operation<Void>>any()))
        .thenReturn(deleteProjectPromise);
    when(locale.projectRemoved(PROJECT_NAME)).thenReturn(projectRemoved);

    subscribeToOnBeforeLoadNodeEvent();

    verify(dialogFactory)
        .createConfirmDialog(
            eq(SYNCH_DIALOG_TITLE),
            eq(SYNCH_DIALOG_CONTENT),
            eq(IMPORT_BUTTON),
            eq(REMOVE_BUTTON),
            confirmCallbackArgumentCaptor.capture(),
            cancelCallbackArgumentCaptor.capture());
    verify(confirmDialog).show();

    cancelCallbackArgumentCaptor.getValue().cancelled();
    verify(resource).delete();

    verify(deleteProjectPromise).then(projectDeleted.capture());
    projectDeleted.getValue().apply(null);
    verify(notificationManager).notify(projectRemoved, SUCCESS, EMERGE_MODE);
  }

  @Test
  public void importButtonIsClicked() throws Exception {
    subscribeToOnBeforeLoadNodeEvent();

    verify(dialogFactory)
        .createConfirmDialog(
            eq(SYNCH_DIALOG_TITLE),
            eq(SYNCH_DIALOG_CONTENT),
            eq(IMPORT_BUTTON),
            eq(REMOVE_BUTTON),
            confirmCallbackArgumentCaptor.capture(),
            cancelCallbackArgumentCaptor.capture());
    verify(confirmDialog).show();

    confirmCallbackArgumentCaptor.getValue().accepted();
    verify(wsRoot).importProject();
    verify(projectRequest).withBody(resource);
    verify(projectRequest).send();
  }

  @Test
  public void changeLocationWindowShouldBeShown() throws Exception {
    String newLocation = "new/location";

    when(sourceStorage.getLocation()).thenReturn(null);
    when(changeLocationWidget.getText()).thenReturn(newLocation);
    when(dialogFactory.createConfirmDialog(
            anyString(), org.mockito.ArgumentMatchers.<IsWidget>any(), anyObject(), anyObject()))
        .thenReturn(changeConfirmDialog);

    subscribeToOnBeforeLoadNodeEvent();

    verify(dialogFactory)
        .createConfirmDialog(
            eq(SYNCH_DIALOG_TITLE),
            eq(SYNCH_DIALOG_CONTENT),
            eq(IMPORT_BUTTON),
            eq(REMOVE_BUTTON),
            confirmCallbackArgumentCaptor.capture(),
            cancelCallbackArgumentCaptor.capture());
    verify(confirmDialog).show();

    confirmCallbackArgumentCaptor.getValue().accepted();

    verify(dialogFactory)
        .createConfirmDialog(
            eq(SYNCH_DIALOG_TITLE),
            eq(changeLocationWidget),
            confirmCallbackArgumentCaptor.capture(),
            eq(null));
    verify(changeConfirmDialog).show();

    confirmCallbackArgumentCaptor.getValue().accepted();
    verify(sourceStorage).setLocation(newLocation);
    verify(sourceStorage).setType("github");

    verify(wsRoot).importProject();
    verify(projectRequest).withBody(resource);
    verify(projectRequest).send();
  }
}
