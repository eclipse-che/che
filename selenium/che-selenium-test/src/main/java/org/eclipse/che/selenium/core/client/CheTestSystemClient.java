/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.api.system.shared.dto.SystemStateDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;

/**
 * Client of workspace master system service.
 *
 * @author Musienko Maxim
 * @author Nochevnov Dmytro
 */
@Singleton
public class CheTestSystemClient {

  @Inject CheTestApiEndpointUrlProvider cheTestApiEndpointUrlProvider;

  @Inject CheTestAdminHttpJsonRequestFactory testUserHttpJsonRequestFactory;

  /**
   * Stops workspace master. Checks on next statuses flow: PREPARING_TO_SHUTDOWN, READY_TO_SHUTDOWN.
   *
   * @throws Exception
   */
  public void stop() throws Exception {
    int maxRequestAttempts = 30;
    int requestTimeoutInSec = 1;

    String restUrlForSuspendingWorkspaces =
        cheTestApiEndpointUrlProvider.get().toString() + "system/stop";

    testUserHttpJsonRequestFactory
        .fromUrl(restUrlForSuspendingWorkspaces)
        .usePostMethod()
        .request();

    waitWorkspaceMasterStatus(
        maxRequestAttempts, requestTimeoutInSec, SystemStatus.PREPARING_TO_SHUTDOWN);

    waitWorkspaceMasterStatus(
        maxRequestAttempts, requestTimeoutInSec, SystemStatus.READY_TO_SHUTDOWN);
  }

  /**
   * Returns workspace master system state, or <b>null</b> if system is inaccessible (suspended).
   */
  @Nullable
  public SystemStatus getStatus() throws Exception {
    String restUrlForGettingSuspendingStatus =
        cheTestApiEndpointUrlProvider.get().toString() + "system/state";

    try {
      return testUserHttpJsonRequestFactory
          .fromUrl(restUrlForGettingSuspendingStatus)
          .useGetMethod()
          .request()
          .asDto(SystemStateDto.class)
          .getStatus();
    } catch (IOException ex) {
      return null;
    }
  }

  public void waitWorkspaceMasterStatus(
      int readStatusAttempts, int readStatusTimeoutInSec, SystemStatus expectedStatus)
      throws Exception {
    int timeToReadStatus = readStatusAttempts * readStatusTimeoutInSec;
    while (readStatusAttempts-- > 0) {
      if (expectedStatus.equals(getStatus())) {
        return;
      }

      WaitUtils.sleepQuietly(readStatusTimeoutInSec);
    }

    throw new IOException(
        String.format(
            "Workspace Master hasn't achieved status '%s' in '%s' seconds.",
            expectedStatus, timeToReadStatus));
  }
}
