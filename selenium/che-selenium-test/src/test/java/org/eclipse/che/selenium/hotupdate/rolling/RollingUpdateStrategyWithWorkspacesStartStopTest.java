/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.hotupdate.rolling;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.executor.hotupdate.HotUpdateUtil;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class RollingUpdateStrategyWithWorkspacesStartStopTest {

  @InjectTestWorkspace(startAfterCreation = false)
  private TestWorkspace workspaceForStarting;

  @Inject private TestWorkspace workspaceForStopping;
  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private HotUpdateUtil hotUpdateUtil;

  @Test
  public void startStopWorkspaceFunctionsShouldBeAvailableDuringRollingUpdate() throws Exception {
    int currentRevision = hotUpdateUtil.getMasterPodRevision();

    // open 'Workspaces' page
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();

    // check existing of expected workspaces and their statuses
    workspaces.waitPageLoading();
    workspaces.waitWorkspaceIsPresent(workspaceForStopping.getName());
    workspaces.waitWorkspaceIsPresent(workspaceForStarting.getName());
    workspaces.waitWorkspaceStatus(workspaceForStopping.getName(), Workspaces.Status.RUNNING);
    workspaces.waitWorkspaceStatus(workspaceForStarting.getName(), Workspaces.Status.STOPPED);

    hotUpdateUtil.executeMasterPodUpdateCommand();

    // execute stop-start commands for existing workspaces
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);
    workspaces.clickOnWorkspaceStopStartButton(workspaceForStarting.getName());
    workspaces.clickOnWorkspaceStopStartButton(workspaceForStopping.getName());

    // wait successful results of the stop-start requests
    workspaces.waitWorkspaceStatus(workspaceForStopping.getName(), Workspaces.Status.STOPPED);
    workspaces.waitWorkspaceStatus(workspaceForStarting.getName(), Workspaces.Status.RUNNING);

    // check that che is updated
    hotUpdateUtil.waitMasterPodRevision(currentRevision + 1);
    hotUpdateUtil.waitFullMasterPodUpdate(currentRevision);
  }
}
