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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class WorkingWithJavaMySqlStackTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final String PROJECT_NAME = "web-java-petclinic";
  private static final String PROCESS_NAME = PROJECT_NAME + ":build and deploy";

  private static final List<String> infoDataBases =
      Arrays.asList("Database", "information_schema", "petclinic", "mysql");
  private static final String MSG_CLOSE_PROCESS =
      "The process "
          + PROJECT_NAME
          + ":build and deploy will be terminated after closing console. Do you want to continue?";

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
  @Inject private CodenvyEditor editor;
  @Inject private MachineTerminal terminal;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void checkJavaMySqlAndRunApp() {
    // create workspace and project
    dashboard.open();
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    loader.waitOnClosed();
    createWorkspace.selectStack(TestStacksConstants.JAVA_MYSQL.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    projectSourcePage.clickAddOrImportProjectButton();
    projectSourcePage.selectSample(PROJECT_NAME);
    projectSourcePage.clickAdd();
    createWorkspace.clickCreate();
    loader.waitOnClosed();
    seleniumWebDriver.switchFromDashboardIframeToIde(60);

    // expand the project
    currentWindow = seleniumWebDriver.getWindowHandle();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME, 600);
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorer(
        PROJECT_NAME + "/src/main/java/org.springframework.samples.petclinic");
    projectExplorer.expandPathInProjectExplorer(
        PROJECT_NAME + "/src/test/java/org.springframework.samples.petclinic", 2);
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/test/java/org/springframework/samples/petclinic/model");
    projectExplorer.openItemByPath(
        PROJECT_NAME
            + "/src/test/java/org/springframework/samples/petclinic/model/OwnerTests.java");
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/springframework/samples/petclinic/service");
    projectExplorer.openItemByPath(
        PROJECT_NAME
            + "/src/main/java/org/springframework/samples/petclinic/service/ClinicService.java");
    editor.waitActiveEditor();

    // select the db machine and perform 'show databases'
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT_NAME, "show databases", "db");
    loader.waitOnClosed();
    for (String text : infoDataBases) {
      consoles.waitExpectedTextIntoConsole(text);
    }

    // build and deploy the web application
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.RUN, PROJECT_NAME, "build and deploy", "dev-machine");
    loader.waitOnClosed();
    consoles.waitTabNameProcessIsPresent(PROCESS_NAME);
    consoles.waitProcessInProcessConsoleTree(PROCESS_NAME);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS, 150);
    consoles.waitExpectedTextIntoConsole("Server startup in", 200);
    consoles.waitPreviewUrlIsPresent();

    // run the application
    loader.waitOnClosed();
    consoles.clickOnPreviewUrl();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    checkWebJavaPetclinicAppl();
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriver.switchFromDashboardIframeToIde();
    consoles.waitProcessInProcessConsoleTree(PROCESS_NAME);
    consoles.waitTabNameProcessIsPresent(PROCESS_NAME);
    consoles.closeProcessByTabName(PROCESS_NAME);
    askDialog.acceptDialogWithText(MSG_CLOSE_PROCESS);
    consoles.waitProcessIsNotPresentInProcessConsoleTree(PROCESS_NAME);
    consoles.waitTabNameProcessIsNotPresent(PROCESS_NAME);
    consoles.selectProcessByTabName("Terminal");
    loader.waitOnClosed();
    terminal.typeIntoTerminal("ps ax | grep tomcat8");
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("catalina.startup.Bootstrap start");
  }

  /** check main elements of the web-java-petclinic */
  private void checkWebJavaPetclinicAppl() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Welcome']")));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='navbar-inner']")));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='footer']")));
  }
}
