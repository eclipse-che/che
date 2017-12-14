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
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.INSTALLERS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.PROJECTS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.SERVERS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceEnvVariables;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceInstallers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceServers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class WorkspaceDetailsSingleMachineTest {
  private static final String WORKSPACE = NameGenerator.generate("java-mysql", 4);
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private String workspaceName;

  private Map<String, Boolean> installers = new HashMap<>();

  @Inject private TestUser testUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
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
  @Inject private WorkspaceInstallers workspaceInstallers;
  @Inject private WorkspaceEnvVariables workspaceEnvVariables;
  @Inject private MachineTerminal terminal;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    workspaceName = testWorkspace.getName();
    createMaps();
    URL resource =
        WorkspaceDetailsSingleMachineTest.this.getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    dashboard.open();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaces.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(RUNNING);
    workspaceDetails.clickOnStopWorkspace();
    workspaceDetails.checkStateOfWorkspace(STOPPED);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(workspaceName, testUser.getName());
  }

  // @Test
  public void workingWithEnvVariables() {}

  // @Test
  public void workingWithInstallers() {
    workspaceDetails.selectTabInWorkspaceMenu(INSTALLERS);

    // check both versions of the 'Workspace API' installer
    assertTrue(workspaceInstallers.isInstallerStateTurnedOn("Workspace API", "1.0.1"));
    assertFalse(workspaceInstallers.isInstallerStateTurnedOn("Workspace API", "1.0.0"));
    assertTrue(workspaceInstallers.isInstallerStateNotChangeable("Workspace API", "1.0.1"));
    assertTrue(workspaceInstallers.isInstallerStateNotChangeable("Workspace API", "1.0.0"));

    // check all needed installers in dev-machine exist
    workspaceMachines.selectMachine("Workspace Installers", "dev-machine");
    installers.forEach(
        (name, value) -> {
          workspaceInstallers.checkInstallerExists(name);
        });

    // switch all installers and save changes
    installers.forEach(
        (name, value) -> {
          Assert.assertEquals(workspaceInstallers.isInstallerStateTurnedOn(name), value);
          workspaceInstallers.switchInstallerState(name);
          WaitUtils.sleepQuietly(1);
        });
    clickOnSaveButton();

    // switch all installers, save changes and check its states are as previous(by default for the
    // Java-MySql stack)
    installers.forEach(
        (name, value) -> {
          workspaceInstallers.switchInstallerState(name);
          loader.waitOnClosed();
        });
    clickOnSaveButton();
    installers.forEach(
        (name, value) -> {
          Assert.assertEquals(workspaceInstallers.isInstallerStateTurnedOn(name), value);
        });
  }

  // @Test
  public void workingWithServers() {
    workspaceDetails.selectTabInWorkspaceMenu(SERVERS);

    // add a new server to db machine, save changes and check it exists
    workspaceServers.clickOnAddServerButton();
    workspaceServers.waitAddServerDialogIsOpen();
    workspaceServers.enterReference("agen");
    workspaceServers.enterPort("8083");
    workspaceServers.enterProtocol("https");
    workspaceDetails.clickOnAddButtonInDialogWindow();
    clickOnSaveButton();
    workspaceServers.checkServerExists("agen", "8083");

    // edit the server and check it exists
    workspaceServers.clickOnEditServerButton("agen");
    workspaceServers.enterReference("agent");
    workspaceServers.enterPort("83");
    workspaceServers.enterProtocol("http");
    workspaceDetails.clickOnUpdateButtonInDialogWindow();
    workspaceServers.checkServerExists("agent", "83");

    // delete the server and check it is not exist
    workspaceServers.clickOnDeleteServerButton("agent");
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    clickOnSaveButton();
    workspaceServers.checkServerIsNotExists("agent", "83");
  }

  // @Test
  public void workingWithMachines() {

    clickOnSaveButton();
  }

  @Test(priority = 1)
  public void workingWithProjects() {
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS);

    // add new project and cancel adding
    addNewProject(WEB_JAVA_SPRING);
    workspaceDetails.clickOnCancelChangesBtn();
    workspaceProjects.waitProjectIsNotPresent(WEB_JAVA_SPRING);

    // create new projects and save changes
    workspaceProjects.clickOnAddNewProjectButton();
    projectSourcePage.selectSample(WEB_JAVA_SPRING);
    projectSourcePage.selectSample(CONSOLE_JAVA_SIMPLE);
    projectSourcePage.clickOnAddProjectButton();
    clickOnSaveButton();

    // check that project exists(workspace will restart)
    workspaceProjects.waitProjectIsPresent(WEB_JAVA_SPRING);
    workspaceProjects.waitProjectIsPresent(CONSOLE_JAVA_SIMPLE);

    // delete added projects
    workspaceProjects.selectProject(CONSOLE_JAVA_SIMPLE);
    workspaceProjects.selectProject(WEB_JAVA_SPRING);
    workspaceProjects.clickOnDeleteProjectButton();
    workspaceProjects.clickOnDeleteItInDialogWindow();
    workspaceProjects.waitProjectIsNotPresent(CONSOLE_JAVA_SIMPLE);
    workspaceProjects.waitProjectIsNotPresent(WEB_JAVA_SPRING);
    clickOnSaveButton();
    workspaceProjects.waitProjectIsNotPresent(CONSOLE_JAVA_SIMPLE);
    workspaceProjects.waitProjectIsNotPresent(WEB_JAVA_SPRING);

    // start the workspace and check that the new project exists
    workspaceDetails.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME, PROJECT_FOLDER);
  }

  private void addNewProject(String projectName) {
    workspaceProjects.clickOnAddNewProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();
    workspaceProjects.waitProjectIsPresent(projectName);
  }

  private void createMaps() {
    installers.put("C# language server", false);
    installers.put("Exec", false);
    installers.put("File sync", false);
    installers.put("Git credentials", false);
    installers.put("JSON language server", false);
    installers.put("PHP language server", false);
    installers.put("Python language server", false);
    installers.put("Simple Test language server", false);
    installers.put("SSH", false);
    installers.put("Terminal", true);
    installers.put("TypeScript language server", false);
    installers.put("Yaml language server", false);
  }

  private void clickOnSaveButton() {
    workspaceDetails.clickOnSaveChangesBtn();
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }
}
