/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.OVERVIEW;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class DeleteStoppingWorkspaceTest {

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private TestWorkspace ws;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceOverview workspaceOverview;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = ws.getName();
    dashboard.open();
  }

  @Test
  public void deleteStoppingWorkspaceTest() {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(RUNNING);
    workspaceDetails.clickOnStopWorkspace();
    workspaceDetails.checkStateOfWorkspace(STOPPED);
    workspaceOverview.clickOnDeleteWorkspace();
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaces.waitWorkspaceIsNotPresent(workspaceName);
    dashboard.waitToolbarTitleName("Workspaces");
  }
}
