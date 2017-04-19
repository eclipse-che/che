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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.CREATING;
import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

//import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;

/**
 * Notifies about changing machine state.
 *
 * @author Artem Zatsarynnyi
 * @author Roman Nikitenko
 */
@Singleton
public class MachineStatusHandler implements MachineStatusChangedEvent.Handler {

    private final EventBus                    eventBus;
    private final AppContext                  appContext;
    private final EntityFactory               entityFactory;
    private final WorkspaceServiceClient      workspaceServiceClient;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant locale;

    @Inject
    MachineStatusHandler(final EventBus eventBus,
                         final AppContext appContext,
                         final EntityFactory entityFactory,
                         final WorkspaceServiceClient workspaceServiceClient,
                         final NotificationManager notificationManager,
                         final MachineLocalizationConstant locale) {
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.entityFactory = entityFactory;
        this.workspaceServiceClient = workspaceServiceClient;
        this.notificationManager = notificationManager;
        this.locale = locale;

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    @Override
    public void onMachineStatusChanged(final MachineStatusChangedEvent event) {
        final String machineId = event.getMachineId();
        final String workspaceId = event.getWorkspaceId();

        workspaceServiceClient.getWorkspace(workspaceId).then(workspace -> {
            RuntimeDto workspaceRuntime = workspace.getRuntime();
            if (workspaceRuntime == null) {
                return;
            }

            appContext.setWorkspace(workspace);

            switch (event.getEventType()) {
                case CREATING:
                    handleMachineCreating(machineId, workspaceRuntime);
                    break;
                case RUNNING:
                    handleMachineRunning(machineId, workspaceRuntime);
                    break;
                case ERROR:
                    handleMachineError(event);
                    break;
            }
        });
    }

    private void handleMachineError(MachineStatusChangedEvent event) {
        if (isNullOrEmpty(event.getErrorMessage())) {
            return;
        }
        notificationManager.notify(event.getErrorMessage(), FAIL, EMERGE_MODE);
    }

    private MachineEntity getMachine(String machineId, RuntimeDto workspaceRuntime) {
        for (Map.Entry<String, MachineDto> entry : workspaceRuntime.getMachines().entrySet()) {
            if (machineId.equals(entry.getKey())) {
                return entityFactory.createMachine(entry.getValue());
            }
        }
        notificationManager.notify(locale.failedToFindMachine(machineId));
        return null;
    }

    private void handleMachineCreating(final String machineId, final RuntimeDto workspaceRuntime) {
        final MachineEntity machine = getMachine(machineId, workspaceRuntime);
        if (machine == null) {
            return;
        }
        eventBus.fireEvent(new MachineStateEvent(machine, CREATING));
    }

    private void handleMachineRunning(final String machineId, final RuntimeDto workspaceRuntime) {
        final MachineEntity machine = getMachine(machineId, workspaceRuntime);
        if (machine == null) {
            return;
        }

        eventBus.fireEvent(new MachineStateEvent(machine, RUNNING));
    }

}
