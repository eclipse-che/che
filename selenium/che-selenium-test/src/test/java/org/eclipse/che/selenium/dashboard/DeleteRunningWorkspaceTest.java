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
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class DeleteRunningWorkspaceTest {
  private static final Logger LOG = LoggerFactory.getLogger(DeleteRunningWorkspaceTest.class);

  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private TestWorkspace ws;
  @Inject private Loader loader;
  @Inject private DefaultTestUser user;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = ws.getName();
    dashboard.open();
  }

  @Test
  public void deleteRunningWorkspaceTest() {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboardWorkspace.selectWorkspaceItemName(workspaceName);
    dashboardWorkspace.waitToolbarTitleName(workspaceName);
    dashboardWorkspace.selectTabInWorspaceMenu(DashboardWorkspace.TabNames.SETTINGS);
    dashboardWorkspace.checkStateOfWorkspace(DashboardWorkspace.StateWorkspace.RUNNING);
    dashboardWorkspace.clickOnDeleteWorkspace();
    dashboardWorkspace.clickOnDeleteItInDialogWindow();
    dashboardWorkspace.checkStateOfWorkspace(DashboardWorkspace.StateWorkspace.STOPPING);
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.waitWorkspaceIsNotPresent(workspaceName);
  }
}
