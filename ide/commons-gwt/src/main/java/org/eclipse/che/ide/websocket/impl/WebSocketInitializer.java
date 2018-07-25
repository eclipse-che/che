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
package org.eclipse.che.ide.websocket.impl;

import static java.util.Collections.emptySet;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Contain all routines related to a web socket connection initialization
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketInitializer {
  private final WebSocketConnectionManager connectionManager;
  private final WebSocketPropertyManager propertyManager;
  private final WebSocketActionManager actionManager;
  private final UrlResolver urlResolver;

  @Inject
  public WebSocketInitializer(
      WebSocketConnectionManager connectionManager,
      WebSocketPropertyManager propertyManager,
      WebSocketActionManager actionManager,
      UrlResolver urlResolver) {
    this.connectionManager = connectionManager;
    this.propertyManager = propertyManager;
    this.actionManager = actionManager;
    this.urlResolver = urlResolver;
  }

  /**
   * Initializes a web socket connection, set default values, perform mandatory preparation work.
   *
   * @param endpointId high level identifier of a web socket connection, used by high level service
   *     (e.g. json rpc infrastructure)
   * @param url url of a web socket endpoint
   */
  public void initialize(String endpointId, String url) {
    initialize(endpointId, url, emptySet());
  }

  /**
   * Initializes a web socket connection, set default values, perform mandatory preparation work.
   *
   * @param endpointId high level identifier of a web socket connection, used by high level service
   *     (e.g. json rpc infrastructure)
   * @param url url of a web socket endpoint
   * @param initActions actions to be performed each time the connection is established
   */
  public void initialize(String endpointId, String url, Set<Runnable> initActions) {
    Log.debug(getClass(), "Initializing with url: " + url);

    urlResolver.setMapping(endpointId, url);

    propertyManager.initializeConnection(url);

    actionManager.setOnEstablishActions(url, initActions);

    connectionManager.initializeConnection(url);
    connectionManager.establishConnection(url);
  }

  /**
   * Terminate web socket connection and clean up resources
   *
   * @param endpointId high level identifier of a web socket connection, used by high level service
   *     (e.g. json rpc infrastructure)
   */
  public void terminate(String endpointId) {
    terminate(endpointId, emptySet());
  }

  /**
   * Terminate web socket connection and clean up resources
   *
   * @param endpointId high level identifier of a web socket connection, used by high level service
   *     (e.g. json rpc infrastructure)
   * @param terminateActions actions to be performed each time the connection is terminated
   */
  public void terminate(String endpointId, Set<Runnable> terminateActions) {
    Log.debug(getClass(), "Stopping");

    String url = urlResolver.removeMapping(endpointId);

    propertyManager.disableSustainer(url);

    actionManager.setOnCloseActions(url, terminateActions);

    connectionManager.closeConnection(url);
  }
}
