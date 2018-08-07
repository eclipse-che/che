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
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WorkspaceDetailsProjectsTest {
  private static final String PROJECT_NAME = NameGenerator.generate("wsDetails", 4);
  private static final String PROJECT_FOR_SEARCHING_NAME = NameGenerator.generate("searchWs", 4);
  private static final String SPRING_SAMPLE_NAME = "web-java-spring";
  private static final String CONSOLE_SAMPLE_NAME = "console-java-simple";
  private static final String EXPECTED_SUCCESS_NOTIFICATION = "Success\n" + "Workspace updated.";
  private static final String CHECKING_CHECKBOXES_PROJECT_NAME = "checkboxesProject";
  private static final String CHECKING_CHECKBOXES_NEVEST_PROJECT_NAME = "newestProject";
  private static final String PROJECTS_TAB_NAME = "Projects";
  private static final String EXPECTED_DELETE_BUTTON_MESSAGE =
      "Projects cannot be removed in stopped workspace.";

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestWorkspace checkingCheckboxesWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceProjectsSamples workspaceProjectsSamples;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private ProjectExplorer projectExplorer;

  @BeforeClass
  public void setup() throws Exception {

    Path projectPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());
    testProjectServiceClient.importProject(
        testWorkspace.getId(), projectPath, PROJECT_NAME, MAVEN_SPRING);

    testProjectServiceClient.importProject(
        testWorkspace.getId(), projectPath, PROJECT_FOR_SEARCHING_NAME, MAVEN_SPRING);

    testProjectServiceClient.importProject(
        checkingCheckboxesWorkspace.getId(),
        projectPath,
        CHECKING_CHECKBOXES_PROJECT_NAME,
        MAVEN_SPRING);

    testProjectServiceClient.importProject(
        checkingCheckboxesWorkspace.getId(),
        projectPath,
        CHECKING_CHECKBOXES_NEVEST_PROJECT_NAME,
        MAVEN_SPRING);

    testWorkspaceServiceClient.stop(
        checkingCheckboxesWorkspace.getName(), defaultTestUser.getName());

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitToolbarTitleName();
    workspaces.selectWorkspaceItemName(testWorkspace.getName());
    workspaceDetails.waitToolbarTitleName(testWorkspace.getName());
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS_TAB_NAME);
  }

  @Test
  public void mainElementsShouldBePresent() throws Exception {
    openWoprkspaceDetailsProjectsPage(testWorkspace.getName());

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

  @Test
  public void checkSearchField() throws Exception {
    final String textForSearching = "sea";

    openWoprkspaceDetailsProjectsPage(testWorkspace.getName());

    workspaceProjects.waitSearchField();
    workspaceProjects.typeToSearchField(textForSearching);
    workspaceProjects.waitProjectIsNotPresent(PROJECT_NAME);
    workspaceProjects.waitProjectIsPresent(PROJECT_FOR_SEARCHING_NAME);

    workspaceProjects.typeToSearchField("");
    workspaceProjects.waitProjectIsPresent(PROJECT_NAME);
    workspaceProjects.waitProjectIsPresent(PROJECT_FOR_SEARCHING_NAME);
  }

  @Test(priority = 1)
  public void checkOfCheckboxes() throws Exception {
    openWoprkspaceDetailsProjectsPage(checkingCheckboxesWorkspace.getName());

    workspaceProjects.selectProject(CHECKING_CHECKBOXES_PROJECT_NAME);
    workspaceProjects.waitDeleteButtonDisabling();
    workspaceProjects.waitDeleteButtonMessage(EXPECTED_DELETE_BUTTON_MESSAGE);

    workspaceProjects.selectProject(CHECKING_CHECKBOXES_PROJECT_NAME);
    workspaceProjects.waitDeleteButtonDisappearance();
    workspaceProjects.waitDeleteButtonMessageDisappearance();

    workspaceProjects.clickOnSelectAllCheckbox();
    workspaceProjects.waitCheckboxEnabled(
        CHECKING_CHECKBOXES_PROJECT_NAME, CHECKING_CHECKBOXES_NEVEST_PROJECT_NAME);
    workspaceProjects.waitDeleteButtonDisabling();
    workspaceProjects.waitDeleteButtonMessage(EXPECTED_DELETE_BUTTON_MESSAGE);

    workspaceProjects.clickOnSelectAllCheckbox();
    workspaceProjects.waitCheckboxDisabled(
        CHECKING_CHECKBOXES_PROJECT_NAME, CHECKING_CHECKBOXES_NEVEST_PROJECT_NAME);
    workspaceProjects.waitDeleteButtonMessageDisappearance();
    workspaceProjects.waitDeleteButtonMessageDisappearance();

    workspaceDetails.clickOpenInIdeWsBtn();
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(CHECKING_CHECKBOXES_PROJECT_NAME);
    projectExplorer.waitItem(CHECKING_CHECKBOXES_NEVEST_PROJECT_NAME);
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

    workspaceProjects.waitBottomButtonEnabled(
        WorkspaceProjects.BottomButton.CANCEL_BUTTON, APPLY_BUTTON, SAVE_BUTTON);
    workspaceProjects.waitProjectIsPresent(SPRING_SAMPLE_NAME);
  }

  private void checkProjectAppearanceAndButtonsState() {
    workspaceProjects.waitProjectIsPresent(SPRING_SAMPLE_NAME);
    workspaceProjects.waitBottomButtonEnabled(
        APPLY_BUTTON, SAVE_BUTTON, WorkspaceProjects.BottomButton.CANCEL_BUTTON);
  }

  private void openWoprkspaceDetailsProjectsPage(String workspaceName) throws Exception {
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitPageLoading();
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS_TAB_NAME);
    workspaceProjects.waitProjectDetailsPage();
  }
}
