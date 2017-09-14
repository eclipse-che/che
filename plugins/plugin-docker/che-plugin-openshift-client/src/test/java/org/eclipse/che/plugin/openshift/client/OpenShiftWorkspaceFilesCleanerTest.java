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
package org.eclipse.che.plugin.openshift.client;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.event.ServerIdleEvent;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenShiftWorkspaceFilesCleanerTest {

  private static final String CHE_OPENSHIFT_PROJECT = "eclipse-che";
  private static final String WORKSPACES_PVC_NAME = "che-data-volume";
  private static final String WORKSPACE_ONE = "testworkspaceone";
  private static final String WORKSPACE_TWO = "testworkspacetwo";

  @Mock private OpenShiftPvcHelper pvcHelper;
  @Captor private ArgumentCaptor<EventSubscriber<ServerIdleEvent>> subscriberCaptor;

  private OpenShiftWorkspaceFilesCleaner cleaner;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
    cleaner =
        new OpenShiftWorkspaceFilesCleaner(pvcHelper, CHE_OPENSHIFT_PROJECT, WORKSPACES_PVC_NAME);
  }

  @Test
  public void shouldSubscribeToEventService() {
    //given
    EventService eventService = mock(EventService.class);

    //when
    cleaner.subscribe(eventService);

    //then
    verify(eventService).subscribe(cleaner);
    verify(eventService).subscribe(any(), eq(ServerIdleEvent.class));
  }

  @Test
  public void shouldDeleteWorkspaceInQueueOnServerIdleEvent() {
    //given
    OpenShiftWorkspaceFilesCleaner spyCleaner = spy(cleaner);
    doNothing().when(spyCleaner).deleteWorkspacesInQueue();

    EventService eventService = mock(EventService.class);
    spyCleaner.subscribe(eventService);
    verify(eventService).subscribe(subscriberCaptor.capture(), eq(ServerIdleEvent.class));
    EventSubscriber<ServerIdleEvent> subscriber = subscriberCaptor.getValue();

    //when
    subscriber.onEvent(new ServerIdleEvent(1000));

    //then
    verify(spyCleaner).deleteWorkspacesInQueue();
  }

  @Test
  public void shouldDoNothingWithoutIdleEvent() throws ServerException, IOException {
    // Given
    Workspace workspace = generateWorkspace(WORKSPACE_ONE);

    // When
    cleaner.onEvent(new WorkspaceRemovedEvent(workspace));

    // Then
    verify(pvcHelper, never())
        .createJobPod(
            anyString(),
            anyString(),
            anyString(),
            any(OpenShiftPvcHelper.Command.class),
            any(String[].class));
  }

  @Test
  public void shouldDeleteWorkspaceOnIdleEvent() throws ServerException, IOException {
    // Given
    Workspace workspace = generateWorkspace(WORKSPACE_ONE);

    // When
    cleaner.onEvent(new WorkspaceRemovedEvent(workspace));
    cleaner.deleteWorkspacesInQueue();

    // Then
    verify(pvcHelper, times(1))
        .createJobPod(
            anyString(),
            anyString(),
            anyString(),
            eq(OpenShiftPvcHelper.Command.REMOVE),
            eq(WORKSPACE_ONE));
  }

  @Test
  public void shouldDeleteMultipleQueuedWorkspacesAtOnce() throws ServerException, IOException {
    // Given
    Workspace workspaceOne = generateWorkspace(WORKSPACE_ONE);
    Workspace workspaceTwo = generateWorkspace(WORKSPACE_TWO);
    String[] expectedDirs = new String[] {WORKSPACE_ONE, WORKSPACE_TWO};
    ArgumentCaptor<String> dirCaptor = ArgumentCaptor.forClass(String.class);

    // When
    cleaner.onEvent(new WorkspaceRemovedEvent(workspaceOne));
    cleaner.onEvent(new WorkspaceRemovedEvent(workspaceTwo));
    cleaner.deleteWorkspacesInQueue();

    // Then
    verify(pvcHelper, times(1))
        .createJobPod(
            anyString(),
            anyString(),
            anyString(),
            eq(OpenShiftPvcHelper.Command.REMOVE),
            dirCaptor.capture(), // Varargs capture doesn't seem to work.
            dirCaptor.capture());

    List<String> dirs = dirCaptor.getAllValues();
    String[] actualDirs = dirs.toArray(new String[dirs.size()]);
    // Sort arrays to ignore order
    Arrays.sort(actualDirs);
    Arrays.sort(expectedDirs);
    assertEquals(actualDirs, expectedDirs, "Expected all dirs to be deleted when server is idled.");
  }

  @Test
  public void shouldRetainQueueIfDeletionFails() throws ServerException, IOException {
    // Given
    Workspace workspaceOne = generateWorkspace(WORKSPACE_ONE);
    when(pvcHelper.createJobPod(any(), any(), any(), any(), any())).thenReturn(false);

    // When
    cleaner.onEvent(new WorkspaceRemovedEvent(workspaceOne));
    cleaner.deleteWorkspacesInQueue();

    // Then
    verify(pvcHelper, times(1))
        .createJobPod(
            anyString(),
            anyString(),
            anyString(),
            eq(OpenShiftPvcHelper.Command.REMOVE),
            eq(WORKSPACE_ONE));

    // When
    cleaner.deleteWorkspacesInQueue();

    // Then
    verify(pvcHelper, times(2))
        .createJobPod(
            anyString(),
            anyString(),
            anyString(),
            eq(OpenShiftPvcHelper.Command.REMOVE),
            eq(WORKSPACE_ONE));
  }

  @Test
  public void shouldUseProjectNamespaceAndPvcNameAsParameters()
      throws ServerException, IOException {
    // Given
    Workspace workspaceOne = generateWorkspace(WORKSPACE_ONE);

    // When
    cleaner.onEvent(new WorkspaceRemovedEvent(workspaceOne));
    cleaner.deleteWorkspacesInQueue();

    // Then
    verify(pvcHelper, times(1))
        .createJobPod(
            eq(WORKSPACES_PVC_NAME),
            eq(CHE_OPENSHIFT_PROJECT),
            anyString(),
            eq(OpenShiftPvcHelper.Command.REMOVE),
            eq(WORKSPACE_ONE));
  }

  private Workspace generateWorkspace(String id) {
    WorkspaceConfigImpl config = new WorkspaceConfigImpl();
    config.setName(id);
    return new WorkspaceImpl(id, null, config);
  }
}
