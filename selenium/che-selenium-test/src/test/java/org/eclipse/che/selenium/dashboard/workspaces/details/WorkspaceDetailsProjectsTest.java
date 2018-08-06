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
package org.eclipse.che.selenium.dashboard.workspaces.details;

import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.BottomButton.APPLY_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.BottomButton.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.BottomButton.ADD_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.BottomButton.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.UpperButton.SAMPLES_BUTTON;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WorkspaceDetailsProjectsTest {
  private static final String PROJECT_NAME = NameGenerator.generate("wsDetails", 4);
  private static final String SPRING_SAMPLE_NAME = "web-java-spring";
  private static final String CONSOLE_SAMPLE_NAME = "console-java-simple";
  private static final String EXPECTED_SUCCESS_NOTIFICATION = "Success\n" + "Workspace updated.";

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private TestWorkspace testWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceProjectsSamples workspaceProjectsSamples;

  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {

    Path projectPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());
    testProjectServiceClient.importProject(
        testWorkspace.getId(), projectPath, PROJECT_NAME, MAVEN_SPRING);

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitToolbarTitleName();
    workspaces.selectWorkspaceItemName(testWorkspace.getName());
    workspaceDetails.waitToolbarTitleName(testWorkspace.getName());
    workspaceDetails.selectTabInWorkspaceMenu("Projects");
  }

  @Test
  public void mainElementsShouldBePresent() {
    workspaceProjects.waitProjectDetailsPage();

    addSpringSampleToWorkspace();

    checkProjectAppearanceAndButtonsState();

    workspaceProjects.clickOnBottomButton(WorkspaceProjects.BottomButton.CANCEL_BUTTON);
    workspaceProjects.waitProjectIsNotPresent(SPRING_SAMPLE_NAME);

    addSpringSampleToWorkspace();

    checkProjectAppearanceAndButtonsState();

    workspaceProjects.clickOnBottomButton(SAVE_BUTTON);

    workspaceProjects.waitNotification(EXPECTED_SUCCESS_NOTIFICATION);
    workspaceProjects.waitProjectIsPresent(SPRING_SAMPLE_NAME);
  }

  private void addSpringSampleToWorkspace() {
    workspaceProjects.clickOnAddNewProjectButton();
    workspaceProjectsSamples.waitSamplesForm();
    workspaceProjectsSamples.waitTabSelected(SAMPLES_BUTTON);
    workspaceProjectsSamples.waitCheckboxDisabled(SPRING_SAMPLE_NAME, CONSOLE_SAMPLE_NAME);
    workspaceProjectsSamples.waitButtonDisabled(ADD_BUTTON, CANCEL_BUTTON);

    workspaceProjectsSamples.clickOnCheckbox(SPRING_SAMPLE_NAME);
    workspaceProjectsSamples.waitCheckboxEnabled(SPRING_SAMPLE_NAME);
    workspaceProjectsSamples.waitButtonEnabled(ADD_BUTTON, CANCEL_BUTTON);

    workspaceProjectsSamples.clickOnButton(ADD_BUTTON);
  }

  private void checkProjectAppearanceAndButtonsState() {
    workspaceProjects.waitProjectIsPresent(SPRING_SAMPLE_NAME);
    workspaceProjects.waitBottomButtonEnabled(
        APPLY_BUTTON, SAVE_BUTTON, WorkspaceProjects.BottomButton.CANCEL_BUTTON);
  }
}
