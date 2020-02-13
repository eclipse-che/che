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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.hotupdate.HotUpdateUtil;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class RollingUpdateStrategyWithWorkspacesStartStopTest {

  private static final String STARTED_WORKSPACE_NAME =
      generate(RollingUpdateStrategyWithWorkspacesStartStopTest.class.getSimpleName(), 5);
  private static final String STOPPED_WORKSPACE_NAME =
      generate(RollingUpdateStrategyWithWorkspacesStartStopTest.class.getSimpleName(), 5);

  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private HotUpdateUtil hotUpdateUtil;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private DefaultTestUser defaultTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
    createWorkspaceHelper.createAndEditWorkspaceFromStack(
        Devfile.JAVA_MAVEN, STARTED_WORKSPACE_NAME, Collections.emptyList(), null);
    dashboard.open();
    createWorkspaceHelper.createAndStartWorkspaceFromStack(
        Devfile.JAVA_MAVEN, STOPPED_WORKSPACE_NAME, Collections.emptyList(), null);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(STARTED_WORKSPACE_NAME, defaultTestUser.getName());
    workspaceServiceClient.delete(STOPPED_WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void startStopWorkspaceFunctionsShouldBeAvailableDuringRollingUpdate() throws Exception {
    theiaIde.waitOpenedWorkspaceIsReadyToUse();

    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(CONSOLE_JAVA_SIMPLE);
    theiaIde.waitAllNotificationsClosed();

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();

    // check existing of expected workspaces and their statuses
    workspaces.waitPageLoading();
    workspaces.waitWorkspaceIsPresent(STOPPED_WORKSPACE_NAME);
    workspaces.waitWorkspaceIsPresent(STARTED_WORKSPACE_NAME);
    workspaces.waitWorkspaceStatus(STOPPED_WORKSPACE_NAME, Workspaces.Status.RUNNING);
    workspaces.waitWorkspaceStatus(STARTED_WORKSPACE_NAME, Workspaces.Status.STOPPED);

    hotUpdateUtil.executeMasterPodUpdateCommand();
    // check that che is updated
    assertTrue(
        hotUpdateUtil.getRolloutStatus().contains("deployment \"che\" successfully rolled out"));
    WaitUtils.sleepQuietly(60);

    // execute stop-start commands for existing workspaces
    workspaces.clickOnWorkspaceStopStartButton(STOPPED_WORKSPACE_NAME);
    workspaces.waitWorkspaceStatus(STOPPED_WORKSPACE_NAME, Workspaces.Status.STOPPED);
    workspaces.clickOnWorkspaceStopStartButton(STARTED_WORKSPACE_NAME);
    workspaces.waitWorkspaceStatus(STARTED_WORKSPACE_NAME, Workspaces.Status.RUNNING);
  }
}
