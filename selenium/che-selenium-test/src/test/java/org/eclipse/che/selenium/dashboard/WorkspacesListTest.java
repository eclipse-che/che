/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Statuses;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WorkspacesListTest {

  private String workspaceName1 = NameGenerator.generate("wksp-", 5);
  private String workspaceName2 = NameGenerator.generate("wksp-", 5);
  private String workspaceName3 = NameGenerator.generate("wksp-", 5);

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceConfig workspaceConfig;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();

    createWorkspace(workspaceName1);
    createWorkspace(workspaceName2);
    createWorkspace(workspaceName3);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(workspaceName1, defaultTestUser.getName());
    workspaceServiceClient.delete(workspaceName2, defaultTestUser.getName());
    workspaceServiceClient.delete(workspaceName3, defaultTestUser.getName());
  }

  @BeforeMethod
  public void openWorkspacesPage() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
  }

  @Test
  public void checkWorkspacesList() {
    // check UI views of workspaces list
    workspaces.waitToolbarTitleName();
    workspaces.waitDocumentationLink();
    workspaces.waitAddWorkspaceButton();
    workspaces.waitSearchWorkspaceByNameField();

    try {
      workspaces.waitBulkCheckbox();
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8594");
    }

    // check all headers are present
    ArrayList<String> headers = workspaces.getWorkspaceListHeaders();
    assertTrue(headers.contains("NAME"));
    assertTrue(headers.contains("RAM"));
    assertTrue(headers.contains("PROJECTS"));
    assertTrue(headers.contains("STACK"));
    assertTrue(headers.contains("ACTIONS"));

    // check workspaces info
    Assert.assertEquals(workspaces.getWorkspaceRamValue(workspaceName1), "2048 MB");
    Assert.assertEquals(workspaces.getWorkspaceStackName(workspaceName2), "java-default");
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(workspaceName3), "1");
  }

  @Test
  public void checkWorkspaceSelectingByCheckbox() {
    // select the test workspace by checkbox and select it is checked
    try {
      workspaces.selectWorkspaceByCheckbox(workspaceName1);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8594");
    }

    assertTrue(workspaces.isWorkspaceChecked(workspaceName1));
    workspaces.selectWorkspaceByCheckbox(workspaceName1);
    Assert.assertFalse(workspaces.isWorkspaceChecked(workspaceName1));

    // click on the Bulk button and check that all workspaces are checked
    workspaces.selectAllWorkspacesByBulk();
    assertTrue(workspaces.isWorkspaceChecked(workspaceName1));
    assertTrue(workspaces.isWorkspaceChecked(workspaceName2));
    assertTrue(workspaces.isWorkspaceChecked(workspaceName3));

    workspaces.selectAllWorkspacesByBulk();
    Assert.assertFalse(workspaces.isWorkspaceChecked(workspaceName1));
  }

  @Test
  public void checkWorkspaceActions() {
    // open the Config page of the test workspace
    try {
      workspaces.clickOnWorkspaceConfigureButton(workspaceName1);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8594");
    }

    workspaceDetails.waitToolbarTitleName(workspaceName1);
    workspaceConfig.waitConfigForm();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(workspaceName1);

    // open the Projects page of the test workspace
    workspaces.clickOnWorkspaceAddProjectButton(workspaceName2);
    workspaceDetails.waitToolbarTitleName(workspaceName2);
    workspaceProjects.waitProjectIsPresent(WEB_JAVA_SPRING);
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(workspaceName1);

    // check statuses of the created workspaces
    workspaces.waitWorkspaceStatus(workspaceName1, Statuses.STOPPED);
    workspaces.waitWorkspaceStatus(workspaceName2, Statuses.STOPPED);
    workspaces.waitWorkspaceStatus(workspaceName3, Statuses.STOPPED);

    // stop the workspace by the Actions button and check its status is RUNNING
    Assert.assertEquals(workspaces.getWorkspaceStatus(workspaceName3), Statuses.STOPPED);
    workspaces.clickOnWorkspaceActionsButton(workspaceName3);
    workspaces.waitWorkspaceStatus(workspaceName3, Statuses.RUNNING);
  }

  @Test
  public void checkWorkspaceFiltering() {
    workspaces.waitSearchWorkspaceByNameField();

    workspaces.typeToSearchInput("*");
    workspaces.waitNoWorkspacesFound();

    workspaces.typeToSearchInput("wksp");
    try {
      workspaces.waitWorkspaceIsPresent(workspaceName1);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8594");
    }

    workspaces.waitWorkspaceIsPresent(workspaceName2);
    workspaces.waitWorkspaceIsPresent(workspaceName3);

    // search a workspace by a full name
    workspaces.typeToSearchInput(workspaceName1);
    workspaces.waitWorkspaceIsPresent(workspaceName1);

    // search a workspace by a part name
    workspaces.typeToSearchInput(workspaceName3.substring(workspaceName3.length() / 2));
    workspaces.waitWorkspaceIsPresent(workspaceName3);
  }

  @Test(priority = 1)
  public void checkWorkspaceDeleting() {
    // delete all created test workspaces
    try {
      workspaces.selectWorkspaceByCheckbox(workspaceName1);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8594");
    }

    workspaces.selectWorkspaceByCheckbox(workspaceName2);
    workspaces.selectWorkspaceByCheckbox(workspaceName3);

    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();

    workspaces.waitWorkspaceIsNotPresent(workspaceName1);
    workspaces.waitWorkspaceIsNotPresent(workspaceName2);
    workspaces.waitWorkspaceIsNotPresent(workspaceName3);
  }

  private void createWorkspace(String name) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.selectStack(JAVA.getId());
    newWorkspace.typeWorkspaceName(name);
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(WEB_JAVA_SPRING);
    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndEditWorkspace();

    workspaceDetails.waitToolbarTitleName(name);
    workspaceDetails.checkStateOfWorkspace(STOPPED);
  }
}
