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
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.OVERVIEW;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class DeleteRunningWorkspaceTest {

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = NameGenerator.generate("workspace", 5);
    dashboard.open();
    createWorkspaceHelper.createAndEditWorkspaceFromStack(
        Devfile.JAVA_MAVEN, workspaceName, Collections.emptyList(), null);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(workspaceName, defaultTestUser.getName());
  }

  @Test
  public void deleteRunningWorkspaceTest() {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.clickOnWorkspaceStopStartButton(workspaceName);
    workspaces.waitWorkspaceStatus(workspaceName, Status.RUNNING);

    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(RUNNING);
    workspaceOverview.clickOnDeleteWorkspace();
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.waitWorkspaceIsNotPresent(workspaceName);
  }
}
