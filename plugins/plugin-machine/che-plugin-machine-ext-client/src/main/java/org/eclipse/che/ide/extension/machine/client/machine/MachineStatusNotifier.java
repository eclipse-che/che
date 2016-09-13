/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Notifies about changing machine state.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MachineStatusNotifier implements MachineStatusChangedEvent.Handler {

    private final EventBus                    eventBus;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant locale;
    private final MachineServiceClient        machineServiceClient;
    private final LoaderPresenter             loader;

    @Inject
    MachineStatusNotifier(final EventBus eventBus,
                          final MachineServiceClient machineServiceClient,
                          final NotificationManager notificationManager,
                          final MachineLocalizationConstant locale,
                          final LoaderPresenter loader) {
        this.eventBus = eventBus;
        this.machineServiceClient = machineServiceClient;
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.loader = loader;

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    @Override
    public void onMachineStatusChanged(final MachineStatusChangedEvent event) {
        final String machineName = event.getMachineName();
        final String machineId = event.getMachineId();
        final String workspaceId = event.getWorkspaceId();

        switch (event.getEventType()) {
            case CREATING:
                getMachine(workspaceId, machineId).then(notifyMachineCreating());
                break;
            case RUNNING:
                getMachine(workspaceId, machineId).then(notifyMachineRunning());
                break;
            case DESTROYED:
                notificationManager.notify(locale.notificationMachineDestroyed(machineName), SUCCESS, EMERGE_MODE);
                break;
            case ERROR:
                notificationManager.notify(event.getErrorMessage(), FAIL, EMERGE_MODE);
                break;
        }
    }

    private Promise<MachineDto> getMachine(final String workspaceId, final String machineId) {
        return machineServiceClient.getMachine(workspaceId, machineId).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(locale.failedToFindMachine(machineId));
            }
        });
    }

    private Operation<MachineDto> notifyMachineCreating() {
        return new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto machine) throws OperationException {
                if (machine.getConfig().isDev()) {
                    // Will be used later
                    // loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.LOADING);
                }

                eventBus.fireEvent(new MachineStateEvent(machine, MachineStateEvent.MachineAction.CREATING));
            }
        };
    }

    private Operation<MachineDto> notifyMachineRunning() {
        return new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto machine) throws OperationException {
                final MachineConfigDto machineConfig = machine.getConfig();
                if (machineConfig.isDev()) {
                    // Will be used later
                    // loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.SUCCESS);
                }

                final String message = locale.notificationMachineIsRunning(machineConfig.getName());
                notificationManager.notify(message, SUCCESS, EMERGE_MODE);
                eventBus.fireEvent(new MachineStateEvent(machine, MachineStateEvent.MachineAction.RUNNING));
            }
        };
    }

}
