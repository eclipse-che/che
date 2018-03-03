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
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private Workspaces workspaces;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @BeforeMethod
  private void openNewWorkspacePage() {
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
  }

  @Test
  public void checkWorkspaceName() {
    newWorkspace.typeWorkspaceName(TOO_SHORT_WORKSPACE_NAME);
    assertTrue(newWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    assertFalse(newWorkspace.isCreateWorkspaceButtonEnabled());

    newWorkspace.typeWorkspaceName(TOO_LONG_WORKSPACE_NAME);
    assertTrue(newWorkspace.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    assertFalse(newWorkspace.isCreateWorkspaceButtonEnabled());

    // type valid names and check that the Create button is enabled
    newWorkspace.typeWorkspaceName(MIN_VALID_WORKSPACE_NAME);
    assertTrue(newWorkspace.isCreateWorkspaceButtonEnabled());
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    assertTrue(newWorkspace.isCreateWorkspaceButtonEnabled());
    newWorkspace.typeWorkspaceName(MAX_VALID_WORKSPACE_NAME);
    assertTrue(newWorkspace.isCreateWorkspaceButtonEnabled());
  }

  @Test
  public void checkMachines() {
    String machineName = "dev-machine";

    // change the RAM number by the increment and decrement buttons
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(JAVA.getId());
    assertTrue(newWorkspace.isMachineExists(machineName));
    assertEquals(newWorkspace.getRAM(machineName), 2.0);
    newWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 2.5);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 1.0);

    // type number of memory in the RAM field
    newWorkspace.setMachineRAM(machineName, 5.0);
    assertEquals(newWorkspace.getRAM(machineName), 5.0);

    // check the RAM section of the Java-MySql stack(with two machines)
    newWorkspace.selectStack(JAVA_MYSQL.getId());
    assertTrue(newWorkspace.isMachineExists("db"));
    assertTrue(newWorkspace.isMachineExists(machineName));
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    newWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(newWorkspace.getRAM(machineName), 1.5);
    assertEquals(newWorkspace.getRAM("db"), 0.5);
    newWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(newWorkspace.getRAM("db"), 0.5);
    newWorkspace.setMachineRAM(machineName, 100.0);
    newWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 100.0);
  }

  @Test
  public void checkFiltersStacksFeature() {

    // filter stacks by 'java' value and check filtered stacks list
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.typeToFiltersInput("java");
    newWorkspace.selectFilterSuggestion("JAVA");
    assertTrue(newWorkspace.isStackVisible(JAVA.getId()));
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(JAVA.getId()));

    // filter stacks by 'php' value and check filtered stacks list
    newWorkspace.clickOnSingleMachineTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.clearSuggestions();
    newWorkspace.typeToFiltersInput("php");
    newWorkspace.selectFilterSuggestion("PHP");
    assertTrue(newWorkspace.isStackVisible(PHP.getId()));

    // filter the Java-MySql stack
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.clearSuggestions();
    newWorkspace.typeToFiltersInput("java 1");
    newWorkspace.selectFilterSuggestion("JAVA 1.8, TOMCAT 8, MYSQL 5.7");
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    newWorkspace.clickOnSingleMachineTab();
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));

    newWorkspace.clickOnFiltersButton();
    newWorkspace.clearSuggestions();
  }

  @Test
  public void checkSearchStackFeature() {

    // search stacks with 'java' value
    newWorkspace.typeToSearchInput("java");
    newWorkspace.clickOnSingleMachineTab();
    assertTrue(newWorkspace.isStackVisible(JAVA.getId()));
    assertTrue(newWorkspace.isStackVisible(ECLIPSE_CHE.getId()));
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    newWorkspace.clearTextInSearchInput();

    // search stacks with 'php' value
    newWorkspace.typeToSearchInput("php");
    newWorkspace.clickOnQuickStartTab();
    assertTrue(newWorkspace.isStackVisible(PHP.getId()));
    assertFalse(newWorkspace.isStackVisible("php-gae"));
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(PHP.getId()));
    assertTrue(newWorkspace.isStackVisible("php-gae"));
    newWorkspace.clearTextInSearchInput();

    // search stacks with 'mysql' value
    newWorkspace.typeToSearchInput("mysql");
    newWorkspace.clickOnMultiMachineTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    assertFalse(newWorkspace.isStackVisible(PHP.getId()));
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL.getId()));
    assertTrue(newWorkspace.isStackVisible(PHP.getId()));

    // search stacks with 'net' value
    newWorkspace.typeToSearchInput("net");
    assertTrue(newWorkspace.isStackVisible(DOTNET.getId()));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(DOTNET.getId()));

    newWorkspace.clearTextInSearchInput();
  }

  @Test
  public void checkProjectSourcePage() {
    newWorkspace.clickOnQuickStartTab();

    // add a project from the 'web-java-spring' sample
    newWorkspace.selectStack(JAVA.getId());
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
