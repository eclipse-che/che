/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.hc;

import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * Creates {@link TerminalHttpConnectionServerChecker} for terminal server and {@link
 * HttpConnectionServerChecker} for others.
 *
 * @author Alexander Garagatyi
 */
public class ServerCheckerFactoryImpl implements ServerCheckerFactory {
  @Override
  public HttpConnectionServerChecker httpChecker(
      URL url, RuntimeIdentity identity, String machineName, String serverRef, Timer timer) throws InfrastructureException{
    // TODO add readiness endpoint to terminal and remove this
    // workaround needed because terminal server doesn't have endpoint to check it readiness
    if ("terminal".equals(serverRef)) {
      return new TerminalHttpConnectionServerChecker(
          url, machineName, serverRef, 3, 180, TimeUnit.SECONDS, timer);
    }
    // TODO do not hardcode timeouts, use server conf instead
    return new HttpConnectionServerChecker(
        url, machineName, serverRef, 3, 180, TimeUnit.SECONDS, timer);
  }
}
