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
package org.eclipse.che.ide.websocket.impl;

import static java.util.Collections.emptySet;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

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
  private SecurityTokenProvider securityTokenProvider;

  @Inject
  public WebSocketInitializer(
      WebSocketConnectionManager connectionManager,
      WebSocketPropertyManager propertyManager,
      WebSocketActionManager actionManager,
      UrlResolver urlResolver,
      SecurityTokenProvider securityTokenProvider) {
    this.connectionManager = connectionManager;
    this.propertyManager = propertyManager;
    this.actionManager = actionManager;
    this.urlResolver = urlResolver;
    this.securityTokenProvider = securityTokenProvider;
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
    securityTokenProvider
        .getSecurityToken()
        .then(
            new Operation<String>() {
              @Override
              public void apply(String token) throws OperationException {
                String separator = url.contains("?") ? "&" : "?";
                final String secureUrl = url + separator + "token=" + token;

                Log.debug(getClass(), "Initializing with secureUrl: " + secureUrl);

                urlResolver.setMapping(endpointId, secureUrl);

                propertyManager.initializeConnection(secureUrl);

                actionManager.setOnEstablishActions(secureUrl, initActions);

                connectionManager.initializeConnection(secureUrl);
                connectionManager.establishConnection(secureUrl);
              }
            });
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
