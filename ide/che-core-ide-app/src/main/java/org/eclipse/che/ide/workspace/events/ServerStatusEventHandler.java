/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.ExecAgentServerStoppedEvent;
import org.eclipse.che.ide.api.machine.events.ServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.ServerStoppedEvent;
import org.eclipse.che.ide.api.machine.events.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.TerminalAgentServerStoppedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

import java.util.function.BiConsumer;

import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.STOPPED;
import static org.eclipse.che.api.machine.shared.Constants.EXEC_AGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.ide.api.machine.events.WsAgentStateEvent.createWsAgentStartedEvent;

/**
 * Receives notifications about changing servers' statuses.
 * After a notification is received it is processed and
 * an appropriate event is fired on the {@link EventBus}.
 */
@Singleton
class ServerStatusEventHandler {

    @Inject
    ServerStatusEventHandler(RequestHandlerConfigurator configurator,
                             EventBus eventBus,
                             AppContext appContext,
                             WorkspaceServiceClient workspaceServiceClient) {
        BiConsumer<String, ServerStatusEvent> operation = (String endpointId, ServerStatusEvent event) -> {
            Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

            workspaceServiceClient.getWorkspace(appContext.getWorkspaceId()).then(workspace -> {
                // Update workspace model in AppContext before firing an event.
                // Because AppContext always must return an actual workspace model.
                ((AppContextImpl)appContext).setWorkspace(workspace);

                if (event.getStatus() == RUNNING) {
                    eventBus.fireEvent(new ServerRunningEvent(event.getServerName(), event.getMachineName()));

                    if (WSAGENT_REFERENCE.equals(event.getServerName())) {
                        eventBus.fireEvent(new WsAgentServerRunningEvent(event.getMachineName()));

                        // fire deprecated WsAgentStateEvent for backward compatibility with IDE 5.x
                        eventBus.fireEvent(createWsAgentStartedEvent());
                    } else if (TERMINAL_REFERENCE.equals(event.getServerName())) {
                        eventBus.fireEvent(new TerminalAgentServerRunningEvent(event.getMachineName()));
                    } else if (EXEC_AGENT_REFERENCE.equals(event.getServerName())) {
                        eventBus.fireEvent(new ExecAgentServerRunningEvent(event.getMachineName()));
                    }
                } else if (event.getStatus() == STOPPED) {
                    eventBus.fireEvent(new ServerStoppedEvent(event.getServerName(), event.getMachineName()));

                    if (WSAGENT_REFERENCE.equals(event.getServerName())) {
                        eventBus.fireEvent(new WsAgentServerStoppedEvent(event.getMachineName()));
                    } else if (TERMINAL_REFERENCE.equals(event.getServerName())) {
                        eventBus.fireEvent(new TerminalAgentServerStoppedEvent(event.getMachineName()));
                    } else if (EXEC_AGENT_REFERENCE.equals(event.getServerName())) {
                        eventBus.fireEvent(new ExecAgentServerStoppedEvent(event.getMachineName()));
                    }
                }
            });
        };

        configurator.newConfiguration()
                    .methodName("server/statusChanged")
                    .paramsAsDto(ServerStatusEvent.class)
                    .noResult()
                    .withBiConsumer(operation);

        // fire deprecated WsAgentStateEvent for backward compatibility with IDE 5.x
        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> {
            if (appContext.getWorkspace().getStatus() == WorkspaceStatus.RUNNING) {
                eventBus.fireEvent(WsAgentStateEvent.createWsAgentStartedEvent());
            }
        });
    }
}
