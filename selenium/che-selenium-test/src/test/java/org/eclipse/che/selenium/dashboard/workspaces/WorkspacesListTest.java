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
package org.eclipse.che.selenium.dashboard.workspaces;

import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_JDK8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DocumentationPage;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.*;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status;
import org.testng.annotations.*;

/**
 * @author Sergey Skorik
 * @author Ihor Okhrimenko
 */
@Test(groups = TestGroup.OSIO)
public class WorkspacesListTest {
  private static final int EXPECTED_WORKSPACES_COUNT = 2;
  private static final int BLANK_WS_MB = 2048;
  private static final int JAVA_WS_MB = 3072;
  private static final int BLANK_WS_PROJECTS_COUNT = 0;
  private static final int JAVA_WS_PROJECTS_COUNT = 1;
  private static final String EXPECTED_DOCUMENTATION_PAGE_TITLE = "What Is a Che Workspace?";
  private static final String EXPECTED_JAVA_PROJECT_NAME = "web-java-spring";
  private static final String NEWEST_CREATED_WORKSPACE_NAME = "just-created-workspace";
  private static final int EXPECTED_SORTED_WORKSPACES_COUNT = 1;

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceConfig workspaceConfig;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DocumentationPage documentationPage;
  @Inject private WorkspaceOverview workspaceOverview;

  @InjectTestWorkspace(memoryGb = 2, startAfterCreation = false)
  private TestWorkspace blankWorkspace;

  @InjectTestWorkspace(template = UBUNTU_JDK8, memoryGb = 3)
  private TestWorkspace javaWorkspace;

  private Workspaces.WorkspaceListItem expectedBlankItem;
  private Workspaces.WorkspaceListItem expectedJavaItem;
  private Workspaces.WorkspaceListItem expectedNewestWorkspaceItem;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");

    testProjectServiceClient.importProject(
        javaWorkspace.getId(), Paths.get(resource.toURI()), "web-java-spring", MAVEN_SPRING);

    expectedBlankItem =
        new Workspaces.WorkspaceListItem(
            defaultTestUser.getName(),
            blankWorkspace.getName(),
            BLANK_WS_MB,
            BLANK_WS_PROJECTS_COUNT);
    expectedJavaItem =
        new Workspaces.WorkspaceListItem(
            defaultTestUser.getName(), javaWorkspace.getName(), JAVA_WS_MB, JAVA_WS_PROJECTS_COUNT);

    expectedNewestWorkspaceItem =
        new Workspaces.WorkspaceListItem(
            defaultTestUser.getName(),
            NEWEST_CREATED_WORKSPACE_NAME,
            BLANK_WS_MB,
            BLANK_WS_PROJECTS_COUNT);

