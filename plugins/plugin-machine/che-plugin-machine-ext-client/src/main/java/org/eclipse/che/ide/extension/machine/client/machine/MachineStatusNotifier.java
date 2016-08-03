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
import org.eclipse.che.ide.api.workspace.event.EnvironmentStatusChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.OperationInfo;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.MACHINE_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;

/**
 * Notifies about changing machine state.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MachineStatusNotifier implements EnvironmentStatusChangedEvent.Handler {

    private final EventBus                    eventBus;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant locale;
    private final MachineServiceClient        machineServiceClient;
    private final InitialLoadingInfo          initialLoadingInfo;

    @Inject
    MachineStatusNotifier(final EventBus eventBus,
                          final InitialLoadingInfo initialLoadingInfo,
                          final MachineServiceClient machineServiceClient,
                          final NotificationManager notificationManager,
                          final MachineLocalizationConstant locale) {
        this.eventBus = eventBus;
        this.initialLoadingInfo = initialLoadingInfo;
        this.machineServiceClient = machineServiceClient;
        this.notificationManager = notificationManager;
        this.locale = locale;

        eventBus.addHandler(EnvironmentStatusChangedEvent.TYPE, this);
    }

    @Override
    public void onEnvironmentStatusChanged(final EnvironmentStatusChangedEvent event) {
        final String machineName = event.getMachineName();
        final String machineId = event.getMachineId();

        switch (event.getEventType()) {
            case CREATING:
                getMachine(machineId).then(notifyMachineCreating());
                break;
            case RUNNING:
                getMachine(machineId).then(notifyMachineRunning());
                break;
            case DESTROYED:
                notificationManager.notify(locale.notificationMachineDestroyed(machineName), SUCCESS, EMERGE_MODE);
                break;
            case ERROR:
                notificationManager.notify(event.getError(), FAIL, EMERGE_MODE);
                break;
        }
    }

    private Promise<MachineDto> getMachine(final String machineId) {
        return machineServiceClient.getMachine(machineId).catchError(new Operation<PromiseError>() {
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
                    initialLoadingInfo.setOperationStatus(MACHINE_BOOTING.getValue(), IN_PROGRESS);
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
                    initialLoadingInfo.setOperationStatus(MACHINE_BOOTING.getValue(), OperationInfo.Status.SUCCESS);
                }

                final String message = locale.notificationMachineIsRunning(machineConfig.getName());
                notificationManager.notify(message, SUCCESS, EMERGE_MODE);
                eventBus.fireEvent(new MachineStateEvent(machine, MachineStateEvent.MachineAction.RUNNING));
            }
        };
    }
}
