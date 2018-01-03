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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
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
@Test(groups = {TestGroup.DOCKER})
public class WorkspaceDetailsComposeTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final ImmutableMap<String, String> EXPECTED_VARIABLES =
      ImmutableMap.of(
          "MYSQL_DATABASE", "petclinic",
          "MYSQL_PASSWORD", "password",
          "MYSQL_ROOT_PASSWORD", "password",
          "MYSQL_USER", "petclinic");

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
    createWsFromJavaMySqlStack();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, testUser.getName());
  }

  public void workingWithEnvVariables() {
    workspaceDetails.selectTabInWorkspaceMenu(ENV_VARIABLES);

    // create a new variable, save changes and check it exists
    workspaceMachines.selectMachine("Environment variables", "dev-machine");
    createVariable("logi", "admin");
    clickOnSaveButton();
    assertTrue(workspaceEnvVariables.checkEnvVariableExists("logi"));

    // rename the variable, save changes and check it is renamed
    assertTrue(workspaceEnvVariables.checkValueExists("logi", "admin"));
    workspaceEnvVariables.clickOnEditEnvVariableButton("logi");
    workspaceEnvVariables.enterEnvVariableName("login");
    workspaceDetails.clickOnUpdateButtonInDialogWindow();
    clickOnSaveButton();
    assertTrue(workspaceEnvVariables.checkEnvVariableExists("login"));
    assertTrue(workspaceEnvVariables.checkValueExists("login", "admin"));

    // delete the variable, save changes and check it is not exists
    deleteVariable("login", "admin");
    clickOnSaveButton();
    workspaceEnvVariables.checkValueIsNotExists("login", "admin");

    workspaceMachines.selectMachine("Environment variables", "db");
    // add variables to 'db' machine, check they exist and save changes
    EXPECTED_VARIABLES.forEach(
        (name, value) -> {
          WaitUtils.sleepQuietly(1);
          createVariable(name, value);
          assertTrue(workspaceEnvVariables.checkEnvVariableExists(name));
          assertTrue(workspaceEnvVariables.checkValueExists(name, value));
        });
    clickOnSaveButton();

    // delete all variables from the 'db' machine, check they don't exist and save changes
    EXPECTED_VARIABLES.forEach(
        (name, value) -> {
          deleteVariable(name, value);
        });

    clickOnSaveButton();
  }

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
    WaitUtils.sleepQuietly(1);
    workspaceMachines.clickOnDeleteMachineButton(machineName);
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaceMachines.checkMachineIsNotExists(machineName);

    // create a new machine, edit(change the name) and save changes
    createMachine(machineName);
    workspaceMachines.clickOnEditMachineButton(machineName);
    workspaceMachines.checkEditTheMachineDialogIsOpen();
    workspaceMachines.setMachineNameInDialog("machine");
    workspaceMachines.clickOnSaveNameDialogButton();
    workspaceMachines.checkMachineExists("machine");
    clickOnSaveButton();
    workspaceMachines.checkMachineExists("machine");
  }

  @Test(priority = 1)
  public void startWorkspaceAndCheckChanges() {
    // check that created machine exists in the Process Console tree
    workspaceDetails.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();

    terminal.waitTerminalTab(LOADER_TIMEOUT_SEC);
    consoles.waitProcessInProcessConsoleTree("machine");
    consoles.waitTabNameProcessIsPresent("machine");
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

  private void createVariable(String varName, String varValue) {
    workspaceEnvVariables.clickOnAddEnvVariableButton();
    workspaceEnvVariables.checkAddNewEnvVarialbleDialogIsOpen();
    workspaceEnvVariables.addNewEnvironmentVariable(varName, varValue);
    workspaceDetails.clickOnAddButtonInDialogWindow();
  }

  private void deleteVariable(String varName, String varValue) {
    workspaceEnvVariables.clickOnDeleteEnvVariableButton(varName);
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaceEnvVariables.checkValueIsNotExists(varName, varValue);
  }
}
