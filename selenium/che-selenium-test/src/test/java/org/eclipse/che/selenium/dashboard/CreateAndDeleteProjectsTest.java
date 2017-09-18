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

import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;

import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardProject;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateAndDeleteProjectsTest {

  private final String WORKSPACE = NameGenerator.generate("workspace", 4);

  @Inject private Dashboard dashboard;
  @Inject private DashboardProject dashboardProject;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private Loader loader;
  @Inject private ProjectExplorer explorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void createAndDeleteProjectTest() throws ExecutionException, InterruptedException {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);

    dashboardWorkspace.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    createWorkspace.selectStack(TestStacksConstants.JAVA.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickAddOrImportProjectButton();

    projectSourcePage.selectSample(DashboardProject.Template.WEB_JAVA_SPRING.value());
    projectSourcePage.selectSample(DashboardProject.Template.CONSOLE_JAVA_SIMPLE.value());
    projectSourcePage.clickAdd();

    createWorkspace.clickCreate();

    String dashboardWindow = seleniumWebDriver.getWindowHandle();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    loader.waitOnClosed();
    explorer.waitProjectExplorer();
    explorer.waitItem(DashboardProject.Template.CONSOLE_JAVA_SIMPLE.value());
    explorer.waitFolderDefinedTypeOfFolderByPath(
        DashboardProject.Template.CONSOLE_JAVA_SIMPLE.value(), PROJECT_FOLDER);
    explorer.waitFolderDefinedTypeOfFolderByPath(
        DashboardProject.Template.WEB_JAVA_SPRING.value(), PROJECT_FOLDER);
    switchToWindow(dashboardWindow);
    dashboard.selectWorkspacesItemOnDashboard();

    dashboardWorkspace.selectWorkspaceItemName(WORKSPACE);
    dashboardWorkspace.selectTabInWorspaceMenu(DashboardWorkspace.TabNames.PROJECTS);
    dashboardProject.waitProjectIsPresent(DashboardProject.Template.WEB_JAVA_SPRING.value());
    dashboardProject.waitProjectIsPresent(DashboardProject.Template.CONSOLE_JAVA_SIMPLE.value());
    dashboardProject.openSettingsForProjectByName(
        DashboardProject.Template.WEB_JAVA_SPRING.value());
    dashboardProject.clickOnDeleteProject();
    dashboardProject.clickOnDeleteItInDialogWindow();
    dashboardProject.waitProjectIsNotPresent(DashboardProject.Template.WEB_JAVA_SPRING.value());
    dashboardProject.openSettingsForProjectByName(
        DashboardProject.Template.CONSOLE_JAVA_SIMPLE.value());
    dashboardProject.clickOnDeleteProject();
    dashboardProject.clickOnDeleteItInDialogWindow();
    dashboardProject.waitProjectIsNotPresent(DashboardProject.Template.CONSOLE_JAVA_SIMPLE.value());
  }

  private void switchToWindow(String windowHandle) {
    seleniumWebDriver.switchTo().window(windowHandle);
  }
}
