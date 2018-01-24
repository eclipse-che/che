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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateWorkspaceOnDashboardTest {
  private static final String WS_NAME = generate("WsDashboard", 4);
  private static final String PROJECT_NAME = "web-java-spring";
  private static final String PATH_JAVA_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/che/examples/GreetingController.java";

  @Inject private NewWorkspace newWorkspace;
  @Inject private TestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private MachineTerminal terminal;
  @Inject private Dashboard dashboard;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

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

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.typeWorkspaceName(WS_NAME);
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(JAVA.getId());
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    seleniumWebDriver.switchFromDashboardIframeToIde();

    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab(EXPECTED_MESS_IN_CONSOLE_SEC);

    // Import the "web-java-spring" project
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(WEB_JAVA_SPRING, PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    // wait that type of the added project folder has PROJECT FOLDER status
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME, PROJECT_FOLDER);
    projectExplorer.selectItem(PROJECT_NAME);

    // open a file in the Editor
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PATH_JAVA_FILE);
    projectExplorer.openItemByPath(PATH_JAVA_FILE);
    editor.waitActive();
    editor.waitTabIsPresent("GreetingController");
  }
}
