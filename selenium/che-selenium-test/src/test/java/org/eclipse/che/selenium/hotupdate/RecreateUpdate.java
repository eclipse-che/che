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
package org.eclipse.che.selenium.hotupdate;

import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RecreateUpdate {
  @Inject CheTestAdminHttpJsonRequestFactory testUserHttpJsonRequestFactory;
  @Inject CheTestApiEndpointUrlProvider cheTestApiEndpointUrlProvider;
  @Inject ProjectExplorer projectExplorer;
  @Inject OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  @Inject private ProcessAgent processAgent;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private CheTerminal terminal;
  @Inject private Menu menu;
  private String cheRevisionBeforeRollout;

  private enum WsMasterStauses {
    RUNNING,
    READY_TO_SHUTDOWN,
    PREPARING_TO_SHUTDOWN;
  }

  @BeforeClass
  public void setUp() throws IOException {
    String ocGettingCurrentRevisionChe = "get dc | grep che | awk '{print $2}'";
    cheRevisionBeforeRollout = openShiftCliCommandExecutor.execute(ocGettingCurrentRevisionChe);
  }

  @Test
  public void checkRecreateUpdateStrategy() throws Exception {
    String ocClientRolloutCommand = "rollout latest che";
    String restUrlForSuspendingWorkspaces =
        cheTestApiEndpointUrlProvider.get().toString() + "system/stop";
    int timeLimitInSecForRecreateingUpdate = 600;
    int delayBetweenRequest = 6;

    ide.open(workspace);
    testUserHttpJsonRequestFactory
        .fromUrl(restUrlForSuspendingWorkspaces)
        .usePostMethod()
        .request();
    checkWorkspaceIsNotAvailable();
    openShiftCliCommandExecutor.execute(ocClientRolloutCommand);
    waitExpectedStatus(
        timeLimitInSecForRecreateingUpdate, delayBetweenRequest, WsMasterStauses.RUNNING);
  }

  private void checkWorkspaceIsNotAvailable() throws Exception {
    int timeLimitForReadyToShutdownStatus = 15;
    int delayBetweenRequestes = 1;

    waitExpectedStatus(
        timeLimitForReadyToShutdownStatus,
        delayBetweenRequestes,
        WsMasterStauses.PREPARING_TO_SHUTDOWN);

    waitExpectedStatus(
        timeLimitForReadyToShutdownStatus,
        delayBetweenRequestes,
        WsMasterStauses.READY_TO_SHUTDOWN);

    projectExplorer.waitProjectExplorerDisappearance(timeLimitForReadyToShutdownStatus);
    terminal.waitTerminalIsNotPresent(timeLimitForReadyToShutdownStatus);
  }

  private void waitExpectedStatus(
      int maxWaitingLimitInSec, int delayBetweenRequestsInSec, WsMasterStauses staus)
      throws Exception {

    // if the limit is not exceeded - do request and check status of the system
    while (maxWaitingLimitInSec > 0) {
      System.out.println(
          "<<<<<<<<<<<getted status: "
              + getCurrentRollingStatus()
              + "eq status: "
              + staus.toString());
      boolean isStatusFinished = getCurrentRollingStatus().equals(staus.toString());

      if (isStatusFinished) {
        break;
      }

      // delay after request and decrement limit
      WaitUtils.sleepQuietly(delayBetweenRequestsInSec);
      maxWaitingLimitInSec -= delayBetweenRequestsInSec;
      // if limit exceeded - something went wrong
      if (maxWaitingLimitInSec <= 0) {
        throw new RuntimeException(
            "The process did not end in the allotted limit or something went wrong with the test environment");
      }
    }
  }

  private String getCurrentRollingStatus() throws Exception {
    String restUrlForGettingSuspendingStatus =
        cheTestApiEndpointUrlProvider.get().toString() + "system/state";

    // get current response code - if system is suspended,  usually we will get 503 response

    try {
      testUserHttpJsonRequestFactory
          .fromUrl(restUrlForGettingSuspendingStatus)
          .useGetMethod()
          .request()
          .getResponseCode();
    } catch (IOException ex) {
      return "SUSPENDED";
    }

    return JsonHelper.parseJson(
            testUserHttpJsonRequestFactory
                .fromUrl(restUrlForGettingSuspendingStatus)
                .useGetMethod()
                .request()
                .asString())
        .getElement("status")
        .getStringValue();
  }
}
