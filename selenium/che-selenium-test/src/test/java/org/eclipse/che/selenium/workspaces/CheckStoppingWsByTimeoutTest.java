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
package org.eclipse.che.selenium.workspaces;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** TODO rewrite to use che7 workspace */
@Test(groups = UNDER_REPAIR)
public class CheckStoppingWsByTimeoutTest {

  private static int TOASTLOADER_WIDGET_LATENCY_TIMEOUT_IN_MILLISEC = 20000;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestWorkspace testWorkspace;
  @Inject private DefaultTestUser testUser;
  @Inject private Events eventsPanel;

  @Inject
  @Named("che.workspace_agent_dev_inactive_stop_timeout_ms")
  private int cheWorkspaceAgentDevInactiveStopTimeoutMilliseconds;

  @Inject
  @Named("che.workspace.activity_check_scheduler_period_s")
  private int cheWorkspaceActivityCheckSchedulerPeriodInSeconds;

  @Inject TestUserHttpJsonRequestFactory testUserHttpJsonRequestFactory;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    // We should invoke delay without any action for triggering workspace activity checker
    sleepQuietly(getCommonTimeoutInMilliSec(), MILLISECONDS);
  }

  @Test
  public void checkStoppingByApi() throws Exception {
    Workspace workspace =
        workspaceServiceClient.getByName(testWorkspace.getName(), testUser.getName());

    assertEquals(workspace.getStatus(), STOPPED);
  }

  @Test(priority = 1)
  public void checkLoadToasterAfterStopping() {
    toastLoader.waitToastLoaderButton("Start");
  }

  @Test(priority = 2)
  public void checkStopReasonNotification() {
    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage("Workspace idle timeout exceeded");
  }

  private int getCommonTimeoutInMilliSec() {
    return cheWorkspaceAgentDevInactiveStopTimeoutMilliseconds
        + TOASTLOADER_WIDGET_LATENCY_TIMEOUT_IN_MILLISEC
        + cheWorkspaceActivityCheckSchedulerPeriodInSeconds * 1000;
  }
}
