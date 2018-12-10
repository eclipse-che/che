/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.BLANK;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CHE_7_PREVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_CENTOS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_THEIA_ON_KUBERNETES;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.utils.WaitUtils;
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
    newWorkspace.waitErrorMessage(WS_NAME_TOO_SHORT);

    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    newWorkspace.typeWorkspaceName(TOO_LONG_WORKSPACE_NAME);
    newWorkspace.waitErrorMessage(WS_NAME_TOO_LONG);
    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    // type valid names and check that the Create button is enabled
    newWorkspace.typeWorkspaceName(MIN_VALID_WORKSPACE_NAME);
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    newWorkspace.typeWorkspaceName(MAX_VALID_WORKSPACE_NAME);
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkMachinesDocker() {
    String machineName = "dev-machine";

    // change the RAM number by the increment and decrement buttons
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(JAVA);
    assertTrue(newWorkspace.isMachineExists(machineName));
    assertEquals(newWorkspace.getRAM(machineName), 2.0);
    newWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 2.1);
    newWorkspace.clickOnDecrementMemoryButton(machineName);

    // we need to wait a little to avoid quick clicking on this button
    WaitUtils.sleepQuietly(1);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    WaitUtils.sleepQuietly(1);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 1.8);

    // type number of memory in the RAM field
    newWorkspace.setMachineRAM(machineName, 5.0);
    assertEquals(newWorkspace.getRAM(machineName), 5.0);

    // check the RAM section of the Java-MySql stack(with two machines)
    newWorkspace.selectStack(JAVA_MYSQL_CENTOS);
    assertTrue(newWorkspace.isMachineExists("db"));
    assertTrue(newWorkspace.isMachineExists(machineName));
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    newWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(newWorkspace.getRAM(machineName), 1.9);
    assertEquals(newWorkspace.getRAM("db"), 0.9);
    newWorkspace.clickOnDecrementMemoryButton("db");
    assertEquals(newWorkspace.getRAM("db"), 0.8);
    newWorkspace.setMachineRAM(machineName, 100.0);
    newWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 100.0);
  }

  @Test(groups = {TestGroup.OPENSHIFT})
  public void checkMachinesOpenshift() {
    String machineName = "dev-machine";

    // change the RAM number by the increment and decrement buttons
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(JAVA);
    assertTrue(newWorkspace.isMachineExists(machineName));
    assertEquals(newWorkspace.getRAM(machineName), 2.0);
    newWorkspace.clickOnIncrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 2.1);
    newWorkspace.clickOnDecrementMemoryButton(machineName);

    // we need to wait a little to avoid quick clicking on this button
    WaitUtils.sleepQuietly(1);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    WaitUtils.sleepQuietly(1);
    newWorkspace.clickOnDecrementMemoryButton(machineName);
    assertEquals(newWorkspace.getRAM(machineName), 1.8);

    // type number of memory in the RAM field
    newWorkspace.setMachineRAM(machineName, 5.0);
    assertEquals(newWorkspace.getRAM(machineName), 5.0);

    // check the RAM section of 'MySQL with Theia IDE on Kubernetes' stack(with two machines)
    newWorkspace.selectStack(JAVA_MYSQL_THEIA_ON_KUBERNETES);
    assertTrue(newWorkspace.isMachineExists("web/mysql"));
    assertTrue(newWorkspace.isMachineExists("web/dev"));
    newWorkspace.clickOnDecrementMemoryButton("web/dev");
    newWorkspace.clickOnDecrementMemoryButton("web/mysql");
    assertEquals(newWorkspace.getRAM("web/dev"), 0.4);
    assertEquals(newWorkspace.getRAM("web/mysql"), 0.2);
    newWorkspace.clickOnDecrementMemoryButton("web/mysql");
    assertEquals(newWorkspace.getRAM("web/mysql"), 0.1);
    newWorkspace.setMachineRAM("web/dev", 100.0);
    newWorkspace.clickOnIncrementMemoryButton("web/dev");
    assertEquals(newWorkspace.getRAM("web/dev"), 100.0);
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkFiltersStacksFeatureDocker() {

    // filter stacks by 'java' value and check filtered stacks list
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.typeToFiltersInput("java");
    newWorkspace.chooseFilterSuggestionByPlusButton("JAVA");
    assertTrue(newWorkspace.isStackVisible(JAVA));
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(JAVA));

    // filter stacks by 'blank' value and check filtered stacks list
    newWorkspace.clickOnSingleMachineTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.clearSuggestions();
    newWorkspace.typeToFiltersInput("blank");
    newWorkspace.chooseFilterSuggestionByPlusButton("BLANK");
    assertTrue(newWorkspace.isStackVisible(BLANK));

    // filter the Java-MySql stack
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.clearSuggestions();
    newWorkspace.typeToFiltersInput("java");
    newWorkspace.chooseFilterSuggestionByPlusButton("JAVA 1.8, TOMCAT 8, MYSQL 5.7");
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL));
    newWorkspace.clickOnSingleMachineTab();
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL));
  }

  @Test(groups = {TestGroup.OPENSHIFT})
  public void checkFiltersStacksFeatureOpenshift() {

    // filter stacks by 'java' value and check filtered stacks list
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.typeToFiltersInput("java");
    newWorkspace.chooseFilterSuggestionByPlusButton("JAVA");
    assertTrue(newWorkspace.isStackVisible(JAVA));
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(JAVA));

    // filter stacks by 'blank' value and check filtered stacks list
    newWorkspace.clickOnSingleMachineTab();
    newWorkspace.clickOnFiltersButton();
    newWorkspace.clearSuggestions();
    newWorkspace.typeToFiltersInput("blank");
    newWorkspace.chooseFilterSuggestionByPlusButton("BLANK");
    assertTrue(newWorkspace.isStackVisible(BLANK));
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkSearchStackFeatureDocker() {

    // search stacks with 'java' value
    newWorkspace.typeToSearchInput("java");
    newWorkspace.clickOnSingleMachineTab();
    assertTrue(newWorkspace.isStackVisible(JAVA));
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL));
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL));
    newWorkspace.clearTextInSearchInput();

    // search stacks with 'mysql' value
    newWorkspace.typeToSearchInput("mysql");
    newWorkspace.clickOnMultiMachineTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL));
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL));

    // search stacks with 'blank' value
    newWorkspace.typeToSearchInput("blank");
    assertTrue(newWorkspace.isStackVisible(BLANK));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(BLANK));

    newWorkspace.clearTextInSearchInput();
  }

  @Test(groups = {TestGroup.OPENSHIFT})
  public void checkSearchStackFeatureOpenshift() {

    // search stacks with 'java' value
    newWorkspace.typeToSearchInput("java");
    newWorkspace.clickOnSingleMachineTab();
    assertTrue(newWorkspace.isStackVisible(JAVA));
    assertFalse(newWorkspace.isStackVisible(JAVA_MYSQL_THEIA_ON_KUBERNETES));
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(JAVA_MYSQL_THEIA_ON_KUBERNETES));
    newWorkspace.clearTextInSearchInput();

    // search stacks with 'mysql' value
    newWorkspace.typeToSearchInput("che 7");
    newWorkspace.clickOnAllStacksTab();
    assertTrue(newWorkspace.isStackVisible(CHE_7_PREVIEW));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(CHE_7_PREVIEW));

    // search stacks with 'blank' value
    newWorkspace.clickOnSingleMachineTab();
    newWorkspace.typeToSearchInput("blank");
    assertTrue(newWorkspace.isStackVisible(BLANK));
    newWorkspace.clickOnMultiMachineTab();
    assertFalse(newWorkspace.isStackVisible(BLANK));

    newWorkspace.clearTextInSearchInput();
  }

  @Test
  public void checkProjectSourcePage() {
    newWorkspace.clickOnAllStacksTab();

    // add a project from the 'web-java-spring' sample
    newWorkspace.selectStack(JAVA);
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
