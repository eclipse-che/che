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

package org.eclipse.che.selenium.pageobject.dashboard;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;

/**
 * Cross dashboard-IDE operations with workspace
 *
 * @author Skoryk Serhii
 */
public class CreateWorkspaceHelper {

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private AskDialog askDialog;
  @Inject private CodenvyEditor editor;
  @Inject private Workspaces workspaces;
  @Inject private ToastLoader toastLoader;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  public void createWorkspaceFromStackWithProject(
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

  public void createWorkspaceFromStackWithoutProject(
      NewWorkspace.Stack stack, String workspaceName) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  public void createWorkspaceFromStackWithProjects(
      NewWorkspace.Stack stack, String workspaceName, ArrayList<String> projectNames) {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);
    projectSourcePage.clickOnAddOrImportProjectButton();

    projectNames.forEach(
        project -> {
          projectSourcePage.selectSample(project);
        });

    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  // Open file and check LS initialization message in "dev-machine" process
  public void checkLanguageServerInitialization(
      String projectName, String fileName, String textInTerminal) {
    consoles.selectProcessByTabName("dev-machine");
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.openItemByPath(projectName);
    projectExplorer.openItemByPath(projectName + "/" + fileName);
    editor.waitTabIsPresent(fileName);

    consoles.waitExpectedTextIntoConsole(textInTerminal, ELEMENT_TIMEOUT_SEC);
  }
}
