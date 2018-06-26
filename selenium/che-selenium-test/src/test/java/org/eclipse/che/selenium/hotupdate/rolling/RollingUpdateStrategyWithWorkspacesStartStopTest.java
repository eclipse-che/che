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
package org.eclipse.che.selenium.hotupdate.rolling;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.annotations.Test;

public class RollingUpdateStrategyWithWorkspacesStartStopTest {
  private static final int TIMEOUT_FOR_ROLLING_UPDATE_FINISH = 100;
  private static final String UPDATE_COMMAND = "rollout latest che";
  private static final String COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE =
      "get dc | grep che | awk '{print $2}'";

  @InjectTestWorkspace(startAfterCreation = false)
  private TestWorkspace workspaceForStarting;

  @Inject private TestWorkspace workspaceForStopping;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private WebDriverWaitFactory webDriverWaitFactory;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;

  @Test
  public void createMavenArchetypeStartProjectByWizard() throws Exception {
    int currentRevision = getRevision();

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

    // execute rolling update command
    executeRollingUpdateCommand();

    // execute stop-start commands for existing workspaces
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);
    workspaces.clickOnWorkspaceStopStartButton(workspaceForStarting.getName());
    workspaces.clickOnWorkspaceStopStartButton(workspaceForStopping.getName());

    // wait successful results of the stop-start requests
    workspaces.waitWorkspaceStatus(workspaceForStopping.getName(), Workspaces.Status.STOPPED);
    workspaces.waitWorkspaceStatus(workspaceForStarting.getName(), Workspaces.Status.RUNNING);

    // check that che is updated
    waitRevision(currentRevision + 1);
  }

  private int getRevision() {
    try {
      return Integer.parseInt(
          openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
  }

  private void waitRevision(int expectedRevision) {
    webDriverWaitFactory
        .get(TIMEOUT_FOR_ROLLING_UPDATE_FINISH)
        .until((ExpectedCondition<Boolean>) driver -> expectedRevision == getRevision());
  }

  private void executeRollingUpdateCommand() throws Exception {
    openShiftCliCommandExecutor.execute(UPDATE_COMMAND);
  }
}
