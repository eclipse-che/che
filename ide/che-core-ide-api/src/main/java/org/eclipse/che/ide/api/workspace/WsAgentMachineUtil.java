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
package org.eclipse.che.ide.api.workspace;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_WEBSOCKET_REFERENCE;

import com.google.inject.Provider;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.QueryParameters;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

@Singleton
public class WsAgentMachineUtil {

  private static final String WSAGENT_SERVER_REF_PREFIX = "wsagent-ref-prefix";

  private final Provider<AppContext> appContextProvider;
  private final QueryParameters queryParameters;

  @Inject
  public WsAgentMachineUtil(
      Provider<AppContext> appContextProvider, QueryParameters queryParameters) {
    this.appContextProvider = appContextProvider;
    this.queryParameters = queryParameters;
  }

  /**
   * Returns a dev-machine or an empty {@code Optional} if none. Dev-machine is a machine where
   * ws-agent server is running.
   */
  public Optional<MachineImpl> getWsAgentServerMachine() {
    WorkspaceImpl workspace = appContextProvider.get().getWorkspace();
    RuntimeImpl runtime = workspace.getRuntime();

    return runtime
        .getMachines()
        .values()
        .stream()
        .filter(this::containsWsAgentHttpServer)
        .findAny();
  }

  public Optional<ServerImpl> getWsAgentWebSocketServer() {
    Optional<MachineImpl> wsAgentServerMachine = getWsAgentServerMachine();
    if (wsAgentServerMachine.isPresent()) {
      Map<String, ServerImpl> servers = wsAgentServerMachine.get().getServers();
      ServerImpl server = servers.get(getWsAgentWebSocketServerReference());
      return Optional.of(server);
    }

    return Optional.empty();
  }

  public Optional<ServerImpl> getWsAgentHttpServer() {
    Optional<MachineImpl> wsAgentServerMachine = getWsAgentServerMachine();
    if (wsAgentServerMachine.isPresent()) {
      Map<String, ServerImpl> servers = wsAgentServerMachine.get().getServers();
      ServerImpl server = servers.get(getWsAgentHttpServerReference());
      return Optional.of(server);
    }

    return Optional.empty();
  }

  /**
   * Checks whether provided {@link MachineImpl} contains wsagent server.
   *
   * @param machine machine config to check
   * @return true when wsagent server is found in provided machine, false otherwise
   */
  public boolean containsWsAgentHttpServer(MachineImpl machine) {
    return machine.getServers().keySet().contains(getWsAgentHttpServerReference());
  }

  public String getWsAgentHttpServerReference() {
    // prefix to prepend to wsagent server reference
    String wsAgentPrefix = queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX);

    if (isNullOrEmpty(wsAgentPrefix)) {
      return SERVER_WS_AGENT_HTTP_REFERENCE;
    }

    return wsAgentPrefix + SERVER_WS_AGENT_HTTP_REFERENCE;
  }

  private String getWsAgentWebSocketServerReference() {
    // prefix to prepend to wsagent server reference
    String wsAgentPrefix = queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX);

    if (isNullOrEmpty(wsAgentPrefix)) {
      return SERVER_WS_AGENT_WEBSOCKET_REFERENCE;
    }

    return wsAgentPrefix + SERVER_WS_AGENT_WEBSOCKET_REFERENCE;
  }
}
