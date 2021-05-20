/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.Pages.DEFAULT_PAGE_SIZE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@Listeners(MockitoTestNGListener.class)
public class TemporaryWorkspaceRemoverTest {

  private static final int COUNT_OF_WORKSPACES = 150;

  @Mock private WorkspaceDao workspaceDao;
  @Mock private WorkspaceRuntimes runtimes;

  @InjectMocks private TemporaryWorkspaceRemover remover;

  @Test
  public void shouldRemoveStoppedTemporaryWorkspaces() throws Exception {
    doReturn(WorkspaceStatus.STOPPED).when(runtimes).getStatus(any());
    when(workspaceDao.getWorkspaces(eq(true), anyInt(), anyLong()))
        .thenReturn(
            new Page<>(
                createStoppedWorkspaces(DEFAULT_PAGE_SIZE),
                0,
                DEFAULT_PAGE_SIZE,
                COUNT_OF_WORKSPACES))
        .thenReturn(
            new Page<>(
                createStoppedWorkspaces(DEFAULT_PAGE_SIZE),
                DEFAULT_PAGE_SIZE,
                DEFAULT_PAGE_SIZE,
                COUNT_OF_WORKSPACES))
        .thenReturn(
            new Page<>(
                createStoppedWorkspaces(DEFAULT_PAGE_SIZE),
                DEFAULT_PAGE_SIZE * 2,
                DEFAULT_PAGE_SIZE,
                COUNT_OF_WORKSPACES));
    remover.removeTemporaryWs();

    verify(workspaceDao, times(COUNT_OF_WORKSPACES)).remove(anyString());
  }

  @Test(dataProvider = "activeWorkspaceStatuses")
  public void shouldNotRemoveActiveWorkspace(WorkspaceStatus status) throws Exception {
    WorkspaceImpl workspace = WorkspaceImpl.builder().setId("ws123").build();
    when(workspaceDao.getWorkspaces(eq(true), anyInt(), anyLong()))
        .thenReturn(new Page<>(singletonList(workspace), 0, 1, 1));
    doReturn(status).when(runtimes).getStatus("ws123");

    remover.removeTemporaryWs();

    verify(workspaceDao, never()).remove(anyString());
  }

  @DataProvider
  public Object[][] activeWorkspaceStatuses() {
    return new Object[][] {
      {WorkspaceStatus.STOPPING}, {WorkspaceStatus.RUNNING}, {WorkspaceStatus.STARTING}
    };
  }

  private List<WorkspaceImpl> createStoppedWorkspaces(int number) {
    List<WorkspaceImpl> wsList = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      String wsId = "id" + i;
      wsList.add(WorkspaceImpl.builder().setId(wsId).build());
    }
    return wsList;
  }
}
