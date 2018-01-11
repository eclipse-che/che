/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.core.constant.TestStacksConstants.DOTNET;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.ECLIPSE_CHE;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA_MYSQL;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.PHP;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Serhii Skoryk */
public class CreateWorkspaceTest {

  private final String WORKSPACE_NAME = NameGenerator.generate("workspace", 4);
  private static final String MIN_VALID_WORKSPACE_NAME = NameGenerator.generate("", 3);
  private static final String TOO_SHORT_WORKSPACE_NAME = NameGenerator.generate("", 2);
  private static final String MAX_VALID_WORKSPACE_NAME = NameGenerator.generate("", 100);
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
  @Inject private Workspaces workspaces;

  @BeforeClass
  public void setUp() {
    dashboard.open();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
  }

  @Test
  public void checkWorkspaceName() {
    createWorkspace.typeWorkspaceName(TOO_SHORT_WORKSPACE_NAME);
    assertTrue(createWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    assertFalse(createWorkspace.isCreateWorkspaceButtonEnabled());

    createWorkspace.typeWorkspaceName(TOO_LONG_WORKSPACE_NAME);
    assertTrue(createWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    assertFalse(createWorkspace.isCreateWorkspaceButtonEnabled());

    // type valid names and check that the Create button is enabled
    createWorkspace.typeWorkspaceName(MIN_VALID_WORKSPACE_NAME);
    assertTrue(createWorkspace.isCreateWorkspaceButtonEnabled());
    createWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    assertTrue(createWorkspace.isCreateWorkspaceButtonEnabled());
    createWorkspace.typeWorkspaceName(MAX_VALID_WORKSPACE_NAME);
    assertTrue(createWorkspace.isCreateWorkspaceButtonEnabled());
  }

  @Test
  public void checkMachines() {
    String machineName = "dev-machine";

    // change the RAM number by the increment and decrement buttons
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

    // type number of memory in the RAM field
    createWorkspace.setMachineRAM(machineName, 5.0);
    assertEquals(createWorkspace.getRAM(machineName), 5.0);

    // check the RAM section of the Java-MySql stack(with two machines)
    createWorkspace.selectStack(JAVA_MYSQL.getId());
    assertTrue(createWorkspace.isMachineExists("db"));
    assertTrue(createWorkspace.isMachineExists(machineName));
    createWorkspace.clickOnDecrementMemoryButton(machineName);
    createWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(createWorkspace.getRAM(machineName), 1.5);
    assertEquals(createWorkspace.getRAM("db"), 0.5);
    createWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(createWorkspace.getRAM("db"), 0.5);
    createWorkspace.setMachineRAM(machineName, 100.0);
    createWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(createWorkspace.getRAM(machineName), 100.0);
  }

  @Test
  public void checkFiltersStacksFeature() {

    // filter stacks by 'java' value and check filtered stacks list
    createWorkspace.clickOnAllStacksTab();
    createWorkspace.clickOnFiltersButton();
    createWorkspace.typeToFiltersInput("java");
    createWorkspace.selectFilterSuggestion("JAVA");
    assertTrue(createWorkspace.isStackVisible(JAVA.getId()));
    assertFalse(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clickOnMultiMachineTab();
    assertFalse(createWorkspace.isStackVisible(JAVA.getId()));

    // filter stacks by 'php' value and check filtered stacks list
    createWorkspace.clickOnSingleMachineTab();
    createWorkspace.clickOnFiltersButton();
    createWorkspace.clearSuggestions();
    createWorkspace.typeToFiltersInput("php");
    createWorkspace.selectFilterSuggestion("PHP");
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));

    // filter the Java-MySql stack
    createWorkspace.clickOnAllStacksTab();
    createWorkspace.clickOnFiltersButton();
    createWorkspace.clearSuggestions();
    createWorkspace.typeToFiltersInput("java 1");
    createWorkspace.selectFilterSuggestion("JAVA 1.8, TOMCAT 8, MYSQL 5.7");
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clickOnSingleMachineTab();
    assertFalse(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));

    createWorkspace.clickOnFiltersButton();
    createWorkspace.clearSuggestions();
  }

  @Test
  public void checkSearchStackFeature() {

    // search stacks with 'java' value
    createWorkspace.typeToSearchInput("java");
    createWorkspace.clickOnSingleMachineTab();
    assertTrue(createWorkspace.isStackVisible(JAVA.getId()));
    assertTrue(createWorkspace.isStackVisible(ECLIPSE_CHE.getId()));
    assertFalse(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clickOnAllStacksTab();
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    createWorkspace.clearTextInSearchInput();

    // search stacks with 'php' value
    createWorkspace.typeToSearchInput("php");
    createWorkspace.clickOnQuickStartTab();
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));
    assertFalse(createWorkspace.isStackVisible("php-gae"));
    createWorkspace.clickOnAllStacksTab();
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));
    assertTrue(createWorkspace.isStackVisible("php-gae"));
    createWorkspace.clearTextInSearchInput();

    // search stacks with 'mysql' value
    createWorkspace.typeToSearchInput("mysql");
    createWorkspace.clickOnMultiMachineTab();
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    assertFalse(createWorkspace.isStackVisible(PHP.getId()));
    createWorkspace.clickOnAllStacksTab();
    assertTrue(createWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    assertTrue(createWorkspace.isStackVisible(PHP.getId()));

    // search stacks with 'net' value
    createWorkspace.typeToSearchInput("net");
    assertTrue(createWorkspace.isStackVisible(DOTNET.getId()));
    createWorkspace.clickOnMultiMachineTab();
    assertFalse(createWorkspace.isStackVisible(DOTNET.getId()));

    createWorkspace.clearTextInSearchInput();
  }

  @Test
  public void checkProjectSourcePage() {
    createWorkspace.clickOnQuickStartTab();

    // add a project from the 'web-java-spring' sample
    createWorkspace.selectStack(JAVA.getId());
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();
    projectSourcePage.waitCreatedProjectButton(projectName);
    projectSourcePage.clickOnCreateProjectButton(projectName);

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
    assertEquals(projectSourcePage.getProjectDescription(), newProjectDescription);
    assertEquals(projectSourcePage.getProjectName(), newProjectName);
    projectSourcePage.waitCreatedProjectButton(newProjectName);

    // remove the added project
    projectSourcePage.clickOnRemoveProjectButton();
    assertTrue(projectSourcePage.isProjectNotExists(newProjectName));
  }
}
