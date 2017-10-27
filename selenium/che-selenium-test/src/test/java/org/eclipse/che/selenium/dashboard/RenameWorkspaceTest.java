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
import java.io.IOException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace.StateWorkspace;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class RenameWorkspaceTest {
  private static final String MIN_WORKSPACE_NAME = NameGenerator.generate("", 3);
  private static final String MAX_WORKSPACE_NAME = NameGenerator.generate("workspace_new", 87);

  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private TestWorkspace ws;
  @Inject private TestUser user;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = ws.getName();
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(ws.getName(), user.getName());
  }

  @Test
  public void renameNameWorkspaceTest() throws IOException {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.selectWorkspaceItemName(workspaceName);
    dashboardWorkspace.waitToolbarTitleName(workspaceName);
    dashboardWorkspace.selectTabInWorspaceMenu(DashboardWorkspace.TabNames.OVERVIEW);

    // type name with 1 characters and check error message that this name is too short
    dashboardWorkspace.enterNameWorkspace("w");
    Assert.assertTrue(dashboardWorkspace.isWorkspaceNameTooShort());
    dashboardWorkspace.clickOnCancelBtn();
    dashboardWorkspace.checkNameWorkspace(workspaceName);

    // type name with 101 characters and check error message that this name is too long
    dashboardWorkspace.enterNameWorkspace(MAX_WORKSPACE_NAME + "1");
    Assert.assertTrue(dashboardWorkspace.isWorkspaceNameTooLong());
    dashboardWorkspace.clickOnCancelBtn();
    dashboardWorkspace.checkNameWorkspace(workspaceName);

    // type a name with min possible size and check that the workspace renamed
    dashboardWorkspace.enterNameWorkspace(MIN_WORKSPACE_NAME);
    Assert.assertFalse(dashboardWorkspace.isWorkspaceNameTooShort());
    clickOnSaveButton();
    dashboardWorkspace.checkNameWorkspace(MIN_WORKSPACE_NAME);

    // type a name with max possible size and check that the workspace renamed
    dashboardWorkspace.enterNameWorkspace(MAX_WORKSPACE_NAME);
    Assert.assertFalse(dashboardWorkspace.isWorkspaceNameTooLong());
    clickOnSaveButton();
    dashboardWorkspace.checkNameWorkspace(MAX_WORKSPACE_NAME);
  }

  private void clickOnSaveButton() {
    dashboardWorkspace.clickOnSaveBtn();
    dashboardWorkspace.checkStateOfWorkspace(StateWorkspace.STOPPING);
    dashboardWorkspace.checkStateOfWorkspace(StateWorkspace.STARTING);
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }
}
