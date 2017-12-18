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

import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA_MYSQL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.ENV_VARIABLES;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.MACHINES;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.OVERVIEW;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceEnvVariables;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class WorkspaceDetailsComposeTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);

  private Map<String, String> variables = new HashMap<>();

  @Inject private TestUser testUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Consoles consoles;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceMachines workspaceMachines;
  @Inject private WorkspaceEnvVariables workspaceEnvVariables;
  @Inject private MachineTerminal terminal;

  @BeforeClass
  public void setUp() throws Exception {
    createMap();
    createWsFromJavaMySqlStack();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, testUser.getName());
  }

  @Test
  public void workingWithEnvVariables() {
    workspaceDetails.selectTabInWorkspaceMenu(ENV_VARIABLES);

    // create a new variable, save changes and check it exists
    workspaceMachines.selectMachine("Environment variables", "dev-machine");
    workspaceEnvVariables.clickOnAddEnvVariableButton();
    workspaceEnvVariables.checkAddNewEnvVarialbleDialogIsOpen();
    workspaceEnvVariables.addNewEnvironmentVariable("logi", "admin");
    workspaceDetails.clickOnAddButtonInDialogWindow();
    clickOnSaveButton();
    assertTrue(workspaceEnvVariables.checkEnvVariableExists("logi"));

    // rename the variable, save changes and check it is renamed
    assertTrue(workspaceEnvVariables.checkValueExists("logi", "admin"));
    workspaceEnvVariables.clickOnEditEnvVariableButton("logi");
    workspaceEnvVariables.enterEnvVariableName("login");
    workspaceDetails.clickOnUpdateButtonInDialogWindow();
    clickOnSaveButton();
    assertTrue(workspaceEnvVariables.checkValueExists("login", "admin"));

    // delete the variable, save changes and check it is not exists
    workspaceEnvVariables.clickOnEnvVariableCheckbox("login");
    workspaceEnvVariables.clickOnDeleteEnvVariableButton("login");
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    clickOnSaveButton();
    workspaceEnvVariables.checkValueIsNotExists("login", "admin");

    workspaceMachines.selectMachine("Environment variables", "db");
    // add variables to 'db' machine, check they exist and save changes
    variables.forEach(
        (name, value) -> {
          loader.waitOnClosed();
          workspaceEnvVariables.clickOnAddEnvVariableButton();
          workspaceEnvVariables.checkAddNewEnvVarialbleDialogIsOpen();
          workspaceEnvVariables.addNewEnvironmentVariable(name, value);
          workspaceDetails.clickOnAddButtonInDialogWindow();
          assertTrue(workspaceEnvVariables.checkEnvVariableExists(name));
          assertTrue(workspaceEnvVariables.checkValueExists(name, value));
        });
    clickOnSaveButton();

    // delete all variables from the 'db' machine, check they don't exist and save changes
    variables.forEach(
        (name, value) -> {
          workspaceEnvVariables.clickOnDeleteEnvVariableButton(name);
          workspaceDetails.clickOnDeleteButtonInDialogWindow();
          workspaceEnvVariables.checkValueIsNotExists(name, value);
        });

    clickOnSaveButton();
  }

  @Test
  public void workingWithMachines() {
    String machineName = "new_machine";

    // check that all machines of the Java-MySql stack created by default exist
    workspaceDetails.selectTabInWorkspaceMenu(MACHINES);
    workspaceMachines.checkMachineExists("db");
    workspaceMachines.checkMachineExists("dev-machine");

    // create a new machine, delete and check it is not exist
    createMachine(machineName);
    workspaceMachines.clickOnDeleteMachineButton(machineName);
    workspaceDetails.clickOnCloseButtonInDialogWindow();
    loader.waitOnClosed();
    workspaceMachines.clickOnDeleteMachineButton(machineName);
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaceMachines.checkMachineIsNotExists(machineName);

    // create a new machine, edit(change the name) and save changes
    createMachine(machineName);
    workspaceMachines.clickOnEditMachineButton(machineName);
    workspaceMachines.checkEditTheMachineDialogIsOpen();
    workspaceMachines.setMachineNameInDialog("machine");
    workspaceMachines.clickOnEditNameDialogButton();
    workspaceMachines.checkMachineExists("machine");
    clickOnSaveButton();
  }

  @Test(priority = 1)
  public void workingWithProjects() {
    // check that created machine exists in the Process Console tree
    workspaceDetails.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();

    consoles.waitProcessInProcessConsoleTree("machine");
    consoles.waitTabNameProcessIsPresent("machine");
  }

  private void createMap() {
    variables.put("MYSQL_DATABASE", "petclinic");
    variables.put("MYSQL_PASSWORD", "password");
    variables.put("MYSQL_ROOT_PASSWORD", "password");
    variables.put("MYSQL_USER", "petclinic");
  }

  private void clickOnSaveButton() {
    workspaceDetails.clickOnSaveChangesBtn();
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
    createWorkspace.selectStack(JAVA_MYSQL.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    createWorkspace.clickOnCreateWorkspaceButton();

    seleniumWebDriver.switchFromDashboardIframeToIde(60);
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab(LOADER_TIMEOUT_SEC);

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.selectWorkspaceItemName(WORKSPACE);
    workspaces.waitToolbarTitleName(WORKSPACE);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(RUNNING);
    workspaceDetails.clickOnStopWorkspace();
    workspaceDetails.checkStateOfWorkspace(STOPPED);
  }

  private void createMachine(String machineName) {
    // add new machine and check it exists
    workspaceMachines.clickOnAddMachineButton();
    workspaceMachines.checkAddNewMachineDialogIsOpen();
    workspaceMachines.setMachineNameInDialog(machineName);
    workspaceDetails.clickOnAddButtonInDialogWindow();
    workspaceMachines.checkMachineExists(machineName);
  }
}
