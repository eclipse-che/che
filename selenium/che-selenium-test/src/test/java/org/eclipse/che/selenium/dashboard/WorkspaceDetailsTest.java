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
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_PETCLINIC;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.TabNames.ENV_VARIABLES;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.TabNames.INSTALLERS;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.TabNames.MACHINES;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.TabNames.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.TabNames.PROJECTS;
import static org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails.TabNames.SERVERS;

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
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.Workspaces;
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
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private DashboardProject dashboardProject;
  @Inject private Consoles consoles;
  @Inject private Workspaces workspaces;
  @Inject private ProjectSourcePage projectSourcePage;

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
    workspaceDetails.selectTabInWorspaceMenu(ENV_VARIABLES);

    // create a new variable, save changes and check it exists
    workspaceDetails.selectMachine("Environment variables", "dev-machine");
    workspaceDetails.clickOnAddEnvVariableButton();
    workspaceDetails.checkAddNewEnvVarialbleDialogIsOpen();
    workspaceDetails.addNewEnvironmentVariable("logi", "admin");
    workspaceDetails.clickOnAddDialogButton();
    clickOnSaveButton();
    Assert.assertTrue(workspaceDetails.checkEnvVariableExists("logi"));

    // rename the variable, save changes and check it is renamed
    Assert.assertTrue(workspaceDetails.checkValueExists("logi", "admin"));
    workspaceDetails.clickOnEditEnvVariableButton("logi");
    workspaceDetails.enterEnvVariableName("login");
    workspaceDetails.clickOnUpdateDialogButton();
    clickOnSaveButton();
    Assert.assertTrue(workspaceDetails.checkValueExists("login", "admin"));

    // delete the variable, save changes and check it is not exists
    workspaceDetails.clickOnEnvVariableCheckbox("login");
    workspaceDetails.clickOnDeleteBtn();
    workspaceDetails.clickOnDeleteDialogButton();
    clickOnSaveButton();
    workspaceDetails.checkValueIsNotExists("login", "admin");

    // delete all variable from db machine, check they don't exist and save changes
    workspaceDetails.selectMachine("Environment variables", "db");
    variables.forEach(
        (name, value) -> {
          workspaceDetails.clickOnDeleteEnvVariableButton(name);
          workspaceDetails.clickOnDeleteDialogButton();
          workspaceDetails.checkValueIsNotExists(name, value);
        });

    clickOnSaveButton();

    // restore variables to db machine, check they exist and save changes
    variables.forEach(
        (name, value) -> {
          loader.waitOnClosed();
          workspaceDetails.clickOnAddEnvVariableButton();
          workspaceDetails.checkAddNewEnvVarialbleDialogIsOpen();
          workspaceDetails.addNewEnvironmentVariable(name, value);
          workspaceDetails.clickOnAddDialogButton();
          Assert.assertTrue(workspaceDetails.checkEnvVariableExists(name));
          Assert.assertTrue(workspaceDetails.checkValueExists(name, value));
        });
    clickOnSaveButton();
  }

  @Test
  public void workingWithInstallers() {
    workspaceDetails.selectTabInWorspaceMenu(INSTALLERS);

    // check all needed installers in dev-machine exist
    workspaceDetails.selectMachine("Workspace Installers", "dev-machine");
    installers.forEach(
        (name, value) -> {
          workspaceDetails.checkInstallerExists(name);
        });

    // switch all installers and save changes
    installers.forEach(
        (name, value) -> {
          Assert.assertEquals(workspaceDetails.getInstallerState(name), value);
          workspaceDetails.switchInstallerState(name);
          WaitUtils.sleepQuietly(1);
        });
    clickOnSaveButton();

    // switch all installers, save changes and check its states are as previous(by default for the
    // Java-MySql stack)
    installers.forEach(
        (name, value) -> {
          workspaceDetails.switchInstallerState(name);
          loader.waitOnClosed();
        });
    clickOnSaveButton();
    installers.forEach(
        (name, value) -> {
          Assert.assertEquals(workspaceDetails.getInstallerState(name), value);
        });
  }

  @Test
  public void workingWithServers() {
    workspaceDetails.selectTabInWorspaceMenu(SERVERS);

    // add a new server to db machine, save changes and check it exists
    workspaceDetails.selectMachine("Servers", "db");
    workspaceDetails.clickOnAddServerButton();
    workspaceDetails.waitAddServerDialogIsOpen();
    workspaceDetails.enterReference("agen");
    workspaceDetails.enterPort("8080");
    workspaceDetails.enterProtocol("https");
    workspaceDetails.clickOnAddDialogButton();
    clickOnSaveButton();
    workspaceDetails.checkServerExists("agen", "8080");

    // edit the server and check it exists
    workspaceDetails.clickOnEditServerButton("agen");
    workspaceDetails.enterReference("agent");
    workspaceDetails.enterPort("80");
    workspaceDetails.enterProtocol("http");
    workspaceDetails.clickOnUpdateDialogButton();
    workspaceDetails.checkServerExists("agent", "80");

    // delete the server and check it is not exist
    workspaceDetails.clickOnDeleteServerButton("agent");
    workspaceDetails.clickOnDeleteDialogButton();
    clickOnSaveButton();
    workspaceDetails.checkServerIsNotExists("agent", "80");
  }

  @Test
  public void workingWithMachines() {
    String machineName = "new_machine";

    // check that all machines of the Java-MySql stack created by default exist
    workspaceDetails.selectTabInWorspaceMenu(MACHINES);
    workspaceDetails.checkMachineExists("db");
    workspaceDetails.checkMachineExists("dev-machine");

    // create a new machine, delete and check it is not exist
    createMachine(machineName);
    workspaceDetails.clickOnDeleteMachineButton(machineName);
    workspaceDetails.clickOnCloseDialogButton();
    loader.waitOnClosed();
    workspaceDetails.clickOnDeleteMachineButton(machineName);
    workspaceDetails.clickOnDeleteDialogButton();
    workspaceDetails.checkMachineIsNotExists(machineName);

    // create a new machine, edit(change the name) and save changes
    createMachine(machineName);
    workspaceDetails.clickOnEditMachineButton(machineName);
    workspaceDetails.checkEditTheMachineDialogIsOpen();
    workspaceDetails.setMachineNameInDialog("machine");
    workspaceDetails.clickOnEditDialogButton();
    workspaceDetails.checkMachineExists("machine");
    clickOnSaveButton();
  }

  @Test(priority = 1)
  public void workingWithProjects() {
    workspaceDetails.selectTabInWorspaceMenu(PROJECTS);

    // create a new project and save changes
    dashboardProject.clickOnAddNewProjectButton();
    projectSourcePage.selectSample(PROJECT_NAME);
    projectSourcePage.clickOnAddProjectButton();
    clickOnSaveButton();

    // check that project exists(workspace will restart)
    dashboardProject.waitProjectIsPresent(WEB_JAVA_PETCLINIC);

    // start the workspace and check that the new project exists
    workspaceDetails.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(WEB_JAVA_PETCLINIC, PROJECT_FOLDER);

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
    workspaceDetails.clickOnSaveBtn();
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }

  private void createWsFromJavaMySqlStack() {
    // create and start a workspace from the Java-MySql stack
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    loader.waitOnClosed();
    createWorkspace.selectStack(TestStacksConstants.JAVA_MYSQL.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    createWorkspace.clickOnCreateWorkspaceButton();

    seleniumWebDriver.switchFromDashboardIframeToIde(60);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.selectWorkspaceItemName(WORKSPACE);
    workspaces.waitToolbarTitleName(WORKSPACE);
    workspaceDetails.selectTabInWorspaceMenu(OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(RUNNING);
    workspaceDetails.clickOnStopWorkspace();
    workspaceDetails.checkStateOfWorkspace(STOPPED);
  }

  private void createMachine(String machineName) {
    // add new machine and check it exists
    workspaceDetails.clickOnAddMachineButton();
    workspaceDetails.checkAddNewMachineDialogIsOpen();
    workspaceDetails.setMachineNameInDialog(machineName);
    workspaceDetails.clickOnAddDialogButton();
    workspaceDetails.checkMachineExists(machineName);
  }
}
