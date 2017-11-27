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
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardProject;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardProject.Template;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace.TabNames;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class WorkspaceDetailsTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final String PROJECT_NAME = "web-java-petclinic";

  private Map<String, Boolean> installers = new HashMap<>();
  private Map<String, String> variables = new HashMap<>();

  @Inject private TestUser testUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private DashboardProject dashboardProject;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    createMaps();
    createWsFromJavaMySqlStack();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, testUser.getName());
  }

  @Test
  public void workingWithEnvVariables() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.ENV_VARIABLES);

    // create a new variable, save changes and check it exists
    dashboardWorkspace.selectMachine("Environment variables", "dev-machine");
    dashboardWorkspace.clickOnAddEnvVariableButton();
    dashboardWorkspace.checkAddNewEnvVarialbleDialogIsOpen();
    dashboardWorkspace.addNewEnvironmentVariable("logi", "admin");
    dashboardWorkspace.clickOnAddDialogButton();
    clickOnSaveButton();
    Assert.assertTrue(dashboardWorkspace.checkEnvVariableExists("logi"));

    // rename the variable, save changes and check it is renamed
    Assert.assertTrue(dashboardWorkspace.checkValueExists("logi", "admin"));
    dashboardWorkspace.clickOnEditEnvVariableButton("logi");
    dashboardWorkspace.enterEnvVariableName("login");
    dashboardWorkspace.clickOnUpdateDialogButton();
    clickOnSaveButton();
    Assert.assertTrue(dashboardWorkspace.checkValueExists("login", "admin"));

    // delete the variable, save changes and check it is not exists
    dashboardWorkspace.clickOnEnvVariableCheckbox("login");
    dashboardWorkspace.clickOnDeleteBtn();
    dashboardWorkspace.clickOnDeleteDialogButton();
    clickOnSaveButton();
    dashboardWorkspace.checkValueIsNotExists("login", "admin");

    // delete all variable from db machine, check they don't exist and save changes
    dashboardWorkspace.selectMachine("Environment variables", "db");
    variables.forEach(
        (name, value) -> {
          dashboardWorkspace.clickOnDeleteEnvVariableButton(name);
          dashboardWorkspace.clickOnDeleteDialogButton();
          dashboardWorkspace.checkValueIsNotExists(name, value);
        });

    clickOnSaveButton();

    // restore variables to db machine, check they exist and save changes
    variables.forEach(
        (name, value) -> {
          loader.waitOnClosed();
          dashboardWorkspace.clickOnAddEnvVariableButton();
          dashboardWorkspace.checkAddNewEnvVarialbleDialogIsOpen();
          dashboardWorkspace.addNewEnvironmentVariable(name, value);
          dashboardWorkspace.clickOnAddDialogButton();
          Assert.assertTrue(dashboardWorkspace.checkEnvVariableExists(name));
          Assert.assertTrue(dashboardWorkspace.checkValueExists(name, value));
        });
    clickOnSaveButton();
  }

  @Test
  public void workingWithInstallers() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.INSTALLERS);

    // check all needed installers in dev-machine exist
    dashboardWorkspace.selectMachine("Workspace Installers", "dev-machine");
    installers.forEach(
        (name, value) -> {
          dashboardWorkspace.checkInstallerExists(name);
        });

    // switch all installers and save changes
    installers.forEach(
        (name, value) -> {
          Assert.assertEquals(dashboardWorkspace.getInstallerState(name), value);
          dashboardWorkspace.switchInstallerState(name);
          WaitUtils.sleepQuietly(1);
        });
    clickOnSaveButton();

    // switch all installers, save changes and check its states are as previous(by default for the
    // Java-MySql stack)
    installers.forEach(
        (name, value) -> {
          dashboardWorkspace.switchInstallerState(name);
          loader.waitOnClosed();
        });
    clickOnSaveButton();
    installers.forEach(
        (name, value) -> {
          Assert.assertEquals(dashboardWorkspace.getInstallerState(name), value);
        });
  }

  @Test
  public void workingWithServers() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.SERVERS);

    // add a new server to db machine, save changes and check it exists
    dashboardWorkspace.selectMachine("Servers", "db");
    dashboardWorkspace.clickOnAddServerButton();
    dashboardWorkspace.waitAddServerDialogIsOpen();
    dashboardWorkspace.enterReference("agen");
    dashboardWorkspace.enterPort("8080");
    dashboardWorkspace.enterProtocol("https");
    dashboardWorkspace.clickOnAddDialogButton();
    clickOnSaveButton();
    dashboardWorkspace.checkServerExists("agen", "8080");

    // edit the server and check it exists
    dashboardWorkspace.clickOnEditServerButton("agen");
    dashboardWorkspace.enterReference("agent");
    dashboardWorkspace.enterPort("80");
    dashboardWorkspace.enterProtocol("http");
    dashboardWorkspace.clickOnUpdateDialogButton();
    dashboardWorkspace.checkServerExists("agent", "80");

    // delete the server and check it is not exist
    dashboardWorkspace.clickOnDeleteServerButton("agent");
    dashboardWorkspace.clickOnDeleteDialogButton();
    clickOnSaveButton();
    dashboardWorkspace.checkServerIsNotExists("agent", "80");
  }

  @Test
  public void workingWithMachines() {
    String machineName = "new_machine";

    // check that all machines of the Java-MySql stack created by default exist
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.MACHINES);
    dashboardWorkspace.checkMachineExists("db");
    dashboardWorkspace.checkMachineExists("dev-machine");

    // create a new machine, delete and check it is not exist
    createMachine(machineName);
    dashboardWorkspace.clickOnDeleteMachineButton(machineName);
    dashboardWorkspace.clickOnCloseDialogButton();
    loader.waitOnClosed();
    dashboardWorkspace.clickOnDeleteMachineButton(machineName);
    dashboardWorkspace.clickOnDeleteDialogButton();
    dashboardWorkspace.checkMachineIsNotExists(machineName);

    // create a new machine, edit(change the name) and save changes
    createMachine(machineName);
    dashboardWorkspace.clickOnEditMachineButton(machineName);
    dashboardWorkspace.checkEditTheMachineDialogIsOpen();
    dashboardWorkspace.setMachineNameInDialog("machine");
    dashboardWorkspace.clickOnEditDialogButton();
    dashboardWorkspace.checkMachineExists("machine");
    clickOnSaveButton();
  }

  @Test(priority = 1)
  public void workingWithProjects() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.PROJECTS);

    // create a new project and save changes
    dashboardWorkspace.clickOnAddNewProjectButton();
    dashboardWorkspace.selectSample("web-java-petclinic");
    dashboardWorkspace.clickOnAddProjects();
    clickOnSaveButton();

    // check that project exists(workspace will restart)
    dashboardProject.waitProjectIsPresent(Template.WEB_JAVA_PETCLINIC.value());

    // start the workspace and check that the new project exists
    dashboardWorkspace.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(
        Template.WEB_JAVA_PETCLINIC.value(), PROJECT_FOLDER);

    // check that created machine exists in the Process Console tree
    consoles.waitProcessInProcessConsoleTree("machine");
    consoles.waitTabNameProcessIsPresent("machine");
  }

  private void createMaps() {
    installers.put("C# language server", false);
    installers.put("Exec", true);
    installers.put("File sync", false);
    installers.put("Git credentials", false);
    installers.put("JSON language server", false);
    installers.put("PHP language server", false);
    installers.put("Python language server", false);
    installers.put("SSH", true);
    installers.put("Terminal", true);
    installers.put("TypeScript language server", false);
    installers.put("Workspace API", true);
    installers.put("Yaml language server", false);

    variables.put("MYSQL_DATABASE", "petclinic");
    variables.put("MYSQL_PASSWORD", "password");
    variables.put("MYSQL_ROOT_PASSWORD", "password");
    variables.put("MYSQL_USER", "petclinic");
  }

  private void clickOnSaveButton() {
    dashboardWorkspace.clickOnSaveBtn();
    try {
      dashboard.waitNotificationMessage("Workspace updated");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7178");
    }

    dashboard.waitNotificationIsClosed();
  }

  private void createWsFromJavaMySqlStack() {
    // create and start a workspace from the Java-MySql stack
    dashboard.open();
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    loader.waitOnClosed();
    createWorkspace.selectStack(TestStacksConstants.JAVA_MYSQL.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    createWorkspace.clickCreate();

    seleniumWebDriver.switchFromDashboardIframeToIde(60);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.selectWorkspaceItemName(WORKSPACE);
    dashboardWorkspace.waitToolbarTitleName(WORKSPACE);
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.OVERVIEW);
    dashboardWorkspace.checkStateOfWorkspace(DashboardWorkspace.StateWorkspace.RUNNING);
    dashboardWorkspace.clickOnStopWorkspace();
    dashboardWorkspace.checkStateOfWorkspace(DashboardWorkspace.StateWorkspace.STOPPED);
  }

  private void createMachine(String machineName) {
    // add new machine and check it exists
    dashboardWorkspace.clickOnAddMachineButton();
    dashboardWorkspace.checkAddNewMachineDialogIsOpen();
    dashboardWorkspace.setMachineNameInDialog(machineName);
    dashboardWorkspace.clickOnAddDialogButton();
    dashboardWorkspace.checkMachineExists(machineName);
  }
}
