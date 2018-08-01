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
package org.eclipse.che.ide.api.jsonrpc;

import static com.google.gwt.user.client.Window.Location.getHost;
import static com.google.gwt.user.client.Window.Location.getProtocol;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/** Initializes json rpc connection to workspace master */
@Singleton
public class WorkspaceMasterJsonRpcInitializer {
  private final JsonRpcInitializer initializer;
  private final AppContext appContext;
  private final RequestTransmitter requestTransmitter;
  private final SecurityTokenProvider securityTokenProvider;

  @Inject
  public WorkspaceMasterJsonRpcInitializer(
      JsonRpcInitializer initializer,
      AppContext appContext,
      RequestTransmitter requestTransmitter,
      SecurityTokenProvider securityTokenProvider) {
    this.initializer = initializer;
    this.appContext = appContext;
    this.requestTransmitter = requestTransmitter;
    this.securityTokenProvider = securityTokenProvider;
    internalInitialize();
  }

  private static native String getWebsocketContext() /*-{
        if ($wnd.IDE && $wnd.IDE.config) {
            return $wnd.IDE.config.websocketContext;
        } else {
            return null;
        }
    }-*/;

  public void initialize() {
    Log.debug(
        WorkspaceMasterJsonRpcInitializer.class,
        "Initializing JSON RPC websocket connection to workspace master");
    try {
      internalInitialize();
    } catch (Exception e) {
      Log.debug(WorkspaceMasterJsonRpcInitializer.class, "Failed, will try one more time.");
      new Timer() {
        @Override
        public void run() {
          internalInitialize();
        }
      }.schedule(1_000);
    }
  }

  private void internalInitialize() {
    securityTokenProvider
        .getSecurityToken()
        .then(
            token -> {
              String protocol = "https:".equals(getProtocol()) ? "wss://" : "ws://";
              String host = getHost();
              String context = getWebsocketContext();
              String url = protocol + host + context;
              char separator = url.contains("?") ? '&' : '?';
              String queryParams =
                  separator
                      + "token="
                      + token
                      + appContext.getApplicationId().map(id -> "&clientId=" + id).orElse("");
              Set<Runnable> initActions =
                  appContext.getApplicationId().isPresent()
                      ? emptySet()
                      : singleton(WorkspaceMasterJsonRpcInitializer.this::processWsId);

              initializer.initialize(
                  "ws-master", singletonMap("url", url + queryParams), initActions);
            });
  }

  private void processWsId() {
    requestTransmitter
        .newRequest()
        .endpointId("ws-master")
        .methodName("websocketIdService/getId")
        .noParams()
        .sendAndReceiveResultAsString()
        .onSuccess(appContext::setApplicationWebsocketId);
  }

  public void terminate() {
    initializer.terminate("ws-master");
  }
}
