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
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.core.Pages.DEFAULT_PAGE_SIZE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@Listeners(MockitoTestNGListener.class)
public class TemporaryWorkspaceRemoverTest {

  static final int COUNT_OF_WORKSPACES = 150;

  @Mock private WorkspaceDao workspaceDao;

  @InjectMocks private TemporaryWorkspaceRemover remover;

  @Test
  public void shouldRemoveTemporaryWorkspaces() throws Exception {
    when(workspaceDao.getWorkspaces(eq(true), anyInt(), anyLong()))
        .thenReturn(
            new Page<>(
                createEntities(DEFAULT_PAGE_SIZE), 0, DEFAULT_PAGE_SIZE, COUNT_OF_WORKSPACES))
        .thenReturn(
            new Page<>(
                createEntities(DEFAULT_PAGE_SIZE),
                DEFAULT_PAGE_SIZE,
                DEFAULT_PAGE_SIZE,
                COUNT_OF_WORKSPACES))
        .thenReturn(
            new Page<>(
                createEntities(DEFAULT_PAGE_SIZE),
                DEFAULT_PAGE_SIZE * 2,
                DEFAULT_PAGE_SIZE,
                COUNT_OF_WORKSPACES));
    remover.removeTemporaryWs();

    verify(workspaceDao, times(COUNT_OF_WORKSPACES)).remove(anyString());
  }

  private List<WorkspaceImpl> createEntities(int number) {
    List<WorkspaceImpl> wsList = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      wsList.add(new WorkspaceImpl("id" + i, null, null));
    }
    return wsList;
  }
}
