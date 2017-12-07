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
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.AGENTS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.ENV_VARIABLES;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.MACHINES;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.PROJECTS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.SERVERS;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceAgents;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceEnvVariables;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceServers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class WorkspaceDetailsTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final String PROJECT_NAME = "web-java-petclinic";

  private Map<String, Boolean> agents = new HashMap<>();
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
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private Consoles consoles;
  @Inject private Workspaces workspaces;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private WorkspaceMachines workspaceMachines;
  @Inject private WorkspaceServers workspaceServers;
  @Inject private WorkspaceAgents workspaceAgents;
  @Inject private WorkspaceEnvVariables workspaceEnvVariables;

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
    workspaceDetails.selectTabInWorkspaceMenu(ENV_VARIABLES);

    // create a new variable, save changes and check it exists
    workspaceMachines.selectMachine("Environment variables", "dev-machine");
    workspaceEnvVariables.clickOnAddEnvVariableButton();
    workspaceEnvVariables.checkAddNewEnvVarialbleDialogIsOpen();
    workspaceEnvVariables.addNewEnvironmentVariable("logi", "admin");
    workspaceDetails.clickOnAddButtonInDialogWindow();
    clickOnSaveButton();
    Assert.assertTrue(workspaceEnvVariables.checkEnvVariableExists("logi"));

    // rename the variable, save changes and check it is renamed
    Assert.assertTrue(workspaceEnvVariables.checkValueExists("logi", "admin"));
    workspaceEnvVariables.clickOnEditEnvVariableButton("logi");
    workspaceEnvVariables.enterEnvVariableName("login");
    workspaceDetails.clickOnUpdateButtonInDialogWindow();
    clickOnSaveButton();
    Assert.assertTrue(workspaceEnvVariables.checkValueExists("login", "admin"));

    // delete the variable, save changes and check it is not exists
    workspaceEnvVariables.clickOnEnvVariableCheckbox("login");
    workspaceEnvVariables.clickOnDeleteEnvVariableButton("login");
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    clickOnSaveButton();
    workspaceEnvVariables.checkValueIsNotExists("login", "admin");

    // delete all variable from db machine, check they don't exist and save changes
    workspaceMachines.selectMachine("Environment variables", "db");
    variables.forEach(
        (name, value) -> {
          workspaceEnvVariables.clickOnDeleteEnvVariableButton(name);
          workspaceDetails.clickOnDeleteButtonInDialogWindow();
          workspaceEnvVariables.checkValueIsNotExists(name, value);
        });

    clickOnSaveButton();

    // restore variables to db machine, check they exist and save changes
    variables.forEach(
        (name, value) -> {
          loader.waitOnClosed();
          workspaceEnvVariables.clickOnAddEnvVariableButton();
          workspaceEnvVariables.checkAddNewEnvVarialbleDialogIsOpen();
          workspaceEnvVariables.addNewEnvironmentVariable(name, value);
          workspaceDetails.clickOnAddButtonInDialogWindow();
          Assert.assertTrue(workspaceEnvVariables.checkEnvVariableExists(name));
          Assert.assertTrue(workspaceEnvVariables.checkValueExists(name, value));
        });
    clickOnSaveButton();
  }

  @Test
  public void workingWithAgents() {
    workspaceDetails.selectTabInWorkspaceMenu(AGENTS);

    // check all needed agents in dev-machine exist
    workspaceMachines.selectMachine("Workspace Agents", "dev-machine");
    agents.forEach(
        (name, value) -> {
          workspaceAgents.checkAgentExists(name);
        });

    // switch all agents and save changes
    agents.forEach(
        (name, value) -> {
          Assert.assertEquals(workspaceAgents.getAgentState(name), value);
          workspaceAgents.switchAgentState(name);
          WaitUtils.sleepQuietly(1);
        });
    clickOnSaveButton();

    // switch all agents, save changes and check its states are as previous(by default for the
    // Java-MySql stack)
    agents.forEach(
        (name, value) -> {
          workspaceAgents.switchAgentState(name);
          loader.waitOnClosed();
        });
    clickOnSaveButton();
    agents.forEach(
        (name, value) -> {
          Assert.assertEquals(workspaceAgents.getAgentState(name), value);
        });
  }

  @Test
  public void workingWithServers() {
    workspaceDetails.selectTabInWorkspaceMenu(SERVERS);

    // add a new server to db machine, save changes and check it exists
    workspaceMachines.selectMachine("Servers", "db");
    workspaceServers.clickOnAddServerButton();
    workspaceServers.waitAddServerDialogIsOpen();
    workspaceServers.enterReference("agen");
    workspaceServers.enterPort("8080");
    workspaceServers.enterProtocol("https");
    workspaceDetails.clickOnAddButtonInDialogWindow();
    clickOnSaveButton();
    workspaceServers.checkServerExists("agen", "8080");

    // edit the server and check it exists
    workspaceServers.clickOnEditServerButton("agen");
    workspaceServers.enterReference("agent");
    workspaceServers.enterPort("80");
    workspaceServers.enterProtocol("http");
    workspaceDetails.clickOnUpdateButtonInDialogWindow();
    workspaceServers.checkServerExists("agent", "80");

    // delete the server and check it is not exist
    workspaceServers.clickOnDeleteServerButton("agent");
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    clickOnSaveButton();
    workspaceServers.checkServerIsNotExists("agent", "80");
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
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS);

    // create a new project and save changes
    workspaceProjects.clickOnAddNewProjectButton();
    projectSourcePage.selectSample(PROJECT_NAME);
    projectSourcePage.clickOnAddProjectButton();
    clickOnSaveButton();

    // check that project exists(workspace will restart)
    workspaceProjects.waitProjectIsPresent(WEB_JAVA_PETCLINIC);

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
    agents.put("C# language server", false);
    agents.put("Exec", true);
    agents.put("File sync", false);
    agents.put("Git credentials", false);
    agents.put("JSON language server", false);
    agents.put("PHP language server", false);
    agents.put("Python language server", false);
    agents.put("SSH", true);
    agents.put("Terminal", true);
    agents.put("TypeScript language server", false);
    agents.put("Workspace API", true);
    agents.put("Yaml language server", false);

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
    createWorkspace.selectStack(TestStacksConstants.JAVA_MYSQL.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);
    createWorkspace.clickOnCreateWorkspaceButton();

    seleniumWebDriver.switchFromDashboardIframeToIde(60);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(
        TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);

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
