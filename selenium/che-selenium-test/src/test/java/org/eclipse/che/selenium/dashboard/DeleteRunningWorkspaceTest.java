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
package org.eclipse.che.selenium.dashboard;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class DeleteRunningWorkspaceTest {

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private TestWorkspace ws;
  @Inject private Loader loader;
  @Inject private TestUser user;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = ws.getName();
    dashboard.open();
  }

  @Test
  public void deleteRunningWorkspaceTest() {
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaces.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorspaceMenu(WorkspaceDetails.TabNames.OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(WorkspaceDetails.StateWorkspace.RUNNING);
    workspaceDetails.clickOnDeleteWorkspace();
    workspaceDetails.clickOnDeleteItInDialogWindow();
    workspaceDetails.checkStateOfWorkspace(WorkspaceDetails.StateWorkspace.STOPPING);
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.waitWorkspaceIsNotPresent(workspaceName);
  }
}
