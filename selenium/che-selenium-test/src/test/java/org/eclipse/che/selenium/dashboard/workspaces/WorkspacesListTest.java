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

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.DEFAULT;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_JDK8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
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
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DocumentationPage;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.*;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Statuses;
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

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceConfig workspaceConfig;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DocumentationPage documentationPage;
  @Inject private WorkspaceOverview workspaceOverview;

  private TestWorkspace blankWorkspace;
  private TestWorkspace javaWorkspace;
  private Workspaces.WorkspaceListItem expectedBlankItem;
  private Workspaces.WorkspaceListItem expectedJavaItem;
  private Workspaces.WorkspaceListItem expectedNewestWorkspaceItem;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");

    blankWorkspace = testWorkspaceProvider.createWorkspace(defaultTestUser, 2, DEFAULT);
    javaWorkspace = testWorkspaceProvider.createWorkspace(defaultTestUser, 3, UBUNTU_JDK8);

    blankWorkspace.await();
    javaWorkspace.await();

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

  @AfterClass
  public void tearDown() throws Exception {
    deleteIfWorkspaceExist(expectedJavaItem.getWorkspaceName());
    deleteIfWorkspaceExist(expectedBlankItem.getWorkspaceName());
    deleteIfWorkspaceExist(expectedNewestWorkspaceItem.getWorkspaceName());
  }

  @Test
  public void shouldDisplayElements() {
    assertEquals(EXPECTED_WORKSPACES_COUNT, dashboard.getWorkspacesCountInWorkspacesItem());

    dashboard.selectWorkspacesItemOnDashboard();

    workspaces.waitPageLoading();

    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();
    assertEquals(
        workspaces.getWorkspacesListItemByWorkspaceName(
            items, expectedBlankItem.getWorkspaceName()),
        expectedBlankItem);

    assertEquals(
        workspaces.getWorkspacesListItemByWorkspaceName(items, expectedJavaItem.getWorkspaceName()),
        expectedJavaItem);
  }

  @Test(priority = 1)
  public void checkWorkspaceSelectingByCheckbox() throws Exception {
    String blankWorkspaceName = blankWorkspace.getName();
    String javaWorkspaceName = javaWorkspace.getName();

    workspaces.selectAllWorkspacesByBulk();

    assertTrue(workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtn();

    workspaces.selectAllWorkspacesByBulk();

    assertTrue(!workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(!workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(!workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtnDisappearance();

    workspaces.selectAllWorkspacesByBulk();

    assertTrue(workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtn();

    workspaces.selectWorkspaceByCheckbox(blankWorkspaceName);

    assertTrue(workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(!workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(!workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtn();

    workspaces.selectWorkspaceByCheckbox(javaWorkspaceName);

    assertTrue(!workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(!workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(!workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtnDisappearance();

    workspaces.selectWorkspaceByCheckbox(blankWorkspaceName);

    assertTrue(!workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(!workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtn();

    workspaces.selectWorkspaceByCheckbox(javaWorkspaceName);

    assertTrue(workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtn();

    workspaces.selectAllWorkspacesByBulk();

    assertTrue(!workspaces.isWorkspaceChecked(javaWorkspaceName));
    assertTrue(!workspaces.isWorkspaceChecked(blankWorkspaceName));
    assertTrue(!workspaces.isBulkCheckboxEnabled());
    workspaces.waitDeleteWorkspaceBtnDisappearance();
  }

  @Test(priority = 2)
  public void checkSorting() {
    workspaces.clickOnRamButton();

    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();

    if (items.get(0).getRamAmount() != BLANK_WS_MB) {
      workspaces.clickOnRamButton();
      items = workspaces.getVisibleWorkspaces();
    }

    try {
      assertEquals(items.get(0).getRamAmount(), BLANK_WS_MB);
      assertEquals(items.get(1).getRamAmount(), JAVA_WS_MB);
    } catch (AssertionError ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/4242");
    }

    workspaces.clickOnRamButton();

    items = workspaces.getVisibleWorkspaces();
    try {
      assertEquals(items.get(0).getRamAmount(), JAVA_WS_MB);
      assertEquals(items.get(1).getRamAmount(), BLANK_WS_MB);
    } catch (AssertionError ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/4242");
    }

    workspaces.clickOnProjectsButton();

    items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getProjectsAmount(), BLANK_WS_PROJECTS_COUNT);
    assertEquals(items.get(1).getProjectsAmount(), JAVA_WS_PROJECTS_COUNT);

    workspaces.clickOnProjectsButton();

    items = workspaces.getVisibleWorkspaces();
    assertEquals(items.get(0).getProjectsAmount(), JAVA_WS_PROJECTS_COUNT);
    assertEquals(items.get(1).getProjectsAmount(), BLANK_WS_PROJECTS_COUNT);
  }

  @Test(priority = 3)
  public void checkSearchField() throws Exception {
    int nameLength = expectedBlankItem.getWorkspaceName().length();
    String sequenceForSearch =
        expectedBlankItem.getWorkspaceName().substring(nameLength - 5, nameLength);

    workspaces.typeToSearchInput(sequenceForSearch);

    List<Workspaces.WorkspaceListItem> items = workspaces.getVisibleWorkspaces();
    assertEquals(items.size(), 1);
    assertEquals(items.get(0).getWorkspaceName(), expectedBlankItem.getWorkspaceName());

    workspaces.typeToSearchInput("");

    items = workspaces.getVisibleWorkspaces();
    assertEquals(items.size(), 2);
    assertEquals(
        asList(
                workspaces.getWorkspacesListItemByWorkspaceName(
                    items, expectedBlankItem.getWorkspaceName()))
            .size(),
        1);
    assertEquals(
        asList(
                workspaces.getWorkspacesListItemByWorkspaceName(
                    items, expectedJavaItem.getWorkspaceName()))
            .size(),
        1);
  }

  @Test(priority = 4)
  public void checkWorkspaceActions() {
    String mainWindow = seleniumWebDriver.getWindowHandle();

    workspaces.clickOnDocumentationLink();
    seleniumWebDriverHelper.waitOpenedSomeWin();
    seleniumWebDriverHelper.switchToNextWindow(mainWindow);

    assertEquals(EXPECTED_DOCUMENTATION_PAGE_TITLE, documentationPage.getTitle());

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(mainWindow);

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();

    seleniumWebDriver.navigate().back();

    workspaces.waitPageLoading();

    workspaces.clickOnWorkspaceListItem(
        defaultTestUser.getName(), expectedBlankItem.getWorkspaceName());

    workspaceOverview.checkNameWorkspace(expectedBlankItem.getWorkspaceName());

    seleniumWebDriver.navigate().back();

    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(expectedJavaItem.getWorkspaceName());
    workspaces.clickOnWorkspaceAddProjectButton(expectedJavaItem.getWorkspaceName());

    workspaceProjects.waitProjectIsPresent(EXPECTED_JAVA_PROJECT_NAME);

    seleniumWebDriver.navigate().back();

    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(expectedJavaItem.getWorkspaceName());
    workspaces.clickOnWorkspaceConfigureButton(expectedJavaItem.getWorkspaceName());

    workspaceConfig.waitConfigForm();

    assertEquals(
        workspaceConfig.createExpectedWorkspaceConfig(expectedJavaItem.getWorkspaceName()),
        workspaceConfig.getWorkspaceConfig());

    seleniumWebDriver.navigate().back();
    workspaces.waitPageLoading();

    workspaces.moveCursorToWorkspaceRamSection(expectedJavaItem.getWorkspaceName());
    workspaces.clickOnWorkspaceStopStartButton(expectedJavaItem.getWorkspaceName());
    workspaces.waitWorkspaceStatus(expectedJavaItem.getWorkspaceName(), Statuses.STOPPED);

    workspaces.clickOnWorkspaceStopStartButton(expectedJavaItem.getWorkspaceName());
    workspaces.waitWorkspaceStatus(expectedJavaItem.getWorkspaceName(), Statuses.RUNNING);

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.typeWorkspaceName(NEWEST_CREATED_WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(NEWEST_CREATED_WORKSPACE_NAME);

    assertEquals(dashboard.getWorkspacesCountInWorkspacesItem(), EXPECTED_WORKSPACES_COUNT + 1);

    dashboard.selectWorkspacesItemOnDashboard();

    workspaces.waitPageLoading();
    assertEquals(workspaces.getVisibleWorkspacesCount(), EXPECTED_WORKSPACES_COUNT + 1);

    Workspaces.WorkspaceListItem newestCreatedWorkspaceItem =
        workspaces.getWorkspacesListItemByWorkspaceName(
            workspaces.getVisibleWorkspaces(), NEWEST_CREATED_WORKSPACE_NAME);

    assertEquals(newestCreatedWorkspaceItem, expectedNewestWorkspaceItem);

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

  private void deleteIfWorkspaceExist(String workspaceName) throws Exception {
    if (isWorkspaceExist(workspaceName)) {
      testWorkspaceServiceClient.delete(workspaceName, defaultTestUser.getName());
    }
  }

  private boolean isWorkspaceExist(String workspaceName) throws Exception {
    return testWorkspaceServiceClient.exists(workspaceName, defaultTestUser.getName());
  }
}
