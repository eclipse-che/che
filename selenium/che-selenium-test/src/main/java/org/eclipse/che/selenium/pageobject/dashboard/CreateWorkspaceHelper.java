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

  public TestWorkspace createWorkspaceFromDevfileWithProject(
      Devfile devfile, String workspaceName, String projectName) {
    return createWorkspaceFromStack(devfile, workspaceName, ImmutableList.of(projectName), null);
  }

  public TestWorkspace createWorkspaceFromDevfileWithoutProject(
      Devfile devfile, String workspaceName) {
    return createWorkspaceFromStack(devfile, workspaceName, Collections.emptyList(), null);
  }

  public TestWorkspace createWorkspaceFromStackWithProjects(
      Devfile devfile, String workspaceName, List<String> projectNames) {
    return createWorkspaceFromStack(devfile, workspaceName, projectNames, null);
  }

  public TestWorkspace createWorkspaceFromStack(
      Devfile devfile, String workspaceName, List<String> projectNames, Double machineRam) {
    prepareWorkspace(devfile, workspaceName, machineRam);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectNames.forEach(projectSourcePage::selectSample);

    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

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
