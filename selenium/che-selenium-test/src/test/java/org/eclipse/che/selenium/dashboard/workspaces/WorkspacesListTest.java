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
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.TestGroup.FLAKY;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.Collections;
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
@Test(groups = {FLAKY})
public class WorkspacesListTest {
  private static final String EXPECTED_JAVA_PROJECT_NAME = "console-java-simple";
  private static final String NEWEST_CREATED_WORKSPACE_NAME = "just-created-workspace";
  private static final String WORKSPACE_NAME = generate("test-workspace", 4);
  private static final String WORKSPACE_NAME2 = generate("test-workspace", 4);
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

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();

    createWorkspaceHelper.createAndEditWorkspaceFromStack(
        Devfile.JAVA_MAVEN, WORKSPACE_NAME, Collections.emptyList(), null);
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);

    createWorkspaceHelper.createAndEditWorkspaceFromStack(
        Devfile.JAVA_MAVEN, WORKSPACE_NAME2, Collections.emptyList(), null);
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME2);
  }

  @BeforeMethod
  public void prepareToTestMethod() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
    workspaceServiceClient.delete(WORKSPACE_NAME2, defaultTestUser.getName());
    workspaceServiceClient.delete(NEWEST_CREATED_WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void shouldDisplayElements() throws Exception {
    workspaces.waitPageLoading();
    dashboard.waitWorkspacesCountInWorkspacesItem(testWorkspaceServiceClient.getWorkspacesCount());

    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(WORKSPACE_NAME), "1");

    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME2);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(WORKSPACE_NAME), "1");
  }

  @Test
  public void checkWorkspaceSelectingByCheckbox() {
    String blankWorkspaceName = WORKSPACE_NAME;
    String javaWorkspaceName = WORKSPACE_NAME2;

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

  @Test(groups = UNDER_REPAIR)
  public void checkSearchField() throws Exception {
    int nameLength = WORKSPACE_NAME.length();
    int existingWorkspacesCount = testWorkspaceServiceClient.getWorkspacesCount();
    String sequenceForSearch = WORKSPACE_NAME.substring(nameLength - 5, nameLength);

    workspaces.waitVisibleWorkspacesCount(existingWorkspacesCount);
    workspaces.typeToSearchInput(sequenceForSearch);

    try {
      workspaces.waitVisibleWorkspacesCount(EXPECTED_SEARCHED_WORKSPACES_COUNT);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/13950");
    }

    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getWorkspaceName(), WORKSPACE_NAME);

    // check displaying list size
    workspaces.typeToSearchInput("");
    workspaces.waitVisibleWorkspacesCount(testWorkspaceServiceClient.getWorkspacesCount());

    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(WORKSPACE_NAME), "1");
    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME2);
    Assert.assertEquals(workspaces.getWorkspaceProjectsValue(WORKSPACE_NAME), "1");
  }

  @Test
  public void checkWorkspaceActions() throws Exception {
    workspaces.waitPageLoading();

    // go to workspace details by clicking on item in workspaces list
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();

    seleniumWebDriver.navigate().back();

    workspaces.waitPageLoading();

    workspaces.clickOnWorkspaceListItem(defaultTestUser.getName(), WORKSPACE_NAME);

    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);

    seleniumWebDriver.navigate().back();

    // check "Add project" button
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(WORKSPACE_NAME2);
    workspaces.clickOnWorkspaceAddProjectButton(WORKSPACE_NAME2);

    workspaceProjects.waitProjectIsPresent(EXPECTED_JAVA_PROJECT_NAME);

    seleniumWebDriver.navigate().back();

    // check "Workspace configuration" button
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(WORKSPACE_NAME2);
    workspaces.clickOnWorkspaceConfigureButton(WORKSPACE_NAME2);
    workspaceConfig.waitConfigForm();

    seleniumWebDriver.navigate().back();

    // check stop/start button
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(WORKSPACE_NAME2);
    workspaces.clickOnWorkspaceStopStartButton(WORKSPACE_NAME2);
    workspaces.waitWorkspaceStatus(WORKSPACE_NAME2, Status.RUNNING);

    workspaces.clickOnWorkspaceStopStartButton(WORKSPACE_NAME2);
    workspaces.waitWorkspaceStatus(WORKSPACE_NAME2, Status.STOPPED);

    // check adding the workspace to list
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.typeWorkspaceName(NEWEST_CREATED_WORKSPACE_NAME);
    newWorkspace.selectDevfile(Devfile.JAVA_MAVEN);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(NEWEST_CREATED_WORKSPACE_NAME);

    dashboard.selectWorkspacesItemOnDashboard();

    workspaces.waitPageLoading();
    workspaces.waitVisibleWorkspacesCount(testWorkspaceServiceClient.getWorkspacesCount());
  }

  @Test(priority = 1)
  public void deleteWorkspacesByCheckboxes() {
    workspaces.waitPageLoading();

    workspaces.selectWorkspaceByCheckbox(NEWEST_CREATED_WORKSPACE_NAME);
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();

    workspaces.waitWorkspaceIsNotPresent(NEWEST_CREATED_WORKSPACE_NAME);
  }
}
