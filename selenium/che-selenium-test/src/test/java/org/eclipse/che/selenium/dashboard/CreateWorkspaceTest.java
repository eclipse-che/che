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

import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
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

  @Test
  public void checkProjectSourcePage() {
    // add a project from the 'web-java-spring' sample
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
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
