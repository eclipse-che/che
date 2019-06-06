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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.STOP_WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateWorkspaceOnDashboardTest {

  private static final String WS_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "test-project";
  private static final String PATH_TO_EXPAND = "/src/main/java/org.eclipse.che.examples";
  private static final String PATH_JAVA_FILE =
      "/src/main/java/org/eclipse/che/examples/GreetingController.java";

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private NewWorkspace newWorkspace;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ToastLoader toastLoader;
  @Inject private Workspaces workspaces;
  @Inject private CodenvyEditor editor;
  @Inject private Dashboard dashboard;
  @Inject private Wizard wizard;
  @Inject private Menu menu;
  @Inject private Ide ide;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WS_NAME, defaultTestUser.getName());
  }

  @Test
  public void createWorkspaceOnDashboardTest() {
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    // create and start a new workspace
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.typeWorkspaceName(WS_NAME);
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WS_NAME, defaultTestUser);

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();

    // wait that the workspace is started
    ide.waitOpenedWorkspaceIsReadyToUse();

    // Import the "web-java-spring" project
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(getSampleProjectName(), PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    // wait that type of the added project folder has PROJECT FOLDER status
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME, PROJECT_FOLDER);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // open a file in the Editor
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + getPathToExpand());
    projectExplorer.openItemByPath(PROJECT_NAME + getPathJavaFile());
    editor.waitActive();
    editor.waitTabIsPresent(getTabName());

    // stop the workspace
    menu.runCommand(WORKSPACE, STOP_WORKSPACE);
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running");
  }

  protected String getSampleProjectName() {
    return WEB_JAVA_SPRING;
  }

  protected String getPathToExpand() {
    return PATH_TO_EXPAND;
  }

  protected String getPathJavaFile() {
    return PATH_JAVA_FILE;
  }

  protected String getTabName() {
    return "GreetingController";
  }
}
