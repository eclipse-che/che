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
import com.google.inject.Singleton;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;

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
  @Inject private TheiaIde theiaIde;

  public String createAndStartWorkspace(Devfile devfile) {
    String workspaceName;

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();

    newWorkspace.clickOnGetStartedTab();
    newWorkspace.waitGetStartedTabActive();
    newWorkspace.selectDevfileFromGetStartedList(devfile);

    workspaceName = theiaIde.waitOpenedWorkspaceIsReadyToUse();

    return workspaceName.substring(workspaceName.indexOf("java"), workspaceName.lastIndexOf("?"));
  }
}
