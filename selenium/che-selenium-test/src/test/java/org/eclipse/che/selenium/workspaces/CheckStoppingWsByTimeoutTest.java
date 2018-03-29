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
package org.eclipse.che.selenium.workspaces;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckStoppingWsByTimeoutTest {

  private static int TOASTLOADER_WIDGET_LATENCY_TIMEOUT_IN_MILLISEC = 20000;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestUser testUser;

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

  @Test
  public void checkLoadToasterAfterStopping() {
    toastLoader.waitStartButtonInToastLoader();
  }

  private int getCommonTimeoutInMilliSec() {
    return cheWorkspaceAgentDevInactiveStopTimeoutMilliseconds
        + TOASTLOADER_WIDGET_LATENCY_TIMEOUT_IN_MILLISEC
        + cheWorkspaceActivityCheckSchedulerPeriodInSeconds * 1000;
  }
}
