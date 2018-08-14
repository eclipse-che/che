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
package org.eclipse.che.selenium.hotupdate.rolling;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.executor.hotupdate.HotUpdateUtil;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.Test;

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

/** @author Katerina Kanova */
public class RollingUpdateStrategyWithStartedWorkspaceTest {

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

    // check existing of expected workspace and its status
    workspaces.waitPageLoading();
    workspaces.waitWorkspaceIsPresent(workspaceForStopping.getName());
    workspaces.waitWorkspaceStatus(workspaceForStopping.getName(), Workspaces.Status.RUNNING);

    hotUpdateUtil.executeMasterPodUpdateCommand();

    // check that che is updated
    hotUpdateUtil.waitMasterPodRevision(currentRevision + 1);
    hotUpdateUtil.waitFullMasterPodUpdate(currentRevision);

    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);
    workspaces.waitWorkspaceIsPresent(workspaceForStopping.getName());
    workspaces.waitWorkspaceStatus(workspaceForStopping.getName(), Workspaces.Status.RUNNING);
  }
}
