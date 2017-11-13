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
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.QueryParameters;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

/** Helps to quickly get info related to the "wsagent" server. */
@Singleton
public class WsAgentServerUtil {

  /**
   * URL's query parameter for passing the prefix of the "wsagent" server reference. Allows to tell
   * IDE to use a different "wsagent" server.
   */
  public static final String WSAGENT_SERVER_REF_PREFIX_PARAM = "wsagent-ref-prefix";

  private final Provider<AppContext> appContextProvider;
  private final QueryParameters queryParameters;

  @Inject
  public WsAgentServerUtil(
      Provider<AppContext> appContextProvider, QueryParameters queryParameters) {
    this.appContextProvider = appContextProvider;
    this.queryParameters = queryParameters;
  }

  /**
   * Returns {@code Optional} with a machine which contains the "wsagent" server.
   *
   * @return {@code Optional} with the machine which contains the "wsagent" server if the current
   *     workspace has a runtime and there is such machine, otherwise an empty {@code Optional}
   */
  public Optional<MachineImpl> getWsAgentServerMachine() {
    return getWorkspaceRuntime()
        .flatMap(
            runtime ->
                runtime
                    .getMachines()
                    .values()
                    .stream()
                    .filter(this::containsWsAgentHttpServer)
                    .findAny());
  }

  /**
   * Returns {@code Optional} with the "wsagent/http" server.
   *
   * @return {@code Optional} with the "wsagent/http" server if the current workspace has a runtime
   *     and there is a machine with such server, otherwise an empty {@code Optional}
   */
  public Optional<ServerImpl> getWsAgentHttpServer() {
    return getServerByRef(getWsAgentHttpServerReference());
  }

  /**
   * Returns {@code Optional} with the "wsagent/ws" server.
   *
   * @return {@code Optional} with the "wsagent/ws" server if the current workspace has a runtime
   *     and there is a machine with such server, otherwise an empty {@code Optional}
   */
  public Optional<ServerImpl> getWsAgentWebSocketServer() {
    return getServerByRef(getWsAgentWebSocketServerReference());
  }

  private Optional<ServerImpl> getServerByRef(String ref) {
    Optional<RuntimeImpl> runtimeOpt = getWorkspaceRuntime();

    if (runtimeOpt.isPresent()) {
      for (MachineImpl machine : runtimeOpt.get().getMachines().values()) {
        ServerImpl server = machine.getServers().get(ref);

        if (server != null) {
          return Optional.of(server);
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Checks whether the provided {@link MachineImpl} contains the "wsagent/http" server.
   *
   * @param machine {@link MachineImpl} to check
   * @return {@code true} if the given machine contains the "wsagent/http" server, otherwise {@code
   *     false}
   */
  public boolean containsWsAgentHttpServer(MachineImpl machine) {
    return machine.getServers().keySet().contains(getWsAgentHttpServerReference());
  }

  /**
   * Returns a reference of the "wsagent/http" server.
   *
   * <p><strong>Note</strong>, the returned server reference may be prepended with the prefix passed
   * through the URL's query parameter.
   *
   * @see #WSAGENT_SERVER_REF_PREFIX_PARAM
   */
  public String getWsAgentHttpServerReference() {
    String refPrefix = queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);

    if (isNullOrEmpty(refPrefix)) {
      return SERVER_WS_AGENT_HTTP_REFERENCE;
    }

    return refPrefix + SERVER_WS_AGENT_HTTP_REFERENCE;
  }

  /**
   * Returns a reference of the "wsagent/ws" server.
   *
   * <p><strong>Note</strong>, the returned server reference may be prepended with the prefix passed
   * through the URL's query parameter.
   *
   * @see #WSAGENT_SERVER_REF_PREFIX_PARAM
   */
  public String getWsAgentWebSocketServerReference() {
    String refPrefix = queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);

    if (isNullOrEmpty(refPrefix)) {
      return SERVER_WS_AGENT_WEBSOCKET_REFERENCE;
    }

    return refPrefix + SERVER_WS_AGENT_WEBSOCKET_REFERENCE;
  }

  private Optional<RuntimeImpl> getWorkspaceRuntime() {
    AppContext appContext = appContextProvider.get();
    WorkspaceImpl workspace = appContext.getWorkspace();

    return Optional.ofNullable(workspace.getRuntime());
  }
}
