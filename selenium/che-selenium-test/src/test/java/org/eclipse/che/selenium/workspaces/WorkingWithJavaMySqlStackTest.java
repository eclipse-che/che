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

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA_MYSQL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.CommandsGoal.COMMON;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.CommandsGoal.RUN;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = {TestGroup.DOCKER})
public class WorkingWithJavaMySqlStackTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final String PROJECT_NAME = "web-java-petclinic";
  private static final String BUILD_AND_DEPLOY_PROCESS = PROJECT_NAME + ":build and deploy";

  private static final List<String> infoDataBases =
      Arrays.asList("Database", "information_schema", "petclinic", "mysql");
  private static final String MSG_CLOSE_PROCESS =
      format(
          "The process %s:build and deploy will be terminated after closing console. Do you want to continue?",
          PROJECT_NAME);

  @Inject private TestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private AskDialog askDialog;
  @Inject private MachineTerminal terminal;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  public void checkJavaMySqlAndRunApp() {
    String currentWindow;

    // Create a workspace from the Java-MySql stack with the web-java-petclinic project
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    createWorkspace.selectStack(JAVA_MYSQL.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(PROJECT_NAME);
    projectSourcePage.clickOnAddProjectButton();
    createWorkspace.clickOnCreateButtonAndOpenInIDE();

    seleniumWebDriver.switchFromDashboardIframeToIde(LOADER_TIMEOUT_SEC);
    currentWindow = seleniumWebDriver.getWindowHandle();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME, APPLICATION_START_TIMEOUT_SEC);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME, PROJECT_FOLDER);
    loader.waitOnClosed();
    projectExplorer.selectItem(PROJECT_NAME);

    // Select the db machine and perform 'show databases'
    projectExplorer.invokeCommandWithContextMenu(COMMON, PROJECT_NAME, "show databases", "db");
    consoles.waitTabNameProcessIsPresent("show databases");
    for (String text : infoDataBases) {
      consoles.waitExpectedTextIntoConsole(text);
    }

    // Build and deploy the web application
    consoles.startCommandFromProcessesArea("dev-machine", RUN, BUILD_AND_DEPLOY_PROCESS);
    consoles.waitTabNameProcessIsPresent(BUILD_AND_DEPLOY_PROCESS);
    consoles.waitProcessInProcessConsoleTree(BUILD_AND_DEPLOY_PROCESS);
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS, UPDATING_PROJECT_TIMEOUT_SEC);
    consoles.waitExpectedTextIntoConsole("Server startup in", PREPARING_WS_TIMEOUT_SEC);
    consoles.waitPreviewUrlIsPresent();

    // Run the application
    consoles.clickOnPreviewUrl();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    checkWebJavaPetclinicAppl();
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriver.switchFromDashboardIframeToIde();

    // Close terminal tab for 'build and deploy' process
    consoles.waitProcessInProcessConsoleTree(BUILD_AND_DEPLOY_PROCESS);
    consoles.waitTabNameProcessIsPresent(BUILD_AND_DEPLOY_PROCESS);
    consoles.closeProcessByTabName(BUILD_AND_DEPLOY_PROCESS);
    askDialog.acceptDialogWithText(MSG_CLOSE_PROCESS);
    consoles.waitProcessIsNotPresentInProcessConsoleTree(BUILD_AND_DEPLOY_PROCESS);
    consoles.waitTabNameProcessIsNotPresent(BUILD_AND_DEPLOY_PROCESS);

    // Check that tomcat is not running
    consoles.selectProcessByTabName("Terminal");
    loader.waitOnClosed();
    terminal.typeIntoTerminal("ps ax | grep tomcat8");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("catalina.startup.Bootstrap start");
  }

  // Check main elements of the web-java-petclinic
  private void checkWebJavaPetclinicAppl() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath("//h2[text()='Welcome']")));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath("//div[@class='navbar-inner']")));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(presenceOfElementLocated(By.xpath("//table[@class='footer']")));
  }
}
