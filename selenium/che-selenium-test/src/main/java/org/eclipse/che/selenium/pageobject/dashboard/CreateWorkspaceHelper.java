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
package org.eclipse.che.selenium.pageobject.dashboard;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.CheTestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;

/**
 * Class cover creation of workspaces on Dashboard
 *
 * @author Skoryk Serhii
 */
public class CreateWorkspaceHelper {

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CheTestWorkspaceProvider testWorkspaceProvider;

  public void createWorkspaceFromStackWithProject(
      NewWorkspace.Stack stack, String workspaceName, String projectName) {
    prepareWorkspace(stack, workspaceName);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  public TestWorkspace createWorkspaceFromStackWithoutProject(
      NewWorkspace.Stack stack, String workspaceName) throws Exception {
    prepareWorkspace(stack, workspaceName);

    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    return testWorkspaceProvider.getWorkspace(workspaceName, defaultTestUser);
  }

  public void createWorkspaceFromStackWithProjects(
      NewWorkspace.Stack stack, String workspaceName, List<String> projectNames) {
    prepareWorkspace(stack, workspaceName);

    projectSourcePage.clickOnAddOrImportProjectButton();

    projectNames.forEach(
        project -> {
          projectSourcePage.selectSample(project);
        });

    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();
  }

  private void prepareWorkspace(NewWorkspace.Stack stack, String workspaceName) {
    dashboard.waitDashboardToolbarTitle();

    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(stack);
    newWorkspace.typeWorkspaceName(workspaceName);
  }
}
