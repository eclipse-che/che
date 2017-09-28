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

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class WorkspaceDetailsTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final String PROJECT_NAME = "web-java-petclinic";
  private List<String> AgentsList =
      Arrays.asList(
          "C# language server",
          "Exec",
          "File sync",
          "Git credentials",
          "JSON language server",
          "PHP language server",
          "Python language server",
          "SSH",
          "Terminal",
          "TypeScript language server",
          "Workspace API");
  private List<Boolean> AgentsStateList =
      Arrays.asList(false, true, false, false, false, false, false, true, true, false, true);
  private List<String> VariablesList =
      Arrays.asList("MYSQL_DATABASE", "MYSQL_PASSWORD", "MYSQL_ROOT_PASSWORD", "MYSQL_USER");
  private List<String> ValuesList = Arrays.asList("petclinic", "password", "password", "petclinic");

  @Inject private DefaultTestUser defaultTestUser;
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
    dashboard.open();
    createWsFromJavaMySqlStack();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test(priority = 0)
  public void workingWithEnvVariables() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.ENV_VARIABLES);
    dashboardWorkspace.selectMachine("Environment variables", "dev-machine");
    dashboardWorkspace.clickOnAddEnvVariableButton();
    dashboardWorkspace.checkAddNewEnvVarialbleDialogIsOpen();
    dashboardWorkspace.addNewEnvironmentVariable("logi", "qaadmin");
    dashboardWorkspace.clickOnAddDialogButton();
    dashboardWorkspace.checkEnvVariableExists("logi");
    dashboardWorkspace.checkValue("logi", "qaadmin");
    dashboardWorkspace.clickOnEditEnvVariableButton("logi");
    dashboardWorkspace.enterEnvVariableName("login");
    dashboardWorkspace.clickOnUpdateDialogButton();
    dashboardWorkspace.checkValue("login", "qaadmin");
    loader.waitOnClosed();
    dashboardWorkspace.clickOnEnvVariableCheckbox("login");
    dashboardWorkspace.clickOnDeleteBtn();
    dashboardWorkspace.clickOnDeleteDialogButton();
    clickOnSaveButton();

    dashboardWorkspace.selectMachine("Environment variables", "db");
    for (String name : VariablesList) {
      loader.waitOnClosed();
      dashboardWorkspace.clickOnDeleteEnvVariableButton(name);
      dashboardWorkspace.clickOnDeleteDialogButton();
    }
    clickOnSaveButton();

    for (int i = 0; i < VariablesList.size(); i++) {
      loader.waitOnClosed();
      dashboardWorkspace.clickOnAddEnvVariableButton();
      dashboardWorkspace.checkAddNewEnvVarialbleDialogIsOpen();
      dashboardWorkspace.addNewEnvironmentVariable(VariablesList.get(i), ValuesList.get(i));
      dashboardWorkspace.clickOnAddDialogButton();
      dashboardWorkspace.checkEnvVariableExists(VariablesList.get(i));
      dashboardWorkspace.checkValue(VariablesList.get(i), ValuesList.get(i));
    }
    clickOnSaveButton();
  }

  @Test(priority = 1)
  public void workingWithAgents() {
    Boolean b;
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.AGENTS);
    dashboardWorkspace.selectMachine("Workspace Agents", "dev-machine");
    for (String agentName : AgentsList) {
      dashboardWorkspace.checkAgentExists(agentName);
    }

    for (int i = 0; i < AgentsList.size(); i++) {
      b = dashboardWorkspace.getAgentState(AgentsList.get(i));
      Assert.assertEquals(b, AgentsStateList.get(i));
      dashboardWorkspace.switchAgentState(AgentsList.get(i));
    }
    clickOnSaveButton();

    for (String agentName : AgentsList) {
      dashboardWorkspace.switchAgentState(agentName);
    }
    for (int i = 0; i < AgentsList.size(); i++) {
      b = dashboardWorkspace.getAgentState(AgentsList.get(i));
      Assert.assertEquals(b, AgentsStateList.get(i));
    }
    clickOnSaveButton();
  }

  @Test(priority = 2)
  public void workingWithServers() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.SERVERS);
    dashboardWorkspace.selectMachine("Servers", "db");
    dashboardWorkspace.clickOnAddServerButton();
    dashboardWorkspace.waitAddServerDialogIsOpen();
    dashboardWorkspace.enterReference("agen");
    dashboardWorkspace.enterPort("8080");
    dashboardWorkspace.enterProtocol("https");
    dashboardWorkspace.clickOnAddDialogButton();
    dashboardWorkspace.checkServerExists("agen", "8080");
    clickOnSaveButton();
    dashboardWorkspace.clickOnEditServerButton("agen");
    dashboardWorkspace.enterReference("agent");
    dashboardWorkspace.enterPort("80");
    dashboardWorkspace.enterProtocol("http");
    dashboardWorkspace.clickOnUpdateDialogButton();
    dashboardWorkspace.checkServerExists("agent", "80");
    loader.waitOnClosed();
    dashboardWorkspace.clickOnDeleteServerButton("agent");
    dashboardWorkspace.clickOnDeleteDialogButton();
    clickOnSaveButton();
  }

  @Test(priority = 3)
  public void workingWithMachines() {
    String machineName = "new_machine";
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.MACHINES);
    dashboardWorkspace.checkMachineExists("db");
    dashboardWorkspace.checkMachineExists("dev-machine");

    dashboardWorkspace.clickOnAddMachineButton();
    dashboardWorkspace.checkAddNewMachineDialogIsOpen();
    dashboardWorkspace.setMachineNameInDialog(machineName);
    dashboardWorkspace.clickOnAddDialogButton();
    dashboardWorkspace.checkMachineExists(machineName);
    loader.waitOnClosed();
    dashboardWorkspace.clickOnEditMachineButton(machineName);
    dashboardWorkspace.checkEditTheMachineDialogIsOpen();
    dashboardWorkspace.setMachineNameInDialog("machine");
    dashboardWorkspace.clickOnEditDialogButton();
    dashboardWorkspace.checkMachineExists("machine");
    clickOnSaveButton();

    dashboardWorkspace.clickOnAddMachineButton();
    dashboardWorkspace.checkAddNewMachineDialogIsOpen();
    dashboardWorkspace.setMachineNameInDialog(machineName);
    dashboardWorkspace.clickOnAddDialogButton();
    dashboardWorkspace.checkMachineExists(machineName);
    loader.waitOnClosed();
    dashboardWorkspace.clickOnDeleteMachineButton(machineName);
    dashboardWorkspace.clickOnCloseDialogButton();
    loader.waitOnClosed();
    dashboardWorkspace.clickOnDeleteMachineButton(machineName);
    dashboardWorkspace.clickOnDeleteDialogButton();
  }

  @Test(priority = 4)
  public void workingWithProjects() {
    dashboardWorkspace.selectTabInWorspaceMenu(TabNames.PROJECTS);
    dashboardWorkspace.clickOnAddNewProjectButton();
    dashboardWorkspace.selectSample("web-java-petclinic");
    dashboardWorkspace.clickOnAddProjects();
    clickOnSaveButton();
    dashboardWorkspace.checkStateOfWorkspace(DashboardWorkspace.StateWorkspace.RUNNING);
    dashboardWorkspace.checkStateOfWorkspace(DashboardWorkspace.StateWorkspace.STOPPED);
    dashboardProject.waitProjectIsPresent(Template.WEB_JAVA_PETCLINIC.value());

    dashboardWorkspace.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(
        Template.WEB_JAVA_PETCLINIC.value(), PROJECT_FOLDER);
    notificationsPopupPanel.waitPopUpPanelsIsClosed();
    consoles.waitProcessInProcessConsoleTree("machine");
    consoles.waitTabNameProcessIsPresent("machine");
  }

  private void clickOnSaveButton() {
    dashboardWorkspace.clickOnSaveBtn();
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }

  private void createWsFromJavaMySqlStack() {
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
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(
        TestWorkspaceConstants.RUNNING_WORKSPACE_MESS, 240);

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
}
