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
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_EXEC_AGENT_WEBSOCKET_REFERENCE;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.util.loging.Log;

/** Initializes JSON-RPC connection to the ws-agent server. */
@Singleton
public class ExecAgentJsonRpcInitializer {

  private final AppContext appContext;
  private final JsonRpcInitializer initializer;
  private final AgentURLModifier agentURLModifier;

  @Inject
  public ExecAgentJsonRpcInitializer(
      JsonRpcInitializer initializer,
      AppContext appContext,
      EventBus eventBus,
      AgentURLModifier agentURLModifier) {
    this.appContext = appContext;
    this.initializer = initializer;
    this.agentURLModifier = agentURLModifier;

    eventBus.addHandler(
        ExecAgentServerRunningEvent.TYPE,
        event -> initializeJsonRpcService(event.getMachineName()));
    eventBus.addHandler(
        ExecAgentServerStoppedEvent.TYPE, event -> initializer.terminate(event.getMachineName()));

    // in case workspace is already running
    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        event -> {
          final WorkspaceImpl workspace = appContext.getWorkspace();

          if (workspace.getStatus() == RUNNING) {
            final RuntimeImpl runtime = workspace.getRuntime();

            if (runtime != null) {
              runtime
                  .getMachines()
                  .values()
                  .stream()
                  .map(MachineImpl::getName)
                  .forEach(this::initializeJsonRpcService);
            }
          }
        });
  }

  private void initializeJsonRpcService(String machineName) {
    Log.debug(ExecAgentJsonRpcInitializer.class, "Web socket agent started event caught.");

    try {
      internalInitialize(machineName);
    } catch (Exception e) {
      Log.debug(ExecAgentJsonRpcInitializer.class, "Failed, will try one more time.");

      new Timer() {
        @Override
        public void run() {
          internalInitialize(machineName);
        }
      }.schedule(1_000);
    }
  }

  private void internalInitialize(String machineName) {
    final WorkspaceImpl workspace = appContext.getWorkspace();
    final RuntimeImpl runtime = workspace.getRuntime();

    if (runtime == null) {
      return; // workspace is stopped
    }

    runtime
        .getMachineByName(machineName)
        .ifPresent(
            machine -> {
              Optional<ServerImpl> execAgentServer =
                  machine.getServerByName(SERVER_EXEC_AGENT_WEBSOCKET_REFERENCE);

              execAgentServer.ifPresent(
                  server ->
                      initializer.initialize(
                          machine.getName(),
                          singletonMap("url", agentURLModifier.modify(server.getUrl()))));
            });
  }
}
