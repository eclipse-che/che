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

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.RUNNING;
import static org.eclipse.che.api.workspace.shared.Constants.ERROR_MESSAGE_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_EXEC_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;
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
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.jsonrpc.SubscriptionManagerClient;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/** Initializes JSON-RPC connection to the workspace master. */
@Singleton
public class WsMasterJsonRpcInitializer {

  private final JsonRpcInitializer initializer;
  private final RequestTransmitter requestTransmitter;
  private final AppContext appContext;
  private final EventBus eventBus;
  private final SubscriptionManagerClient subscriptionManagerClient;
  private final WorkspaceServiceClient workspaceServiceClient;
  private final SecurityTokenProvider securityTokenProvider;
  private final WsAgentServerUtil wsAgentServerUtil;

  @Inject
  public WsMasterJsonRpcInitializer(
      JsonRpcInitializer initializer,
      RequestTransmitter requestTransmitter,
      AppContext appContext,
      EventBus eventBus,
      SubscriptionManagerClient subscriptionManagerClient,
      WorkspaceServiceClient workspaceServiceClient,
      SecurityTokenProvider securityTokenProvider,
      WsAgentServerUtil wsAgentServerUtil) {
    this.initializer = initializer;
    this.requestTransmitter = requestTransmitter;
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.subscriptionManagerClient = subscriptionManagerClient;
    this.workspaceServiceClient = workspaceServiceClient;
    this.securityTokenProvider = securityTokenProvider;
    this.wsAgentServerUtil = wsAgentServerUtil;

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
    securityTokenProvider
        .getSecurityToken()
        .then(
            token -> {
              WorkspaceImpl workspace = appContext.getWorkspace();
              String url = workspace.getLinks().get(LINK_REL_ENVIRONMENT_STATUS_CHANNEL);

              if (workspace.getStatus() == STOPPED || url == null) {
                return;
              }

              char separator = url.contains("?") ? '&' : '?';
              Optional<String> appWebSocketId = appContext.getApplicationId();
              String queryParams =
                  separator
                      + "token="
                      + token
                      + appWebSocketId.map(id -> "&clientId=" + id).orElse("");
              String wsMasterEndpointURL = url + queryParams;

              Map<String, String> initProperties = singletonMap("url", wsMasterEndpointURL);

              Set<Runnable> initActions = new HashSet<>();
              initActions.add(this::subscribeToEvents);

              if (!appWebSocketId.isPresent()) {
                initActions.add(this::processWsId);
              }

              initActions.add(this::checkStatuses);

              initializer.initialize(WS_MASTER_JSON_RPC_ENDPOINT_ID, initProperties, initActions);
            });
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
    subscriptionManagerClient.subscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, INSTALLER_STATUS_CHANGED_METHOD, scope);
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
    subscriptionManagerClient.unSubscribe(
        WS_MASTER_JSON_RPC_ENDPOINT_ID, INSTALLER_STATUS_CHANGED_METHOD, scope);
  }

  /**
   * Workspace may be running "immediately" (~500 msec) on some infrastructures. And IDE may
   * subscribe to statuses really late. So need to check whether we missed any status event.
   */
  private void checkStatuses() {
    workspaceServiceClient
        .getWorkspace(appContext.getWorkspaceId())
        .then(
            workspace -> {
              WorkspaceImpl workspacePrev = appContext.getWorkspace();

              // Update workspace model in AppContext before firing an event.
              // Because AppContext always must return an actual workspace model.
              ((AppContextImpl) appContext).setWorkspace(workspace);

              if (workspace.getStatus() != workspacePrev.getStatus()) {
                if (workspace.getStatus() == WorkspaceStatus.STOPPED) {
                  String cause = workspace.getAttributes().get(ERROR_MESSAGE_ATTRIBUTE_NAME);
                  eventBus.fireEvent(
                      new WorkspaceStoppedEvent(true, firstNonNull(cause, "Reason is unknown.")));
                  return;
                } else if (workspace.getStatus() == WorkspaceStatus.RUNNING) {
                  eventBus.fireEvent(new WorkspaceRunningEvent());
                }
              }

              for (MachineImpl machine : workspace.getRuntime().getMachines().values()) {
                for (ServerImpl server : machine.getServers().values()) {
                  Optional<MachineImpl> machinePrev =
                      workspacePrev.getRuntime().getMachineByName(machine.getName());
                  if (machinePrev.isPresent()) {
                    Optional<ServerImpl> serverPrev =
                        machinePrev.get().getServerByName(server.getName());
                    if (serverPrev.isPresent()) {
                      if (server.getStatus() != serverPrev.get().getStatus()) {
                        checkServerStatus(server, machine);
                      }
                    }
                  }
                }
              }
            });
  }

  private void checkServerStatus(ServerImpl server, MachineImpl machine) {
    if (server.getStatus() == RUNNING) {
      eventBus.fireEvent(new ServerRunningEvent(server.getName(), machine.getName()));

      String wsAgentHttpServerRef = wsAgentServerUtil.getWsAgentHttpServerReference();
      // fire events for the often used servers
      if (wsAgentHttpServerRef.equals(server.getName())) {
        eventBus.fireEvent(new WsAgentServerRunningEvent(machine.getName()));
      } else if (SERVER_TERMINAL_REFERENCE.equals(server.getName())) {
        eventBus.fireEvent(new TerminalAgentServerRunningEvent(machine.getName()));
      } else if (SERVER_EXEC_AGENT_HTTP_REFERENCE.equals(server.getName())) {
        eventBus.fireEvent(new ExecAgentServerRunningEvent(machine.getName()));
      }
    }
  }
}
