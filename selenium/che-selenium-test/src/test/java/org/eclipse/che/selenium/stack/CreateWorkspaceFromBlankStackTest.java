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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.BLANK;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromBlankStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "blank-project";

  @Inject private Ide ide;
  @Inject private Dashboard dashboard;
  @Inject private CodenvyEditor editor;
  @Inject private Workspaces workspaces;
  @Inject private ToastLoader toastLoader;
  @Inject private NewWorkspace newWorkspace;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void createWorkspaceFromBlankStackTest() {
    createWorkspaceWithProjectFromStack(BLANK, WORKSPACE_NAME, PROJECT_NAME);

    switchToIdeAndWaitWorkspaceIsReadyToUse();

    waitProjectInitialization(PROJECT_NAME);

    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
    editor.waitActive();
    editor.waitTabIsPresent("README.md");
  }

  private void createWorkspaceWithProjectFromStack(
      NewWorkspace.Stack stack, String workspaceName, String projectName) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  private void switchToIdeAndWaitWorkspaceIsReadyToUse() {
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  private void waitProjectInitialization(String projectName) {
    projectExplorer.waitItem(projectName);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    mavenPluginStatusBar.waitClosingInfoPanel();
    projectExplorer.waitDefinedTypeOfFolder(projectName, PROJECT_FOLDER);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
  }
}
