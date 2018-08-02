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

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Manages all connection related high level processes. Acts as a facade to low level web socket
 * components.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketConnectionManager {
  private final WebSocketFactory webSocketFactory;

  private final Map<String, WebSocketConnection> connectionsRegistry = new HashMap<>();

  @Inject
  public WebSocketConnectionManager(WebSocketFactory webSocketFactory) {
    this.webSocketFactory = webSocketFactory;
  }

  /**
   * Initialize a connection. Performs all necessary preparations except for properties management.
   * Must be called before any interactions with a web socket connection.
   *
   * @param url url of a web socket connection that is to be initialized
   */
  public void initializeConnection(String url) {
    connectionsRegistry.put(url, webSocketFactory.create(url));
  }

  /**
   * Establishes a web socket connection to an endpoint defined by a URL parameter
   *
   * @param url url of a web socket connection that is to be established
   */
  public void establishConnection(String url) {
    final WebSocketConnection webSocketConnection = connectionsRegistry.get(url);

    if (webSocketConnection == null) {
      final String error =
          "No connection with url: " + url + "is initialized. Run 'initializedConnection' first.";
      Log.error(getClass(), error);
      throw new IllegalStateException(error);
    }

    webSocketConnection.open();

    Log.debug(getClass(), "Opening connection. Url: " + url);
  }

  /**
   * Close a web socket connection to an endpoint defined by a URL parameter
   *
   * @param url url of a connection to be closed
   */
  public void closeConnection(String url) {
    final WebSocketConnection webSocketConnection = connectionsRegistry.get(url);

    if (webSocketConnection == null) {
      final String warning =
          "Closing connection that is not registered and seem like does not exist";
      Log.warn(getClass(), warning);
      throw new IllegalStateException(warning);
    }

    webSocketConnection.close();

    Log.debug(WebSocketConnectionManager.class, "Closing connection.");
  }

  /**
   * Sends a message to a specified endpoint over web socket connection.
   *
   * @param url url of an endpoint the message is adressed to
   * @param message plain text message
   */
  public void sendMessage(String url, String message) {
    final WebSocketConnection webSocketConnection = connectionsRegistry.get(url);

    if (webSocketConnection == null) {
      final String error =
          "No connection with url: " + url + "is initialized. Run 'initializedConnection' first.";
      Log.error(getClass(), error);
      throw new IllegalStateException(error);
    }

    webSocketConnection.send(message);
    Log.debug(getClass(), "Sending message: " + message);
  }

  /**
   * Checks if a connection is opened at the moment
   *
   * @param url url of a web socket connection to be checked
   * @return connection status: true if opened, false if else
   */
  public boolean isConnectionOpen(String url) {
    final WebSocketConnection webSocketConnection = connectionsRegistry.get(url);

    return webSocketConnection != null && webSocketConnection.isOpen();
  }
}
