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

import static java.lang.Integer.*;
import static org.eclipse.che.selenium.hotupdate.RecreateUpdateStrategyTest.WsMasterStatus.*;
import static org.testng.Assert.*;

import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
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

@Test(groups = {TestGroup.OPENSHIFT, TestGroup.MULTIUSER})
public class RecreateUpdateStrategyTest {
  private static final String COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE =
      "get dc | grep che | awk '{print $2}'";

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

  private int cheDeploymentBeforeRollout;

  protected enum WsMasterStatus {
    RUNNING,
    READY_TO_SHUTDOWN,
    PREPARING_TO_SHUTDOWN,
    SUSPENDED;
  }

  @BeforeClass
  public void setUp() throws IOException {

    cheDeploymentBeforeRollout =
        parseInt(openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
  }

  @Test
  public void checkRecreateUpdateStrategy() throws Exception {
    String ocClientRolloutCommand = "rollout latest che";
    String restUrlForSuspendingWorkspaces =
        cheTestApiEndpointUrlProvider.get().toString() + "system/stop";
    int timeLimitInSecForRecreatingUpdate = 600;
    int delayBetweenRequests = 6;

    // open a user workspace and send request for preparing to shutdown
    ide.open(workspace);

    prepareToShutdownRequest(restUrlForSuspendingWorkspaces);

    // make sure, that system is prepared to  shutdown and than ready to shutdown
    checkExpectedStatusesAndWorkspaceAfterShutDowning();

    // performs rollout
    openShiftCliCommandExecutor.execute(ocClientRolloutCommand);
    waitWorkspaceMasterStatus(timeLimitInSecForRecreatingUpdate, delayBetweenRequests, RUNNING);

    // get current version of deployment after rollout
    int cheDeploymentAfterRollout =
        parseInt(openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
    // After rollout updating - deployment should be increased on 1. So we previews version +1
    // should be equal current
    assertEquals(cheDeploymentAfterRollout, cheDeploymentBeforeRollout + 1);

    // make sure that CHE ide is available after updating again
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  private void prepareToShutdownRequest(String restUrlForSuspendingWorkspaces)
      throws IOException, ServerException, UnauthorizedException, ForbiddenException,
          NotFoundException, ConflictException, BadRequestException {
    testUserHttpJsonRequestFactory
        .fromUrl(restUrlForSuspendingWorkspaces)
        .usePostMethod()
        .request();
  }

  private void checkExpectedStatusesAndWorkspaceAfterShutDowning() throws Exception {
    int timeLimitForReadyToShutdownStatus = 30;
    int delayBetweenRequestes = 1;

    waitWorkspaceMasterStatus(
        timeLimitForReadyToShutdownStatus,
        delayBetweenRequestes,
        WsMasterStatus.PREPARING_TO_SHUTDOWN);

    waitWorkspaceMasterStatus(
        timeLimitForReadyToShutdownStatus, delayBetweenRequestes, WsMasterStatus.READY_TO_SHUTDOWN);

    // reopen the workspace and make sure that this one is not available after suspending system
    ide.open(workspace);
    projectExplorer.waitProjectExplorerDisappearance(timeLimitForReadyToShutdownStatus);
    terminal.waitTerminalIsNotPresent(timeLimitForReadyToShutdownStatus);
  }

  private void waitWorkspaceMasterStatus(
      int maxReadStatusAttempts, int delayBetweenRequestsInSec, WsMasterStatus expectedStatus)
      throws Exception {
    int readStatusAttempts = maxReadStatusAttempts;
    // if the limit is not exceeded - do request and check status of the system
    while (readStatusAttempts-- > 0) {
      if (getCurrentRollingStatus().equals(expectedStatus.toString())) {
        break;
      }

      // delay if expected status has been not achieved and decrement limit
      WaitUtils.sleepQuietly(delayBetweenRequestsInSec);

      // if the limit has exceeded and we have not achieved expected status - something went wrong
      if (maxReadStatusAttempts <= 0) {
        throw new IOException(
            String.format(
                "Workspace Master hasn't achieved status '%s' in '%' seconds.",
                maxReadStatusAttempts, maxReadStatusAttempts * maxReadStatusAttempts));
      }
    }
  }

  private String getCurrentRollingStatus() throws Exception {
    String restUrlForGettingSuspendingStatus =
        cheTestApiEndpointUrlProvider.get().toString() + "system/state";

    // get current response code - if system is suspended,  we will get IO exception from the
    // server. This mean that we have suspended status
    try {
      testUserHttpJsonRequestFactory
          .fromUrl(restUrlForGettingSuspendingStatus)
          .useGetMethod()
          .request()
          .getResponseCode();
    } catch (IOException ex) {
      return WsMasterStatus.SUSPENDED.toString();
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
