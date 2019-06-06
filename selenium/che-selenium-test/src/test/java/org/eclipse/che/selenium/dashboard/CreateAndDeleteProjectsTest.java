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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.PROJECTS;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateAndDeleteProjectsTest {

  private static final String WORKSPACE = generate("workspace", 4);
  private static final String SECOND_WEB_JAVA_SPRING_PROJECT_NAME = WEB_JAVA_SPRING + "-1";

  private String dashboardWindow;

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private ProjectExplorer explorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private Workspaces workspaces;
  @Inject private Ide ide;
  @Inject private ToastLoader toastLoader;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void createProjectTest() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    // we are selecting 'Java' stack from the 'All Stack' tab for compatibility with OSIO
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(WORKSPACE);

    // create 'web-java-spring' and 'console-java-simple' projects
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(WEB_JAVA_SPRING);
    projectSourcePage.selectSample(CONSOLE_JAVA_SIMPLE);
    projectSourcePage.clickOnAddProjectButton();

    // create 'web-java-spring-1' project
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(WEB_JAVA_SPRING);
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WORKSPACE, defaultTestUser);

    // switch to the IDE and wait for workspace is ready to use
    dashboardWindow = seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();

    // wait for projects initializing
    explorer.waitItem(WEB_JAVA_SPRING);
    explorer.waitItem(CONSOLE_JAVA_SIMPLE);
    explorer.waitItem(SECOND_WEB_JAVA_SPRING_PROJECT_NAME);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    mavenPluginStatusBar.waitClosingInfoPanel();
    explorer.waitDefinedTypeOfFolder(CONSOLE_JAVA_SIMPLE, PROJECT_FOLDER);
    explorer.waitDefinedTypeOfFolder(WEB_JAVA_SPRING, PROJECT_FOLDER);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
  }

  @Test(priority = 1)
  public void deleteProjectsFromDashboardTest() {
    seleniumWebDriver.switchTo().window(dashboardWindow);
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WORKSPACE);
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS);

    deleteProject(WEB_JAVA_SPRING);

    workspaceProjects.waitProjectIsPresent(SECOND_WEB_JAVA_SPRING_PROJECT_NAME);

    deleteProject(CONSOLE_JAVA_SIMPLE);
  }

  private void deleteProject(String projectName) {
    workspaceProjects.waitProjectIsPresent(projectName);

    workspaceProjects.clickOnCheckbox(projectName);
    workspaceProjects.clickOnDeleteButton();
    workspaceDetails.clickOnDeleteButtonInDialogWindow();

    workspaceProjects.waitProjectIsNotPresent(projectName);
  }
}
