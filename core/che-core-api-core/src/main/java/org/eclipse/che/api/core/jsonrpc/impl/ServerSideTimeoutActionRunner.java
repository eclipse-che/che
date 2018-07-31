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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.inject.Singleton;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.che.api.core.jsonrpc.commons.TimeoutActionRunner;

@Singleton
public class ServerSideTimeoutActionRunner implements TimeoutActionRunner {

  @Override
  public void schedule(int timeoutInMillis, Runnable runnable) {
    new Timer()
        .schedule(
            new TimerTask() {
              @Override
              public void run() {
                runnable.run();
              }
            },
            timeoutInMillis);
  }
}
