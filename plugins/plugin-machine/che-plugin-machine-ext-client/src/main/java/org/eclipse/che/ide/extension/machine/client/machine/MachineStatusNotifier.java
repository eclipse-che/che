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

import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.machine.events.MachineStateEvent;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.api.workspace.gwt.client.event.StartWorkspaceEvent;
import org.eclipse.che.api.workspace.gwt.client.event.StartWorkspaceHandler;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.api.machine.gwt.client.MachineManager.MachineOperationType;
import static org.eclipse.che.api.machine.gwt.client.MachineManager.MachineOperationType.RESTART;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Notifies about changing machine state.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
class MachineStatusNotifier {

    /** WebSocket channel to receive messages about changing machine state. */
    public static final String MACHINE_STATUS_WS_CHANNEL = "machine:status:";

    private final EventBus                    eventBus;
    private final DtoUnmarshallerFactory      dtoUnmarshallerFactory;
    private final AppContext                  appContext;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant locale;

    private MessageBus messageBus;

    @Inject
    MachineStatusNotifier(EventBus eventBus,
                          DtoUnmarshallerFactory dtoUnmarshallerFactory,
                          AppContext appContext,
                          final MessageBusProvider messageBusProvider,
                          NotificationManager notificationManager,
                          MachineLocalizationConstant locale) {
        this.eventBus = eventBus;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.locale = locale;

        this.messageBus = messageBusProvider.getMessageBus();

        eventBus.addHandler(StartWorkspaceEvent.TYPE, new StartWorkspaceHandler() {
            @Override
            public void onWorkspaceStarted(UsersWorkspaceDto workspace) {
                messageBus = messageBusProvider.getMessageBus();
            }
        });
    }

    /**
     * Start tracking machine state and notify about state changing.
     *
     * @param machineState
     *         machine to track
     */
    void trackMachine(MachineStateDto machineState, MachineOperationType operationType) {
        trackMachine(machineState, null, operationType);
    }

    /**
     * Start tracking machine state and notify about state changing.
     *
     * @param machineState
     *         machine to track
     * @param runningListener
     *         listener that will be notified when machine is running
     */
    void trackMachine(final MachineStateDto machineState, final RunningListener runningListener, final MachineOperationType operationType) {
        final String machineName = machineState.getName();
        final String workspaceId = appContext.getWorkspace().getId();
        final String wsChannel = MACHINE_STATUS_WS_CHANNEL + workspaceId + ":" + machineName;

        final StatusNotification notification = notificationManager.notify("", PROGRESS, false);

        final Unmarshallable<MachineStatusEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(MachineStatusEvent.class);
        final MessageHandler messageHandler = new SubscriptionHandler<MachineStatusEvent>(unmarshaller) {
            @Override
            protected void onMessageReceived(MachineStatusEvent result) {
                switch (result.getEventType()) {
                    case RUNNING:
                        unsubscribe(wsChannel, this);

                        if (runningListener != null) {
                            runningListener.onRunning();
                        }

                        final String message = RESTART.equals(operationType) ? locale.machineRestarted(machineName)
                                                                             : locale.notificationMachineIsRunning(machineName);
                        notification.setTitle(message);
                        notification.setStatus(SUCCESS);
                        eventBus.fireEvent(MachineStateEvent.createMachineRunningEvent(machineState));
                        break;
                    case DESTROYED:
                        unsubscribe(wsChannel, this);
                        notification.setStatus(SUCCESS);
                        notification.setTitle(locale.notificationMachineDestroyed(machineName));
                        eventBus.fireEvent(MachineStateEvent.createMachineDestroyedEvent(machineState));
                        break;
                    case ERROR:
                        unsubscribe(wsChannel, this);
                        notification.setStatus(FAIL);
                        notification.setTitle(result.getError());
                        break;
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                unsubscribe(wsChannel, this);
                notification.setStatus(FAIL);
            }
        };

        switch (operationType) {
            case START:
                notification.setTitle(locale.notificationCreatingMachine(machineName));
                break;
            case RESTART:
                notification.setTitle(locale.notificationMachineRestarting(machineName));
                break;
            case DESTROY:
                notification.setTitle(locale.notificationDestroyingMachine(machineName));
                break;
        }

        notification.setStatus(PROGRESS);

        subscribe(wsChannel, messageHandler);
    }

    private void subscribe(@NotNull String wsChannel, @NotNull MessageHandler handler) {
        try {
            messageBus.subscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    private void unsubscribe(@NotNull String wsChannel, @NotNull MessageHandler handler) {
        try {
            messageBus.unsubscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    /** Listener's method will be invoked when machine is running. */
    interface RunningListener {
        void onRunning();
    }
}
