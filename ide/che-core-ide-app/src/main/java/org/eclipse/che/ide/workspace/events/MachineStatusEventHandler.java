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
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntityImpl;
import org.eclipse.che.ide.api.machine.events.MachineFailedEvent;
import org.eclipse.che.ide.api.machine.events.MachineRunningEvent;
import org.eclipse.che.ide.api.machine.events.MachineStartingEvent;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.machine.events.MachineStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

import java.util.Optional;
import java.util.function.BiConsumer;

import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.CREATING;
import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.DESTROYED;
import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.RUNNING;

/**
 * Receives notifications about changing machines' statuses.
 * After a notification is received it is processed and
 * an appropriate event is fired on the {@link EventBus}.
 */
@Singleton
class MachineStatusEventHandler {

    private AppContext appContext;

    @Inject
    MachineStatusEventHandler(RequestHandlerConfigurator configurator,
                              EventBus eventBus,
                              AppContext appContext,
                              WorkspaceServiceClient workspaceServiceClient) {
        this.appContext = appContext;

        BiConsumer<String, MachineStatusEvent> operation = (String endpointId, MachineStatusEvent event) -> {
            Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

            final String machineName = event.getMachineName();
            final String workspaceId = event.getIdentity().getWorkspaceId();

            workspaceServiceClient.getWorkspace(workspaceId).then(workspace -> {
                RuntimeImpl workspaceRuntime = workspace.getRuntime();
                if (workspaceRuntime == null) {
                    return;
                }

                // Update workspace model in AppContext before firing an event.
                // Because AppContext always must return an actual workspace model.
                ((AppContextImpl)appContext).setWorkspace(workspace);

                switch (event.getEventType()) {
                    case STARTING:
                        getMachineByName(machineName).ifPresent(m -> {
                            eventBus.fireEvent(new MachineStartingEvent(m));

                            // fire deprecated MachineStateEvent for backward compatibility with IDE 5.x
                            eventBus.fireEvent(new MachineStateEvent(new MachineEntityImpl(machineName, m), CREATING));
                        });
                        break;
                    case RUNNING:
                        getMachineByName(machineName).ifPresent(m -> {
                            eventBus.fireEvent(new MachineRunningEvent(m));

                            // fire deprecated MachineStateEvent for backward compatibility with IDE 5.x
                            eventBus.fireEvent(new MachineStateEvent(new MachineEntityImpl(machineName, m), RUNNING));
                        });
                        break;
                    case STOPPED:
                        getMachineByName(machineName).ifPresent(m -> {
                            eventBus.fireEvent(new MachineStoppedEvent(m));

                            // fire deprecated MachineStateEvent for backward compatibility with IDE 5.x
                            eventBus.fireEvent(new MachineStateEvent(new MachineEntityImpl(machineName, m), DESTROYED));
                        });
                        break;
                    case FAILED:
                        getMachineByName(machineName).ifPresent(m -> {
                            eventBus.fireEvent(new MachineFailedEvent(m, event.getError()));

                            // fire deprecated MachineStateEvent for backward compatibility with IDE 5.x
                            eventBus.fireEvent(new MachineStateEvent(new MachineEntityImpl(machineName, m), DESTROYED));
                        });
                        break;
                }
            });
        };

        configurator.newConfiguration()
                    .methodName("machine/statusChanged")
                    .paramsAsDto(MachineStatusEvent.class)
                    .noResult()
                    .withBiConsumer(operation);
    }

    private Optional<MachineImpl> getMachineByName(String machineName) {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final RuntimeImpl runtime = workspace.getRuntime();

        if (runtime == null) {
            return Optional.empty();
        }

        return runtime.getMachineByName(machineName);
    }
}
