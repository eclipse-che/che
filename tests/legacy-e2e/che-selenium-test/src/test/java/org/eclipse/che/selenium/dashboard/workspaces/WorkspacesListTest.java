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
package org.eclipse.che.selenium.dashboard.workspaces;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.WORKSPACE_ITEM_ADD_PROJECT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.WORKSPACE_ITEM_CONFIGURE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.WORKSPACE_ITEM_STOP_START_WORKSPACE_BUTTON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Sergey Skorik
 * @author Ihor Okhrimenko
 */
@Test
public class WorkspacesListTest {
  private static final String EXPECTED_JAVA_PROJECT_NAME = "console-java-simple";
  private static final String NEWEST_CREATED_WORKSPACE_NAME = "just-created-workspace";
  private static final int EXPECTED_SEARCHED_WORKSPACES_COUNT = 1;

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceConfig workspaceConfig;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;

  private String workspaceName;
  private String workspaceName1;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
    workspaceName = createWorkspaceHelper.createAndStartWorkspace(Devfile.JAVA_MAVEN);
    dashboard.open();
    workspaceName1 = createWorkspaceHelper.createAndStartWorkspace(Devfile.JAVA_MAVEN);
  }

  @BeforeMethod
  public void prepareToTestMethod() {
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(workspaceName, defaultTestUser.getName());
    workspaceServiceClient.delete(workspaceName1, defaultTestUser.getName());
    workspaceServiceClient.delete(NEWEST_CREATED_WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void shouldDisplayElements() throws Exception {
    workspaces.waitPageLoading();
    dashboard.waitWorkspacesCountInWorkspacesItem(testWorkspaceServiceClient.getWorkspacesCount());

    workspaces.waitWorkspaceIsPresent(workspaceName);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(workspaceName), "1");

    workspaces.waitWorkspaceIsPresent(workspaceName1);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(workspaceName), "1");
  }

  @Test
  public void checkWorkspaceSelectingByCheckbox() {
    String blankWorkspaceName = workspaceName;
    String javaWorkspaceName = workspaceName1;

    workspaces.waitPageLoading();

    // select all by bulk
    workspaces.selectAllWorkspacesByBulk();
    workspaces.waitWorkspaceCheckboxEnabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxEnabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxEnabled();
    workspaces.waitDeleteWorkspaceBtn();

    // unselect all by bulk
    workspaces.selectAllWorkspacesByBulk();
    workspaces.waitWorkspaceCheckboxDisabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxDisabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxDisabled();
    workspaces.waitDeleteWorkspaceBtnDisappearance();

    // select all by bulk
    workspaces.selectAllWorkspacesByBulk();
    workspaces.waitWorkspaceCheckboxEnabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxEnabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxEnabled();
    workspaces.waitDeleteWorkspaceBtn();

    // unselect one checkbox
    workspaces.selectWorkspaceByCheckbox(blankWorkspaceName);
    workspaces.waitWorkspaceCheckboxEnabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxDisabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxDisabled();
    workspaces.waitDeleteWorkspaceBtn();

    // unselect all checkboxes
    workspaces.selectWorkspaceByCheckbox(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxDisabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxDisabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxDisabled();

    // for avoid of failing in the multi-thread mode when unexpected workspaces can appear in the
    // workspaces list
    workspaces.clickOnUnexpectedWorkspacesCheckboxes(asList(blankWorkspaceName, javaWorkspaceName));

    workspaces.waitDeleteWorkspaceBtnDisappearance();

    // select one checkbox
    workspaces.selectWorkspaceByCheckbox(blankWorkspaceName);
    workspaces.waitWorkspaceCheckboxDisabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxEnabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxDisabled();
    workspaces.waitDeleteWorkspaceBtn();

    // select all checkboxes
    workspaces.selectWorkspaceByCheckbox(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxEnabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxEnabled(blankWorkspaceName);

    // for avoid of failing in the multi-thread mode
    workspaces.clickOnUnexpectedWorkspacesCheckboxes(asList(blankWorkspaceName, javaWorkspaceName));

    workspaces.waitBulkCheckboxEnabled();
    workspaces.waitDeleteWorkspaceBtn();

    // unselect all by bulk
    workspaces.selectAllWorkspacesByBulk();
    workspaces.waitWorkspaceCheckboxDisabled(javaWorkspaceName);
    workspaces.waitWorkspaceCheckboxDisabled(blankWorkspaceName);
    workspaces.waitBulkCheckboxDisabled();
    workspaces.waitDeleteWorkspaceBtnDisappearance();
  }

  @Test
  public void checkSearchField() throws Exception {
    int nameLength = workspaceName.length();
    int existingWorkspacesCount = testWorkspaceServiceClient.getWorkspacesCount();
    String sequenceForSearch = workspaceName.substring(nameLength - 5, nameLength);

    workspaces.waitVisibleWorkspacesCount(existingWorkspacesCount);
    workspaces.typeToSearchInput(sequenceForSearch);

    try {
      workspaces.waitVisibleWorkspacesCount(EXPECTED_SEARCHED_WORKSPACES_COUNT);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/13950");
    }

    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getWorkspaceName(), workspaceName);

    // check displaying list size
    workspaces.typeToSearchInput("");
    workspaces.waitVisibleWorkspacesCount(testWorkspaceServiceClient.getWorkspacesCount());

    workspaces.waitWorkspaceIsPresent(workspaceName);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(workspaceName), "1");
    workspaces.waitWorkspaceIsPresent(workspaceName1);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(workspaceName), "1");
  }

  @Test
  public void checkWorkspaceActions() throws Exception {
    workspaces.waitPageLoading();

    // go to workspace details by clicking on item in workspaces list
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitCustomWorkspacesTab();

    seleniumWebDriver.navigate().back();

    workspaces.waitPageLoading();

    workspaces.clickOnWorkspaceListItem(defaultTestUser.getName(), workspaceName);

    workspaceOverview.checkNameWorkspace(workspaceName);

    seleniumWebDriver.navigate().back();

    // check "Add project" button
    workspaces.waitPageLoading();
    workspaces.clickOnWorkspaceActionsButton(workspaceName1, WORKSPACE_ITEM_ADD_PROJECT_BUTTON);
    workspaceProjects.waitProjectIsPresent(EXPECTED_JAVA_PROJECT_NAME);

    seleniumWebDriver.navigate().back();

    // check "Workspace configuration" button
    workspaces.waitPageLoading();
    workspaces.clickOnWorkspaceActionsButton(workspaceName1, WORKSPACE_ITEM_CONFIGURE_BUTTON);
    workspaceConfig.waitConfigForm();

    seleniumWebDriver.navigate().back();

    // check stop/start button
    workspaces.waitPageLoading();
    workspaces.clickOnWorkspaceActionsButton(
        workspaceName1, WORKSPACE_ITEM_STOP_START_WORKSPACE_BUTTON);
    workspaces.waitWorkspaceStatus(workspaceName1, Status.STOPPED);
    workspaces.clickOnWorkspaceActionsButton(
        workspaceName1, WORKSPACE_ITEM_STOP_START_WORKSPACE_BUTTON);
    workspaces.waitWorkspaceStatus(workspaceName1, Status.RUNNING);

    workspaces.waitVisibleWorkspacesCount(testWorkspaceServiceClient.getWorkspacesCount());
  }

  @Test(priority = 1)
  public void deleteWorkspacesByCheckboxes() {
    workspaces.waitPageLoading();

    workspaces.selectWorkspaceByCheckbox(workspaceName);
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();

    workspaces.waitWorkspaceIsNotPresent(workspaceName);
  }
}
