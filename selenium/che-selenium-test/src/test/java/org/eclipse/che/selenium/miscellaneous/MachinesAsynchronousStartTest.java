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
package org.eclipse.che.selenium.miscellaneous;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status.RUNNING;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test(groups = OPENSHIFT)
public class MachinesAsynchronousStartTest {
  private static final String GET_POD_NAME_COMMAND_COMMAND_TEMPLATE =
      "get pod --no-headers=true -l che.workspace_id=%s | awk '{print $1}'";
  private static final String GET_POD_RELATED_EVENTS_COMMAND_TEMPLATE =
      "get events --no-headers=true --field-selector involvedObject.name=%s | awk '{print $7 \" \" $8}'";

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  @Inject private WebDriverWaitFactory webDriverWaitFactory;

  private TestWorkspace brokenWorkspace;

  @AfterClass
  public void cleanUp() throws Exception {
    testWorkspaceServiceClient.delete(brokenWorkspace.getName(), defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaces() throws Exception {
    // prepare
    dashboard.open();

    // create and start broken workspace
    brokenWorkspace = createBrokenWorkspace();
    startBrokenWorkspaceAndWaitRunningStatus();

    // check that broken workspace is displayed with "Running" status
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitPageLoading();
    waitWorkspaceRunningStatusOnDashboard();

    // check openshift events log
    waitEvent("Failed");
    waitEvent("BackOff");
  }

  private void startBrokenWorkspaceAndWaitRunningStatus() throws Exception {
    try {
      testWorkspaceServiceClient.start(
          brokenWorkspace.getName(), brokenWorkspace.getId(), defaultTestUser);
    } catch (Exception ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/10295", ex);
    }
  }

  private void waitWorkspaceRunningStatusOnDashboard() throws Exception {
    try {
      workspaces.waitWorkspaceStatus(brokenWorkspace.getName(), RUNNING);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/10295", ex);
    }
  }

  private TestWorkspace createBrokenWorkspace() throws Exception {
    return testWorkspaceProvider.createWorkspace(
        defaultTestUser, 2, WorkspaceTemplate.BROKEN, true);
  }

  private String getPodName() throws Exception {
    String command = format(GET_POD_NAME_COMMAND_COMMAND_TEMPLATE, brokenWorkspace.getId());
    return openShiftCliCommandExecutor.execute(command);
  }

  private List<String> getPodRelatedEvents() throws Exception {
    String command = format(GET_POD_RELATED_EVENTS_COMMAND_TEMPLATE, getPodName());
    String events = openShiftCliCommandExecutor.execute(command);
    return asList(events.split("[\\ \\n]"));
  }

  private boolean eventIsPresent(String event) {
    try {
      return getPodRelatedEvents().contains(event);
    } catch (Exception e) {
      throw new RuntimeException("Fail of events logs reading", e);
    }
  }

  private void waitEvent(String event) {
    final int timeoutInSeconds = 12;
    final int delayBetweenRequestsInSeconds = 2;

    webDriverWaitFactory
        .get(timeoutInSeconds, delayBetweenRequestsInSeconds)
        .until((ExpectedCondition<Boolean>) driver -> eventIsPresent(event));
  }
}
