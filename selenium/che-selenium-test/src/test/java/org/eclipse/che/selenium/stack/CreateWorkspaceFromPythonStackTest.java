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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PYTHON;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromPythonStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String PYTHON_FILE_NAME = "main.py";
  private static final String LS_INIT_MESSAGE =
      "Initialized language server 'org.eclipse.che.plugin.python.languageserver";

  @Inject private Ide ide;
  @Inject private Consoles consoles;
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
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromPythonStack() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProject(
            PYTHON, WORKSPACE_NAME, PROJECT_NAME);

    switchToIdeAndWaitWorkspaceIsReadyToUse();

    waitProjectInitialization(PROJECT_NAME);

    startCommandAndCheckMessageInTerminal(PROJECT_NAME, RUN_GOAL, "run", "Hello, world!");
    startCommandAndCheckMessageInTerminal(
        PROJECT_NAME, RUN_GOAL, "console-python3-simple:run", "Hello, world!");

    checkLanguageServerInitialization(PROJECT_NAME, PYTHON_FILE_NAME, LS_INIT_MESSAGE);
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

  private void startCommandAndCheckMessageInTerminal(
      String projectName,
      ContextMenuCommandGoals commandsGoal,
      String commandName,
      String expectedMessage) {
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.invokeCommandWithContextMenu(commandsGoal, projectName, commandName);
    consoles.waitTabNameProcessIsPresent(commandName);
    consoles.waitProcessInProcessConsoleTree(commandName);
    consoles.waitExpectedTextIntoConsole(expectedMessage, WIDGET_TIMEOUT_SEC);
  }

  private void checkLanguageServerInitialization(
      String projectName, String fileName, String textInTerminal) {
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.openItemByPath(projectName);
    projectExplorer.openItemByPath(projectName + "/" + fileName);
    editor.waitTabIsPresent(fileName);

    // check a language server initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(textInTerminal, ELEMENT_TIMEOUT_SEC);
  }
}
