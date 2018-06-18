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
package org.eclipse.che.selenium.core.client;

import static org.eclipse.che.selenium.core.client.CheTestSystemClient.WsMasterStatus.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;

/** @author Musienko Maxim */
@Singleton
public class CheTestSystemClient {

  @Inject CheTestApiEndpointUrlProvider cheTestApiEndpointUrlProvider;

  @Inject CheTestAdminHttpJsonRequestFactory testUserHttpJsonRequestFactory;

  public enum WsMasterStatus {
    RUNNING,
    READY_TO_SHUTDOWN,
    PREPARING_TO_SHUTDOWN,
    SUSPENDED;
  }

  /**
   * Prepare ws master to stopping. It means sending sopping request as admin and checking that next
   * statuses (PREPARING_TO_SHUTDOWN, READY_TO_SHUTDOWN) passed as well
   *
   * @throws Exception
   */
  public void prepareToStopping() throws Exception {
    int timeLimitForReadyToShutdownStatus = 30;
    int delayBetweenRequestes = 1;

    String restUrlForSuspendingWorkspaces =
        cheTestApiEndpointUrlProvider.get().toString() + "system/stop";

    testUserHttpJsonRequestFactory
        .fromUrl(restUrlForSuspendingWorkspaces)
        .usePostMethod()
        .request();

    waitWorkspaceMasterStatus(
        timeLimitForReadyToShutdownStatus, delayBetweenRequestes, PREPARING_TO_SHUTDOWN);

    waitWorkspaceMasterStatus(
        timeLimitForReadyToShutdownStatus, delayBetweenRequestes, READY_TO_SHUTDOWN);
  }

  public String getCurrentState() throws Exception {
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
      return SUSPENDED.toString();
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

  private void waitWorkspaceMasterStatus(
      int maxReadStatusAttempts, int readStatusTimeoutInSec, WsMasterStatus expectedStatus)
      throws Exception {
    int readStatusAttempts = maxReadStatusAttempts;
    while (readStatusAttempts-- > 0) {
      if (getCurrentState().equals(expectedStatus.toString())) {
        return;
      }

      WaitUtils.sleepQuietly(readStatusTimeoutInSec);
    }

    throw new IOException(
        String.format(
            "Workspace Master hasn't achieved status '%s' in '%' seconds.",
            expectedStatus, maxReadStatusAttempts * readStatusTimeoutInSec));
  }
}
