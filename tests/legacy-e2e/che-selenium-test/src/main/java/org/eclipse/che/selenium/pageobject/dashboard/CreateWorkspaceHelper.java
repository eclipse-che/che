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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;

/**
 * Class cover creation of workspaces on Dashboard
 *
 * @author Skoryk Serhii
 */
@Singleton
public class CreateWorkspaceHelper {

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private WorkspaceDetails workspaceDetails;

  public TestWorkspace createWorkspaceFromDevfileWithProject(
      Devfile devfile, String workspaceName, String projectName) {
    return createAndStartWorkspaceFromStack(
        devfile, workspaceName, ImmutableList.of(projectName), null);
  }

  public TestWorkspace createWorkspaceFromDevfileWithoutProject(
      Devfile devfile, String workspaceName) {
    return createAndStartWorkspaceFromStack(devfile, workspaceName, Collections.emptyList(), null);
  }

  public TestWorkspace createAndStartWorkspaceFromStack(
      Devfile devfile, String workspaceName, List<String> projectNames, Double machineRam) {
    prepareWorkspace(devfile, workspaceName, machineRam);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectNames.forEach(projectSourcePage::selectSample);

    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    return testWorkspaceProvider.getWorkspace(workspaceName, defaultTestUser);
  }

  public TestWorkspace createAndEditWorkspaceFromStack(
      Devfile devfile, String workspaceName, List<String> projectNames, Double machineRam) {
    prepareWorkspace(devfile, workspaceName, machineRam);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectNames.forEach(projectSourcePage::selectSample);

    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndEditWorkspace();

    workspaceDetails.waitToolbarTitleName(workspaceName);

    return testWorkspaceProvider.getWorkspace(workspaceName, defaultTestUser);
  }

  private void prepareWorkspace(Devfile devfile, String workspaceName, Double machineRam) {
    dashboard.waitDashboardToolbarTitle();

    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.selectDevfile(devfile);
    newWorkspace.typeWorkspaceName(workspaceName);
  }
}
