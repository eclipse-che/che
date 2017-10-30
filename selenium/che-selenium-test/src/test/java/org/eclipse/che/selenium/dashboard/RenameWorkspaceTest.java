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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace.StateWorkspace.STARTING;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace.StateWorkspace.STOPPING;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class RenameWorkspaceTest {
  private static final int MIN_WORKSPACE_NAME_SIZE = 3;
  private static final int MAX_WORKSPACE_NAME_SIZE = 100;
  private static final String MIN_WORKSPACE_NAME = generate("", MIN_WORKSPACE_NAME_SIZE);
  private static final String MAX_WORKSPACE_NAME = generate("", MAX_WORKSPACE_NAME_SIZE);
  private static final String WS_NAME_TOO_SHORT =
      ("The name has to be more than 3 characters long.");
  private static final String WS_NAME_TOO_LONG =
      ("The name has to be less than 101 characters long.");

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
    workspaceServiceClient.delete(workspaceName, user.getName());
    workspaceServiceClient.delete(MIN_WORKSPACE_NAME, user.getName());
    workspaceServiceClient.delete(MAX_WORKSPACE_NAME, user.getName());
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
    assertTrue(dashboardWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    dashboardWorkspace.clickOnCancelBtn();
    dashboardWorkspace.checkNameWorkspace(workspaceName);

    // type name with 101 characters and check error message that this name is too long
    dashboardWorkspace.enterNameWorkspace(MAX_WORKSPACE_NAME + "a");
    assertTrue(dashboardWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    dashboardWorkspace.clickOnCancelBtn();
    dashboardWorkspace.checkNameWorkspace(workspaceName);

    // type a name with min possible size and check that the workspace renamed
    renameWorkspace(MIN_WORKSPACE_NAME);

    // type a name with max possible size and check that the workspace renamed
    renameWorkspace(MAX_WORKSPACE_NAME);
  }

  private void renameWorkspace(String name) {
    dashboardWorkspace.enterNameWorkspace(name);
    assertFalse(dashboardWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    assertFalse(dashboardWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    saveAndWaitWorkspaceRestarted();
    dashboardWorkspace.checkNameWorkspace(name);
  }

  private void saveAndWaitWorkspaceRestarted() {
    dashboardWorkspace.clickOnSaveBtn();
    dashboardWorkspace.checkStateOfWorkspace(STOPPING);
    dashboardWorkspace.checkStateOfWorkspace(STARTING);
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }
}
