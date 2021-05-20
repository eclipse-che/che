/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.hc;

import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * This class is used as {@link ServerChecker} for terminal server as it doesn't have an endpoint
 * that responds with 200 and can be used with default {@link HttpConnectionServerChecker}
 *
 * @author Alexander Garagatyi
 */
// TODO add readiness endpoint to terminal and remove this class
class TerminalHttpConnectionServerChecker extends HttpConnectionServerChecker {
  TerminalHttpConnectionServerChecker(
      URL url,
      String machineName,
      String serverRef,
      long period,
      long timeout,
      int successThreshold,
      TimeUnit timeUnit,
      Timer timer,
      String token) {
    super(url, machineName, serverRef, period, timeout, successThreshold, timeUnit, timer, token);
  }

  @Override
  boolean isConnectionSuccessful(int responseCode) {
    return responseCode == 404;
  }
}
