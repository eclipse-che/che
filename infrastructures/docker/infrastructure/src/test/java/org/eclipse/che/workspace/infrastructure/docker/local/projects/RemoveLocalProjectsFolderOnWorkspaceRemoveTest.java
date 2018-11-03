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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link RemoveLocalProjectsFolderOnWorkspaceRemove}.
 *
 * @author Alexander Andrienko
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveLocalProjectsFolderOnWorkspaceRemoveTest {

  private static final String WORKSPACE_NAME = "test-workspace";
  private static final String WORKSPACE_NAMESPACE = "che";
  private static final String WORKSPACE_ID = "workspace123";

  private static final String HOST_INSTANCE_DATA_FOLDER = "/home";
  private static final String CONTAINER_MOUNTED_PROJECTS_FOLDER = "/data/projects/ws123";
  private static final String HOST_PROJECTS_FOLDER =
      HOST_INSTANCE_DATA_FOLDER + CONTAINER_MOUNTED_PROJECTS_FOLDER;

  @Mock private LocalProjectsFolderPathProvider projectsFolderPathProvider;
  @Mock private Workspace workspace;

  private RemoveLocalProjectsFolderOnWorkspaceRemove removeLocalProjectsFolderOnWorkspaceRemove;

  @BeforeMethod
  public void setUp() {
    removeLocalProjectsFolderOnWorkspaceRemove =
        spy(new RemoveLocalProjectsFolderOnWorkspaceRemove(projectsFolderPathProvider));

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspaceConfig.getName()).thenReturn(WORKSPACE_NAME);

    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);
    lenient().when(workspace.getNamespace()).thenReturn(WORKSPACE_NAMESPACE);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);
  }

  @Test
  public void shouldSubscribeListenerToEventService() {
    EventService eventService = mock(EventService.class);

    removeLocalProjectsFolderOnWorkspaceRemove.subscribe(eventService);

    verify(eventService).subscribe(removeLocalProjectsFolderOnWorkspaceRemove);
  }

  @Test
  public void hostInstanceDataFolderShouldBeCutAndProjectsFolderShouldBeCleaned() throws Exception {
    when(projectsFolderPathProvider.getPath(anyString())).thenReturn(HOST_PROJECTS_FOLDER);
    when(removeLocalProjectsFolderOnWorkspaceRemove.getInstanceDataPath())
        .thenReturn(HOST_INSTANCE_DATA_FOLDER);

    removeLocalProjectsFolderOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(projectsFolderPathProvider).getPath(WORKSPACE_ID);
    verify(removeLocalProjectsFolderOnWorkspaceRemove)
        .deleteRecursiveAsync(WORKSPACE_ID, CONTAINER_MOUNTED_PROJECTS_FOLDER);
  }

  @Test
  public void workspaceShouldNotBeCleanedIfHostProjectsRootFolderIsConfigured() throws Exception {
    removeLocalProjectsFolderOnWorkspaceRemove =
        spy(
            new RemoveLocalProjectsFolderOnWorkspaceRemove(
                HOST_PROJECTS_FOLDER, projectsFolderPathProvider));

    when(projectsFolderPathProvider.getPath(anyString())).thenReturn(HOST_PROJECTS_FOLDER);
    when(removeLocalProjectsFolderOnWorkspaceRemove.getInstanceDataPath())
        .thenReturn(HOST_INSTANCE_DATA_FOLDER);

    removeLocalProjectsFolderOnWorkspaceRemove.onEvent(new WorkspaceRemovedEvent(workspace));

    verify(projectsFolderPathProvider).getPath(WORKSPACE_ID);
    verify(removeLocalProjectsFolderOnWorkspaceRemove, never())
        .deleteRecursiveAsync(anyString(), anyString());
  }
}
