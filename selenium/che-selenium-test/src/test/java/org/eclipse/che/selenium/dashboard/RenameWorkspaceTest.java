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
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class RenameWorkspaceTest {
  private static final String CHANGE_WORKSPACE_NAME = NameGenerator.generate("workspace_new", 4);

  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private TestWorkspace ws;
  @Inject private DefaultTestUser user;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = ws.getName();
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(CHANGE_WORKSPACE_NAME, user.getName());
  }

  @Test
  public void renameNameWorkspaceTest() {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.selectWorkspaceItemName(workspaceName);
    dashboardWorkspace.waitToolbarTitleName(workspaceName);
    dashboardWorkspace.selectTabInWorspaceMenu(DashboardWorkspace.TabNames.OVERVIEW);
    dashboardWorkspace.enterNameWorkspace(CHANGE_WORKSPACE_NAME);
    dashboardWorkspace.clickOnSaveBtn();
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
    dashboardWorkspace.checkNameWorkspace(CHANGE_WORKSPACE_NAME);
  }
}
