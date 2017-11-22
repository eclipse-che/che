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
package org.eclipse.che.ide.workspace.events;

import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_EXEC_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.ServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.ServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * Receives notifications about changing servers' statuses. After a notification is received it is
 * processed and an appropriate event is fired on the {@link EventBus}.
 */
@Singleton
class ServerStatusEventHandler {

  private final WorkspaceServiceClient workspaceServiceClient;
  private final AppContext appContext;
  private final EventBus eventBus;
  private final WsAgentServerUtil wsAgentServerUtil;

  @Inject
  ServerStatusEventHandler(
      RequestHandlerConfigurator configurator,
      WorkspaceServiceClient workspaceServiceClient,
      AppContext appContext,
      EventBus eventBus,
      WsAgentServerUtil wsAgentServerUtil) {
    this.workspaceServiceClient = workspaceServiceClient;
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.wsAgentServerUtil = wsAgentServerUtil;

    configurator
        .newConfiguration()
        .methodName(SERVER_STATUS_CHANGED_METHOD)
        .paramsAsDto(ServerStatusEvent.class)
        .noResult()
        .withBiConsumer(
            (endpointId, event) -> {
              Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

              processStatus(event);
            });
  }

  private void processStatus(ServerStatusEvent event) {
    workspaceServiceClient
        .getWorkspace(appContext.getWorkspaceId())
        .then(
            workspace -> {
              // Update workspace model in AppContext before firing an event.
              // Because AppContext always must return an actual workspace model.
              ((AppContextImpl) appContext).setWorkspace(workspace);

              String wsAgentHttpServerRef = wsAgentServerUtil.getWsAgentHttpServerReference();

              if (event.getStatus() == RUNNING) {
                eventBus.fireEvent(
                    new ServerRunningEvent(event.getServerName(), event.getMachineName()));

                // fire events for the often used servers
                if (wsAgentHttpServerRef.equals(event.getServerName())) {
                  eventBus.fireEvent(new WsAgentServerRunningEvent(event.getMachineName()));
                } else if (SERVER_TERMINAL_REFERENCE.equals(event.getServerName())) {
                  eventBus.fireEvent(new TerminalAgentServerRunningEvent(event.getMachineName()));
                } else if (SERVER_EXEC_AGENT_HTTP_REFERENCE.equals(event.getServerName())) {
                  eventBus.fireEvent(new ExecAgentServerRunningEvent(event.getMachineName()));
                }
              } else if (event.getStatus() == STOPPED) {
                eventBus.fireEvent(
                    new ServerStoppedEvent(event.getServerName(), event.getMachineName()));

                // fire events for the often used servers
                if (wsAgentHttpServerRef.equals(event.getServerName())) {
                  eventBus.fireEvent(new WsAgentServerStoppedEvent(event.getMachineName()));
                } else if (SERVER_TERMINAL_REFERENCE.equals(event.getServerName())) {
                  eventBus.fireEvent(new TerminalAgentServerStoppedEvent(event.getMachineName()));
                } else if (SERVER_EXEC_AGENT_HTTP_REFERENCE.equals(event.getServerName())) {
                  eventBus.fireEvent(new ExecAgentServerStoppedEvent(event.getMachineName()));
                }
              }
            });
  }
}
