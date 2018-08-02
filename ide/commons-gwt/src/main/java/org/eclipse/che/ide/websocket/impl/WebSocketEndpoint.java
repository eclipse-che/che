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

/**
 * Handles WEB SOCKET related events
 *
 * @author Dmitry Kuleshov
 */
public interface WebSocketEndpoint {
  /**
   * Is called when connection is opened
   *
   * @param url url of a web socket where event happened, used as a low level identifier inside web
   *     socket infrastructure
   */
  void onOpen(String url);

  /**
   * Is called when connection is closed
   *
   * @param url url of a web socket where event happened, used as a low level identifier inside web
   *     socket infrastructure
   */
  void onClose(String url);

  /**
   * Is called when connection has errors
   *
   * @param url url of a web socket where event happened, used as a low level identifier inside web
   *     socket infrastructure
   */
  void onError(String url);

  /**
   * Is called when connection receives a text message
   *
   * @param url url of a web socket where event happened, used as a low level identifier inside web
   *     socket infrastructure
   */
  void onMessage(String url, String message);
}
