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
package org.eclipse.che.ide.websocket.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Responsible for keeping connection alive and reconnecting if needed. If connection is closed and
 * sustainer is active it tries to reconnect according to its properties. Default values are:
 *
 * <ul>
 *   <li>reconnection delay: 500 milliseconds
 *   <li>reconnection limit: 5 attempts
 * </ul>
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketConnectionSustainer {
  private static final int RECONNECTION_DELAY = 1_000;
  private static final int RECONNECTION_LIMIT = 5;

  private final WebSocketConnectionManager connectionManager;
  private final WebSocketPropertyManager propertyManager;

  @Inject
  public WebSocketConnectionSustainer(
      WebSocketConnectionManager connectionManager, WebSocketPropertyManager propertyManager) {
    this.connectionManager = connectionManager;
    this.propertyManager = propertyManager;
  }

  public void reset(String url) {
    final int attempts = propertyManager.getReConnectionAttempts(url);

    Log.debug(
        getClass(), "Resetting number of reconnection attempt number. Previous was: " + attempts);

    propertyManager.setReConnectionAttempts(url, 0);
  }

  public void sustain(String url) {
    final int reConnectionAttempts = propertyManager.getReConnectionAttempts(url);

    if (reConnectionAttempts + 1 > RECONNECTION_LIMIT) {
      Log.debug(getClass(), "Exceeding reconnection limit.");

      propertyManager.disableSustainer(url);
    }

    if (propertyManager.sustainerEnabled(url)) {
      Log.debug(
          getClass(), "Sustaining connection. Current attempt number: " + reConnectionAttempts);

      propertyManager.setReConnectionAttempts(url, reConnectionAttempts + 1);
      propertyManager.setConnectionDelay(url, RECONNECTION_DELAY);

      connectionManager.establishConnection(url);
    }
  }
}
