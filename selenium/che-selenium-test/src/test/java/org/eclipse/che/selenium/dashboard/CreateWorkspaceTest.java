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

import static org.eclipse.che.selenium.core.constant.TestStacksConstants.ANDROID;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.DOTNET;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA_MYSQL;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.PHP;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateWorkspaceTest {

  private final String WORKSPACE = NameGenerator.generate("workspace", 4);
  private static final String TOO_SHORT_WORKSPACE_NAME = NameGenerator.generate("", 2);
  private static final String TOO_LONG_WORKSPACE_NAME = NameGenerator.generate("", 101);
  private static final String WS_NAME_TOO_SHORT =
      ("The name has to be more than 3 characters long.");
  private static final String WS_NAME_TOO_LONG =
      ("The name has to be less than 100 characters long.");

  private String projectName = WEB_JAVA_SPRING;
  private String newProjectName = projectName + "-1";
  private String projectDescription =
      "A basic example using Spring servlets. The app returns values entered into a submit form.";
  private String newProjectDescription = "This is " + projectDescription;

  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestUser defaultTestUser;
  @Inject private Workspaces workspaces;

  @BeforeClass
  public void setUp() {
    dashboard.open();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void checkWorspaceName() {
    createWorkspace.typeWorkspaceName(TOO_SHORT_WORKSPACE_NAME);
    assertTrue(createWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    assertFalse(createWorkspace.isCreateWorkspaceButtonEnabled());
    createWorkspace.typeWorkspaceName(TOO_LONG_WORKSPACE_NAME);
    WaitUtils.sleepQuietly(1);
    assertTrue(createWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    assertFalse(createWorkspace.isCreateWorkspaceButtonEnabled());
  }

  @Test
  public void checkProjectSourcePage() {
    createWorkspace.clickOnQuickStartTab();

    // add the web-java-spring from the Java stack
    createWorkspace.selectStack(JAVA.getId());
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();
    projectSourcePage.waitCreatedProjectButton(projectName);
    projectSourcePage.clickOnCreatedProjectButton(projectName);

    // change the added project's name and cancel changes
    assertEquals(projectSourcePage.getProjectName(), projectName);
    assertEquals(projectSourcePage.getProjectDescription(), projectDescription);
    projectSourcePage.changeProjectName(newProjectName);
    projectSourcePage.changeProjectDescription(newProjectDescription);
    assertEquals(projectSourcePage.getProjectDescription(), newProjectDescription);
    assertEquals(projectSourcePage.getProjectName(), newProjectName);

    projectSourcePage.clickOnCancelChangesButton();
    assertEquals(projectSourcePage.getProjectName(), projectName);
    assertEquals(projectSourcePage.getProjectDescription(), projectDescription);
    projectSourcePage.waitCreatedProjectButton(projectName);

    // change the added project's name and description
    projectSourcePage.changeProjectName(newProjectName);
    projectSourcePage.changeProjectDescription(newProjectDescription);
    assertEquals(projectSourcePage.getProjectDescription(), newProjectDescription);
    assertEquals(projectSourcePage.getProjectName(), newProjectName);
    projectSourcePage.clickOnSaveChangesButton();
    projectSourcePage.waitCreatedProjectButton(newProjectName);

    // remove the added project
    projectSourcePage.clickOnRemoveProjectButton();
    assertTrue(projectSourcePage.isProjectNotExists(newProjectName));
  }

  @Test
  public void checkMachines() {
    String machineName = "dev-machine";

    createWorkspace.clickOnAllStacksTab();
    createWorkspace.selectStack(JAVA.getId());
    assertTrue(createWorkspace.isMachineExists(machineName));
    assertEquals(createWorkspace.getRAM(machineName), 2.0);
    createWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(createWorkspace.getRAM(machineName), 2.5);
    createWorkspace.clickOnDecrementMemoryButton(machineName);
    createWorkspace.clickOnDecrementMemoryButton(machineName);
    createWorkspace.clickOnDecrementMemoryButton(machineName);
    assertEquals(createWorkspace.getRAM(machineName), 1.0);
    createWorkspace.setMachineRAM(machineName, "5");
    assertEquals(createWorkspace.getRAM(machineName), 5.0);

    createWorkspace.selectStack(JAVA_MYSQL.getId());
    assertTrue(createWorkspace.isMachineExists("db"));
    assertTrue(createWorkspace.isMachineExists(machineName));
    createWorkspace.clickOnDecrementMemoryButton(machineName);
    createWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(createWorkspace.getRAM(machineName), 1.5);
    assertEquals(createWorkspace.getRAM("db"), 0.5);

    createWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(createWorkspace.getRAM("db"), 0.5);
    createWorkspace.setMachineRAM(machineName, "100");
    createWorkspace.clickOnIncrementMemoryButton(machineName);
    createWorkspace.setMachineRAM(machineName, "100");
  }

  @Test
  public void checkFiltersStacksFeature() {
    createWorkspace.clickOnAllStacksTab();
    createWorkspace.clickOnFiltersButton();
    createWorkspace.typeToFiltersInput("java");
    // TODO add locators for suggestions
    createWorkspace.selectFilterSuggestion("JAVA");
    assertTrue(createWorkspace.isStackVisible(JAVA.getId()));
    assertFalse(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clickOnMultiMachineTab();
    assertFalse(createWorkspace.isStackVisible(JAVA.getId()));

    createWorkspace.clickOnSingleMachineTab();
    createWorkspace.clickOnFiltersButton();
    createWorkspace.clearSuggestions();
    createWorkspace.typeToFiltersInput("mysql");
    createWorkspace.selectFilterSuggestion("MYSQL");
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));

    createWorkspace.clickOnMultiMachineTab();
    createWorkspace.clickOnFiltersButton();
    createWorkspace.clearSuggestions();
    createWorkspace.typeToFiltersInput("java");
    createWorkspace.selectFilterSuggestion("MYSQL");
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clickOnSingleMachineTab();
    assertFalse(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));

    createWorkspace.clickOnFiltersButton();
    createWorkspace.clearSuggestions();
  }

  @Test
  public void checkSearchStackFeature() {
    createWorkspace.typeToSearchInput("java");
    createWorkspace.clickOnSingleMachineTab();
    assertTrue(createWorkspace.isStackVisible(JAVA.getId()));
    assertTrue(createWorkspace.isStackVisible(ANDROID.getId()));
    assertTrue(createWorkspace.isStackVisible("che-in-che"));
    assertFalse(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clickOnAllStacksTab();
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clearTextInSearchInput();

    createWorkspace.typeToSearchInput("php");
    createWorkspace.clickOnQuickStartTab();
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));
    assertFalse(createWorkspace.isStackVisible("php-gae"));
    createWorkspace.clickOnAllStacksTab();
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));
    assertTrue(createWorkspace.isStackVisible("php-gae"));
    createWorkspace.clearTextInSearchInput();

    createWorkspace.typeToSearchInput("mysql");
    createWorkspace.clickOnMultiMachineTab();
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    assertFalse(createWorkspace.isStackVisible(PHP.getId()));
    createWorkspace.clickOnAllStacksTab();
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));

    createWorkspace.typeToSearchInput("net");
    assertTrue(createWorkspace.isStackVisible(DOTNET.getId()));
    createWorkspace.clickOnMultiMachineTab();
    assertFalse(createWorkspace.isStackVisible(DOTNET.getId()));

    createWorkspace.clearTextInSearchInput();
  }
}
