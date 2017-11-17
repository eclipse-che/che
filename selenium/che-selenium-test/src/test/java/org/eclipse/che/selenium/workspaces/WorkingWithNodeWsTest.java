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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class WorkingWithNodeWsTest {
  private static final String NODE_JS_PROJECT_NAME = "web-nodejs-simple";
  private static final String ASK_DIALOG_MSG_ANGULAR_APP =
      "The process web-nodejs-simple:run will be terminated after closing console. Do you want to continue?";

  private static final String WORKSPACE = NameGenerator.generate("WorkingWithNode", 4);

  private String currentWindow;

  @Inject private TestUser defaultTestUser;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private AskDialog askDialog;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void checkNodeJsWsAndRunApp() {
    // create workspace and project
    dashboard.open();
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    createWorkspace.selectStack(TestStacksConstants.NODE.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    projectSourcePage.clickAddOrImportProjectButton();
    projectSourcePage.selectSample(NODE_JS_PROJECT_NAME);
    projectSourcePage.clickAdd();
    createWorkspace.clickCreate();
    loader.waitOnClosed();
    seleniumWebDriver.switchFromDashboardIframeToIde();

    // expand web nodeJs simple project
    currentWindow = seleniumWebDriver.getWindowHandle();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.selectItem(NODE_JS_PROJECT_NAME);
    projectExplorer.openItemByPath(NODE_JS_PROJECT_NAME);
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME + "/app");
    projectExplorer.openItemByPath(NODE_JS_PROJECT_NAME + "/app");
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME + "/app/images");
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME + "/app/scripts");
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME + "/app/styles");
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME + "/app/views");
    projectExplorer.waitItem(NODE_JS_PROJECT_NAME + "/test");

    // perform run web nodeJs application
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.BUILD,
        NODE_JS_PROJECT_NAME,
        "web-nodejs-simple:install dependencies");
    consoles.waitExpectedTextIntoConsole("bower_components/angular", 400);
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.RUN, NODE_JS_PROJECT_NAME, "web-nodejs-simple:run");
    loader.waitOnClosed();
    consoles.waitExpectedTextIntoConsole("Started connect web server", 200);
    loader.waitOnClosed();

    // check the preview url is present after refreshing
    consoles.waitPreviewUrlIsPresent();
    seleniumWebDriver.navigate().refresh();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    consoles.waitPreviewUrlIsPresent();
    projectExplorer.selectItem(NODE_JS_PROJECT_NAME);
    consoles.selectProcessInProcessConsoleTreeByName("web-nodejs-simple:run");

    // run the application
    loader.waitOnClosed();
    consoles.clickOnPreviewUrl();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    checkAngularYeomanAppl();
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriver.switchFromDashboardIframeToIde();
    consoles.closeProcessInProcessConsoleTreeByName("web-nodejs-simple:run");
    askDialog.acceptDialogWithText(ASK_DIALOG_MSG_ANGULAR_APP);
    consoles.waitProcessIsNotPresentInProcessConsoleTree("web-nodejs-simple:run");
  }

  /** check main elements of the AngularJS-Yeoman */
  public void checkAngularYeomanAppl() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[text()=\"'Allo, 'Allo!\"]")));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//img[@src='images/yeoman.png']")));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Splendid!")))
        .click();
  }
}
