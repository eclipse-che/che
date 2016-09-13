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

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.CREATING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.DESTROYED;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.ERROR;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.RUNNING;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineStateNotifierTest {
    private static final String MACHINE_NAME = "machineName";
    private static final String MACHINE_ID   = "machineId";
    private static final String WORKSPACE_ID = "workspaceId";

    //constructor mocks
    @Mock
    private LoaderPresenter                       loader;
    @Mock
    private NotificationManager                   notificationManager;
    @Mock
    private MachineLocalizationConstant           locale;
    @Mock
    private MachineServiceClient                  machineServiceClient;

    //additional mocks
    @Mock
    private MachineDto                            machine;
    @Mock
    private MachineConfigDto                      machineConfig;
    @Mock
    private MachineStateEvent.Handler             handler;
    @Mock
    private MachineStatusChangedEvent             machineStatusChangedEvent;
    @Mock
    private Promise<MachineDto>                   machinePromise;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>> machineCaptor;

    private EventBus eventBus = new SimpleEventBus();
    private MachineStatusNotifier statusNotifier;

    @Before
    public void setUp() {
        statusNotifier = new MachineStatusNotifier(eventBus, machineServiceClient, notificationManager, locale, loader);
        eventBus.addHandler(MachineStateEvent.TYPE, handler);

        when(machine.getConfig()).thenReturn(machineConfig);
        when(machineConfig.getName()).thenReturn(MACHINE_NAME);
        when(machineStatusChangedEvent.getMachineId()).thenReturn(MACHINE_ID);
        when(machineStatusChangedEvent.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(machineStatusChangedEvent.getMachineName()).thenReturn(MACHINE_NAME);
        when(machineServiceClient.getMachine(WORKSPACE_ID, MACHINE_ID)).thenReturn(machinePromise);
        when(machinePromise.then(Matchers.<Operation<MachineDto>>anyObject())).thenReturn(machinePromise);
        when(machinePromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(machinePromise);
    }

    @Test
    public void shouldNotifyWhenDevMachineStateIsCreating() throws Exception {
        when(machineConfig.isDev()).thenReturn(true);

        when(machineStatusChangedEvent.getEventType()).thenReturn(CREATING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(handler).onMachineCreating(Matchers.<MachineStateEvent>anyObject());
    }

    @Test
    public void shouldNotifyWhenNonDevMachineStateIsCreating() throws Exception {
        when(machineConfig.isDev()).thenReturn(false);

        when(machineStatusChangedEvent.getEventType()).thenReturn(CREATING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(handler).onMachineCreating(Matchers.<MachineStateEvent>anyObject());
    }

    @Test
    public void shouldNotifyWhenDevMachineStateIsRunning() throws Exception {
        when(machineConfig.isDev()).thenReturn(true);

        when(machineStatusChangedEvent.getEventType()).thenReturn(RUNNING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(handler).onMachineRunning(Matchers.<MachineStateEvent>anyObject());
        verify(locale).notificationMachineIsRunning(MACHINE_NAME);
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

    @Test
    public void shouldNotifyWhenNonDevMachineStateIsRunning() throws Exception {
        when(machineConfig.isDev()).thenReturn(false);

        when(machineStatusChangedEvent.getEventType()).thenReturn(RUNNING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(handler).onMachineRunning(Matchers.<MachineStateEvent>anyObject());
        verify(locale).notificationMachineIsRunning(MACHINE_NAME);
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

    @Test
    public void shouldNotifyWhenMachineStateIsDestroyed() throws Exception {
        when(machineStatusChangedEvent.getEventType()).thenReturn(DESTROYED);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(locale).notificationMachineDestroyed(MACHINE_NAME);
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

    @Test
    public void shouldNotifyWhenMachineStateIsError() throws Exception {
        when(machineStatusChangedEvent.getEventType()).thenReturn(ERROR);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(machineStatusChangedEvent).getErrorMessage();
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

}
