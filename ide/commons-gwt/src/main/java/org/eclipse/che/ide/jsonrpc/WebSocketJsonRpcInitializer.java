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
package org.eclipse.che.ide.jsonrpc;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.impl.WebSocketInitializer;

/** Web socket based json rpc initializer. */
@Singleton
public class WebSocketJsonRpcInitializer implements JsonRpcInitializer {
  private final WebSocketInitializer webSocketInitializer;

  @Inject
  public WebSocketJsonRpcInitializer(WebSocketInitializer webSocketInitializer) {
    this.webSocketInitializer = webSocketInitializer;
  }

  @Override
  public void initialize(String endpointId, Map<String, String> initProperties) {
    initialize(endpointId, initProperties, emptySet());
  }

  @Override
  public void initialize(
      String endpointId, Map<String, String> initProperties, Set<Runnable> initActions) {
    Log.debug(getClass(), "Initializing with properties: " + initProperties);

    String url = initProperties.get("url");
    webSocketInitializer.initialize(endpointId, url, initActions);
  }

  @Override
  public void terminate(String endpointId) {
    terminate(endpointId, emptyMap(), emptySet());
  }

  @Override
  public void terminate(
      String endpointId, Map<String, String> terminateProperties, Set<Runnable> terminateActions) {
    Log.debug(getClass(), "Terminating");

    webSocketInitializer.terminate(endpointId, terminateActions);
  }
}
