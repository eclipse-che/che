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
package org.eclipse.che.ide.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.workspace.shared.dto.RuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntityImpl;
import org.eclipse.che.ide.api.machine.events.MachineRunningEvent;
import org.eclipse.che.ide.api.machine.events.MachineStartingEvent;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.CREATING;
import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Handles changes in the workspace's environment and fires
 * the corresponded events to notify all interested subscribers.
 */
@Singleton
public class EnvironmentStatusHandler {

    private EventBus               eventBus;
    private AppContext             appContext;
    private WorkspaceServiceClient workspaceServiceClient;
    private NotificationManager    notificationManager;
    private RequestTransmitter     transmitter;

    @Inject
    EnvironmentStatusHandler(EventBus eventBus,
                             AppContext appContext,
                             WorkspaceServiceClient workspaceServiceClient,
                             NotificationManager notificationManager,
                             RequestTransmitter transmitter) {
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.workspaceServiceClient = workspaceServiceClient;
        this.notificationManager = notificationManager;
        this.transmitter = transmitter;
    }

    public void handleEnvironmentStatusChanged(MachineStatusEvent event) {
        final String machineName = event.getMachineName();
        final String workspaceId = event.getIdentity().getWorkspaceId();

        workspaceServiceClient.getWorkspace(workspaceId).then(workspace -> {
            RuntimeDto workspaceRuntime = workspace.getRuntime();
            if (workspaceRuntime == null) {
                return;
            }

            ((AppContextImpl)appContext).setWorkspace(workspace);

            switch (event.getEventType()) {
                case STARTING:
                    handleMachineStarting(machineName);
                    break;
                case RUNNING:
                    handleMachineRunning(machineName);
                    break;
                case FAILED:
                    handleMachineError(event);
                    break;
            }
        });
    }

    private void handleMachineStarting(String machineName) {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final RuntimeImpl runtime = workspace.getRuntime();

        if (runtime == null) {
            return;
        }

        runtime.getMachineByName(machineName)
               .ifPresent(m -> {
                   subscribeToMachineOutput(machineName);
                   eventBus.fireEvent(new MachineStartingEvent(m));

                   // fire deprecated MachineStateEvent for backward compatibility
                   eventBus.fireEvent(new MachineStateEvent(new MachineEntityImpl(machineName, m), CREATING));

               });
    }

    private void subscribeToMachineOutput(String machineName) {
        final String endpointId = "ws-master";
        final String subscribeByName = "event:environment-output:subscribe-by-machine-name";
        final String workspaceIdPlusMachineName = appContext.getWorkspaceId() + "::" + machineName;

        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(subscribeByName)
                   .paramsAsString(workspaceIdPlusMachineName)
                   .sendAndSkipResult();
    }

    private void handleMachineRunning(String machineName) {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final RuntimeImpl runtime = workspace.getRuntime();

        if (runtime == null) {
            return;
        }

        runtime.getMachineByName(machineName)
               .ifPresent(m -> {
                   eventBus.fireEvent(new MachineRunningEvent(m));

                   // fire deprecated MachineStateEvent for backward compatibility
                   eventBus.fireEvent(new MachineStateEvent(new MachineEntityImpl(machineName, m), RUNNING));
               });
    }

    private void handleMachineError(MachineStatusEvent event) {
        if (!isNullOrEmpty(event.getError())) {
            notificationManager.notify(event.getError(), FAIL, EMERGE_MODE);
        }
    }
}