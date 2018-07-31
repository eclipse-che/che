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
package org.eclipse.che.ide.api.workspace;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_WEBSOCKET_REFERENCE;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.shared.Constants;
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

  private final AppContext appContext;
  private final QueryParameters queryParameters;

  @Inject
  public WsAgentServerUtil(AppContext appContext, QueryParameters queryParameters) {
    this.appContext = appContext;
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
   * Returns {@code Optional} with the {@link Constants#SERVER_WS_AGENT_HTTP_REFERENCE wsagent/http}
   * server.
   *
   * @return {@code Optional} with the {@link Constants#SERVER_WS_AGENT_HTTP_REFERENCE wsagent/http}
   *     server if the current workspace has a runtime and there is a machine with such server,
   *     otherwise an empty {@code Optional}
   */
  public Optional<ServerImpl> getWsAgentHttpServer() {
    return getServerByRef(getWsAgentHttpServerReference());
  }

  /**
   * Returns {@code Optional} with the {@link Constants#SERVER_WS_AGENT_WEBSOCKET_REFERENCE
   * wsagent/ws} server.
   *
   * @return {@code Optional} with the {@link Constants#SERVER_WS_AGENT_WEBSOCKET_REFERENCE
   *     wsagent/ws} server if the current workspace has a runtime and there is a machine with such
   *     server, otherwise an empty {@code Optional}
   */
  public Optional<ServerImpl> getWsAgentWebSocketServer() {
    return getServerByRef(getWsAgentWebSocketServerReference());
  }

  @VisibleForTesting
  Optional<ServerImpl> getServerByRef(String ref) {
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
   * Checks whether the provided {@link MachineImpl} contains the {@link
   * Constants#SERVER_WS_AGENT_HTTP_REFERENCE wsagent/http} server.
   *
   * @param machine {@link MachineImpl} to check
   * @return {@code true} if the given machine contains the {@link
   *     Constants#SERVER_WS_AGENT_HTTP_REFERENCE wsagent/http} server server, otherwise {@code
   *     false}
   */
  public boolean containsWsAgentHttpServer(MachineImpl machine) {
    return machine.getServers().keySet().contains(getWsAgentHttpServerReference());
  }

  /**
   * Returns a reference of the {@link Constants#SERVER_WS_AGENT_HTTP_REFERENCE wsagent/http}
   * server.
   *
   * <p><strong>Note</strong>, the returned server reference may be prepended with the prefix passed
   * through the URL's query parameter.
   *
   * @see #WSAGENT_SERVER_REF_PREFIX_PARAM
   */
  public String getWsAgentHttpServerReference() {
    String refPrefix = queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);

    return isNullOrEmpty(refPrefix)
        ? SERVER_WS_AGENT_HTTP_REFERENCE
        : refPrefix + SERVER_WS_AGENT_HTTP_REFERENCE;
  }

  /**
   * Returns a reference of the {@link Constants#SERVER_WS_AGENT_WEBSOCKET_REFERENCE wsagent/ws}
   * server.
   *
   * <p><strong>Note</strong>, the returned server reference may be prepended with the prefix passed
   * through the URL's query parameter.
   *
   * @see #WSAGENT_SERVER_REF_PREFIX_PARAM
   */
  public String getWsAgentWebSocketServerReference() {
    String refPrefix = queryParameters.getByName(WSAGENT_SERVER_REF_PREFIX_PARAM);

    return isNullOrEmpty(refPrefix)
        ? SERVER_WS_AGENT_WEBSOCKET_REFERENCE
        : refPrefix + SERVER_WS_AGENT_WEBSOCKET_REFERENCE;
  }

  @VisibleForTesting
  Optional<RuntimeImpl> getWorkspaceRuntime() {
    WorkspaceImpl workspace = appContext.getWorkspace();

    return Optional.ofNullable(workspace.getRuntime());
  }
}
