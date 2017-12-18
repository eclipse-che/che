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
package org.eclipse.che.ide.processes.runtime;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.MachineConfigImpl;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

/**
 * Implementation fetch user registered servers from the workspace configuration stored in
 * application context.
 *
 * @author Vlad Zhukovskyi
 * @see RuntimeInfoProvider
 * @see RuntimeInfo
 * @see AppContext#getWorkspace()
 * @since 5.18.0
 */
@Singleton
public class ContextBasedRuntimeInfoProvider implements RuntimeInfoProvider {

  private final AppContext appContext;

  @Inject
  public ContextBasedRuntimeInfoProvider(AppContext appContext) {
    this.appContext = appContext;
  }

  @Override
  public List<RuntimeInfo> get(String machineName) {

    requireNonNull(machineName, "Machine name should not be a null");

    WorkspaceImpl workspace = appContext.getWorkspace();

    if (workspace == null) {
      return emptyList();
    }

    // map with servers where probably port is set
    MachineConfigImpl preConfiguredRuntime =
        workspace
            .getConfig()
            .getEnvironments()
            .get(workspace.getConfig().getDefaultEnv())
            .getMachines()
            .get(machineName);

    // current runtime, usually always exists
    Optional<MachineImpl> runtimeMachine = workspace.getRuntime().getMachineByName(machineName);

    if (runtimeMachine.isPresent()) {
      return runtimeMachine
          .get()
          .getServers()
          .entrySet()
          .stream()
          .map(
              runtimeEntry -> {
                String serverRef = runtimeEntry.getKey();
                String serverUrl = runtimeEntry.getValue().getUrl();
                String serverProtocol = extractProtocol(runtimeEntry.getValue().getUrl());

                String serverPort = null;

                if (preConfiguredRuntime.getServers().containsKey(serverRef)) {
                  serverPort = preConfiguredRuntime.getServers().get(serverRef).getPort();
                }

                return new RuntimeInfo(serverRef, serverPort, serverProtocol, serverUrl);
              })
          .sorted(
              (o1, o2) ->
                  Comparator.<String>reverseOrder()
                      .compare(nullToEmpty(o1.getPort()), nullToEmpty(o2.getPort())))
          .collect(Collectors.toList());
    }

    return emptyList();
  }

  private String extractProtocol(String url) {
    String[] parts = url.split("://");
    return parts.length == 2 ? parts[0] : "";
  }
}
