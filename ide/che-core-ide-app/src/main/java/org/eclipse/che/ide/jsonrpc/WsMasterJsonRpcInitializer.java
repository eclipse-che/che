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
package org.eclipse.che.ide.jsonrpc;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STATUS_CHANGED_METHOD;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_MASTER_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.jsonrpc.SubscriptionManagerClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

/** Initializes JSON-RPC connection to the workspace master. */
@Singleton
public class WsMasterJsonRpcInitializer {

  private final JsonRpcInitializer initializer;
  private final RequestTransmitter requestTransmitter;
  private final AppContext appContext;
  private final SubscriptionManagerClient subscriptionManagerClient;

  @Inject
  public WsMasterJsonRpcInitializer(
      JsonRpcInitializer initializer,
      RequestTransmitter requestTransmitter,
      AppContext appContext,
      EventBus eventBus,
      SubscriptionManagerClient subscriptionManagerClient) {
    this.initializer = initializer;
    this.requestTransmitter = requestTransmitter;
    this.appContext = appContext;
    this.subscriptionManagerClient = subscriptionManagerClient;

    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> initialize());
    eventBus.addHandler(WorkspaceStartingEvent.TYPE, e -> initialize());
    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          unsubscribeFromEvents();
          terminate();
        });
  }

  private void initialize() {
    WorkspaceImpl workspace = appContext.getWorkspace();
    String url = workspace.getLinks().get(LINK_REL_ENVIRONMENT_STATUS_CHANNEL);

    if (workspace.getStatus() == STOPPED || url == null) {
      return;
    }

    String separator = url.contains("?") ? "&" : "?";
    Optional<String> appWebSocketId = appContext.getApplicationWebsocketId();
    String queryParams = appWebSocketId.map(id -> separator + "clientId=" + id).orElse("");
    String wsMasterEndpointURL = url + queryParams;

    Map<String, String> initProperties = singletonMap("url", wsMasterEndpointURL);

    Set<Runnable> initActions = new HashSet<>();
    initActions.add(this::subscribeToEvents);

    if (!appWebSocketId.isPresent()) {
      initActions.add(this::processWsId);
    }

    initializer.initialize(WS_MASTER_JSON_RPC_ENDPOINT_ID, initProperties, initActions);
  }

  private void processWsId() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_MASTER_JSON_RPC_ENDPOINT_ID)
        .methodName("websocketIdService/getId")
        .noParams()
        .sendAndReceiveResultAsString()
        .onSuccess(appContext::setApplicationWebsocketId);
  }

  private void terminate() {
    initializer.terminate(WS_MASTER_JSON_RPC_ENDPOINT_ID);
  }

  private void subscribeToEvents() {
    Map<String, String> scope = singletonMap("workspaceId", appContext.getWorkspaceId());

    subscriptionManagerClient.subscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, WORKSPACE_STATUS_CHANGED_METHOD, scope);
    subscriptionManagerClient.subscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, MACHINE_STATUS_CHANGED_METHOD, scope);
    subscriptionManagerClient.subscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, SERVER_STATUS_CHANGED_METHOD, scope);
    subscriptionManagerClient.subscribe(WS_MASTER_JSON_RPC_ENDPOINT_ID, MACHINE_LOG_METHOD, scope);
    subscriptionManagerClient.subscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, INSTALLER_LOG_METHOD, scope);
  }

  private void unsubscribeFromEvents() {
    Map<String, String> scope = singletonMap("workspaceId", appContext.getWorkspaceId());

    subscriptionManagerClient.unSubscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, WORKSPACE_STATUS_CHANGED_METHOD, scope);
    subscriptionManagerClient.unSubscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, MACHINE_STATUS_CHANGED_METHOD, scope);
    subscriptionManagerClient.unSubscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, SERVER_STATUS_CHANGED_METHOD, scope);
    subscriptionManagerClient.unSubscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, MACHINE_LOG_METHOD, scope);
    subscriptionManagerClient.unSubscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, INSTALLER_LOG_METHOD, scope);
  }
}
