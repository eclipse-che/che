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
package org.eclipse.che.selenium.hotupdate.recreate;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.hotupdate.HotUpdateUtil;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.OPENSHIFT, TestGroup.K8S, TestGroup.MULTIUSER})
public class RecreateUpdateStrategyTest {

  private static final String WORKSPACE_NAME =
      generate(RecreateUpdateStrategyTest.class.getSimpleName(), 5);

  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private HotUpdateUtil hotUpdateUtil;
  @Inject private Dashboard dashboard;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;

  private int cheDeploymentBeforeRollout;

  @BeforeClass
  public void setUp() throws Exception {
    cheDeploymentBeforeRollout = hotUpdateUtil.getMasterPodRevision();
    dashboard.open();
    createWorkspaceHelper.createAndStartWorkspaceFromStack(
        Devfile.JAVA_MAVEN, WORKSPACE_NAME, Collections.emptyList(), null);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkRecreateUpdateStrategy() throws Exception {
    int requestAttempts = 100;
    int requestTimeoutInSec = 6;

    // open a user workspace and send request for preparing to shutdown
    theiaIde.waitOpenedWorkspaceIsReadyToUse();
    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(CONSOLE_JAVA_SIMPLE);
    theiaIde.waitAllNotificationsClosed();

    cheTestSystemClient.stop();

    // open the Dashboard and make sure that workspace is not available after suspending system
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    Assert.assertFalse(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_NAME));

    // performs rollout
    hotUpdateUtil.executeMasterPodUpdateCommand();
    cheTestSystemClient.waitWorkspaceMasterStatus(
        requestAttempts, requestTimeoutInSec, SystemStatus.RUNNING);

    // After rollout updating - deployment should be increased on 1
    hotUpdateUtil.waitMasterPodRevision(cheDeploymentBeforeRollout + 1);

    // make sure that workspace exists in workspaces list after updating again
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    workspaces.waitWorkspaceStatus(WORKSPACE_NAME, Status.RUNNING);
  }
}