    dashboard.open();
  }

  @BeforeMethod
  public void prepareToTestMethod() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testWorkspaceServiceClient.delete(
        expectedNewestWorkspaceItem.getWorkspaceName(), defaultTestUser.getName());
  }

  @Test
  public void shouldDisplayElements() throws Exception {
    workspaces.waitPageLoading();
    dashboard.waitWorkspacesCountInWorkspacesItem(getWorkspacesCount());

    checkExpectedBlankWorkspaceDisplaying();

    checkExpectedJavaWorkspaceDisplaying();
  }

  @Test
  public void checkWorkspaceSelectingByCheckbox() throws Exception {
    String blankWorkspaceName = blankWorkspace.getName();
    String javaWorkspaceName = javaWorkspace.getName();

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
  public void checkSorting() {
    workspaces.waitPageLoading();
    workspaces.clickOnRamButton();

    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();

    // items are sorted by name, check is present for ensuring of items order
    if (items.get(0).getRamAmount() != BLANK_WS_MB) {
      workspaces.clickOnRamButton();
      items = workspaces.getVisibleWorkspaces();
    }

    // check items order after "RAM" clicking
    try {
      assertEquals(items.get(0).getRamAmount(), BLANK_WS_MB);
      assertEquals(items.get(1).getRamAmount(), JAVA_WS_MB);
    } catch (AssertionError ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/4242");
    }

    // check reverse order after "RAM" clicking
    workspaces.clickOnRamButton();
    items = workspaces.getVisibleWorkspaces();
    try {
      assertEquals(items.get(0).getRamAmount(), JAVA_WS_MB);
      assertEquals(items.get(1).getRamAmount(), BLANK_WS_MB);
    } catch (AssertionError ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/4242");
    }

    // check items order after "Projects" clicking
    workspaces.clickOnProjectsButton();
    items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getProjectsAmount(), BLANK_WS_PROJECTS_COUNT);
    assertEquals(items.get(1).getProjectsAmount(), JAVA_WS_PROJECTS_COUNT);

    // check items reverse order after "Projects" clicking
    workspaces.clickOnProjectsButton();
    items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getProjectsAmount(), JAVA_WS_PROJECTS_COUNT);
    assertEquals(items.get(1).getProjectsAmount(), BLANK_WS_PROJECTS_COUNT);
  }

  @Test
  public void checkSearchField() throws Exception {
    int nameLength = expectedBlankItem.getWorkspaceName().length();
    int existingWorkspacesCount = getWorkspacesCount();
    String sequenceForSearch =
        expectedBlankItem.getWorkspaceName().substring(nameLength - 5, nameLength);

    workspaces.waitVisibleWorkspacesCount(existingWorkspacesCount);

    workspaces.typeToSearchInput(sequenceForSearch);
    workspaces.waitVisibleWorkspacesCount(EXPECTED_SORTED_WORKSPACES_COUNT);
    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getWorkspaceName(), expectedBlankItem.getWorkspaceName());

    // check displaying list size
    workspaces.typeToSearchInput("");
    workspaces.waitVisibleWorkspacesCount(getWorkspacesCount());

    // check that expected blank and java items are displaying, in sum with previous items count
    // checking it gives a full workspaces list checking
    checkExpectedBlankWorkspaceDisplaying();
    checkExpectedJavaWorkspaceDisplaying();
  }

  @Test(priority = 1)
  public void checkWorkspaceActions() throws Exception {
    workspaces.waitPageLoading();
    String mainWindow = seleniumWebDriver.getWindowHandle();

    // check documentation link
    workspaces.clickOnDocumentationLink();
    seleniumWebDriverHelper.waitOpenedSomeWin();
    seleniumWebDriverHelper.switchToNextWindow(mainWindow);

    assertEquals(EXPECTED_DOCUMENTATION_PAGE_TITLE, documentationPage.getTitle());

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(mainWindow);

    // go to workspace details by clicking on item in workspaces list
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();

    seleniumWebDriver.navigate().back();

    workspaces.waitPageLoading();

    workspaces.clickOnWorkspaceListItem(
        defaultTestUser.getName(), expectedBlankItem.getWorkspaceName());

    workspaceOverview.checkNameWorkspace(expectedBlankItem.getWorkspaceName());

    seleniumWebDriver.navigate().back();

    // check "Add project" button
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(expectedJavaItem.getWorkspaceName());
    workspaces.clickOnWorkspaceAddProjectButton(expectedJavaItem.getWorkspaceName());

    workspaceProjects.waitProjectIsPresent(EXPECTED_JAVA_PROJECT_NAME);

    seleniumWebDriver.navigate().back();

    // check "Workspace configuration" button
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(expectedJavaItem.getWorkspaceName());
    workspaces.clickOnWorkspaceConfigureButton(expectedJavaItem.getWorkspaceName());

    workspaceConfig.waitConfigForm();

    assertEquals(
        workspaceConfig.createExpectedWorkspaceConfig(expectedJavaItem.getWorkspaceName()),
        workspaceConfig.getWorkspaceConfig());

    seleniumWebDriver.navigate().back();

    // check stop/start button
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(expectedJavaItem.getWorkspaceName());
    workspaces.clickOnWorkspaceStopStartButton(expectedJavaItem.getWorkspaceName());
    workspaces.waitWorkspaceStatus(expectedJavaItem.getWorkspaceName(), Status.STOPPED);

    workspaces.clickOnWorkspaceStopStartButton(expectedJavaItem.getWorkspaceName());
    workspaces.waitWorkspaceStatus(expectedJavaItem.getWorkspaceName(), Status.RUNNING);

    // check adding the workspace to list
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.typeWorkspaceName(NEWEST_CREATED_WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(NEWEST_CREATED_WORKSPACE_NAME);
    workspaces.waitVisibleWorkspacesCount(getWorkspacesCount());

    dashboard.selectWorkspacesItemOnDashboard();

    workspaces.waitPageLoading();
    workspaces.waitVisibleWorkspacesCount(getWorkspacesCount());

    Workspaces.WorkspaceListItem newestCreatedWorkspaceItem =
        workspaces.getWorkspacesListItemByWorkspaceName(
            workspaces.getVisibleWorkspaces(), NEWEST_CREATED_WORKSPACE_NAME);

    assertEquals(newestCreatedWorkspaceItem, expectedNewestWorkspaceItem);

    // delete workspaces by checkboxes
    workspaces.selectWorkspaceByCheckbox(expectedNewestWorkspaceItem.getWorkspaceName());
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();

    workspaces.waitWorkspaceIsNotPresent(expectedNewestWorkspaceItem.getWorkspaceName());

    workspaces.selectAllWorkspacesByBulk();
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();

    workspaces.waitWorkspaceIsNotPresent(expectedBlankItem.getWorkspaceName());
    workspaces.waitWorkspaceIsNotPresent(expectedJavaItem.getWorkspaceName());
  }

  private void checkExpectedBlankWorkspaceDisplaying() {
    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();

    Workspaces.WorkspaceListItem currentDisplayingBlankItem =
        workspaces.getWorkspacesListItemByWorkspaceName(
            items, expectedBlankItem.getWorkspaceName());

    assertEquals(currentDisplayingBlankItem, expectedBlankItem);
  }

  private void checkExpectedJavaWorkspaceDisplaying() {
    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();

    Workspaces.WorkspaceListItem currentDisplayingJavaItem =
        workspaces.getWorkspacesListItemByWorkspaceName(items, expectedJavaItem.getWorkspaceName());

    assertEquals(currentDisplayingJavaItem, expectedJavaItem);
  }

  private int getWorkspacesCount() throws Exception {
    return testWorkspaceServiceClient.getAll().size();
  }
}
