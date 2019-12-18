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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GIT;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.PROJECTS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.BottomButton.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.ActionButton.ADD_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.ActionButton.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.TabButton.GIT_BUTTON;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class WorkspaceDetailsProjectsTest {

  private static final String PROJECT_NAME = "console-java-simple";
  private static final String WORKSPACE_NAME = generate("test-workspace", 4);
  private static final String EXPECTED_SUCCESS_NOTIFICATION = "Success\n" + "Workspace updated.";
  private static final String PROJECTS_CANNOT_BE_REMOVED_IN_STOPPED_WORKSPACE_WARNING =
      "Projects cannot be removed in stopped workspace.";
  private static final String NEW_PROJECT_NAME = "console-java-simple-1";

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceProjectsSamples workspaceProjectsSamples;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setup() {
    dashboard.open();
    createWorkspaceHelper.createAndEditWorkspaceFromStack(
        Devfile.JAVA_MAVEN, WORKSPACE_NAME, Collections.emptyList(), null);
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void mainElementsShouldBePresent() {
    openWorkspaceDetailsProjectsPage(WORKSPACE_NAME);

    addSpringSampleToWorkspace();
    checkProjectAppearanceAndButtonsState();

    // reject added project
    workspaceProjects.waitAndClickOn(WorkspaceProjects.BottomButton.CANCEL_BUTTON);
    workspaceProjects.waitProjectIsNotPresent(NEW_PROJECT_NAME);

    addSpringSampleToWorkspace();
    checkProjectAppearanceAndButtonsState();

    // save added project
    workspaceProjects.waitAndClickOn(SAVE_BUTTON);
    workspaceProjects.waitNotification(EXPECTED_SUCCESS_NOTIFICATION);
    workspaceProjects.waitProjectIsPresent(NEW_PROJECT_NAME);
  }

  @Test(priority = 1)
  public void checkSearchField() {
    final String textForSearching = "simple-1";

    openWorkspaceDetailsProjectsPage(WORKSPACE_NAME);

    // check searching
    workspaceProjects.waitSearchField();
    workspaceProjects.typeToSearchField(textForSearching);
    workspaceProjects.waitProjectIsNotPresent(PROJECT_NAME);
    workspaceProjects.waitProjectIsPresent(NEW_PROJECT_NAME);

    // check restoring of the projects list without any specified search information
    workspaceProjects.typeToSearchField("");
    workspaceProjects.waitProjectIsPresent(PROJECT_NAME);
    workspaceProjects.waitProjectIsPresent(NEW_PROJECT_NAME);
  }

  @Test(priority = 1)
  public void checkOfCheckboxes() {
    openWorkspaceDetailsProjectsPage(WORKSPACE_NAME);

    // check of default checkboxes state
    workspaceProjects.selectProject(PROJECT_NAME);
    workspaceProjects.waitDeleteButtonDisabling();
    workspaceProjects.waitDeleteButtonMessage(
        PROJECTS_CANNOT_BE_REMOVED_IN_STOPPED_WORKSPACE_WARNING);

    // check behavior with one selected checkbox
    workspaceProjects.selectProject(PROJECT_NAME);
    workspaceProjects.waitDeleteButtonDisappearance();
    workspaceProjects.waitDeleteButtonMessageDisappearance();

    // check enabling behavior of the "select all" checkbox
    workspaceProjects.clickOnSelectAllCheckbox();
    workspaceProjects.waitCheckboxEnabled(PROJECT_NAME, NEW_PROJECT_NAME);
    workspaceProjects.waitDeleteButtonDisabling();
    workspaceProjects.waitDeleteButtonMessage(
        PROJECTS_CANNOT_BE_REMOVED_IN_STOPPED_WORKSPACE_WARNING);

    // check disabling behavior of the "select all" checkbox
    workspaceProjects.clickOnSelectAllCheckbox();
    workspaceProjects.waitCheckboxDisabled(PROJECT_NAME, NEW_PROJECT_NAME);
    workspaceProjects.waitDeleteButtonMessageDisappearance();
    workspaceProjects.waitDeleteButtonMessageDisappearance();
  }

  private void addSpringSampleToWorkspace() {
    workspaceProjects.clickOnAddNewProjectButton();
    workspaceProjectsSamples.waitSamplesForm();
    workspaceProjectsSamples.waitTabSelected(GIT_BUTTON);
    workspaceProjectsSamples.waitButtonDisabled(ADD_BUTTON, CANCEL_BUTTON);

    projectSourcePage.selectSourceTab(GIT);
    projectSourcePage.typeGitRepositoryLocation(
        "https://github.com/che-samples/console-java-simple.git");
    projectSourcePage.clickOnAddProjectButton();

    workspaceProjects.waitEnabled(WorkspaceProjects.BottomButton.CANCEL_BUTTON, SAVE_BUTTON);
    workspaceProjects.waitProjectIsPresent(NEW_PROJECT_NAME);
  }

  private void checkProjectAppearanceAndButtonsState() {
    workspaceProjects.waitProjectIsPresent(NEW_PROJECT_NAME);
    workspaceProjects.waitEnabled(SAVE_BUTTON, WorkspaceProjects.BottomButton.CANCEL_BUTTON);
  }

  private void openWorkspaceDetailsProjectsPage(String workspaceName) {
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitPageLoading();
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS);
    workspaceProjects.waitProjectDetailsPage();
  }
}
