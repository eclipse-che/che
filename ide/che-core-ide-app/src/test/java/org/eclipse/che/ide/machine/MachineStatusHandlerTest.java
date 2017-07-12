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

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.CREATING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.ERROR;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineStatusHandlerTest {
    private static final String MACHINE_NAME = "machineName";
    private static final String MACHINE_ID   = "machineId";
    private static final String WORKSPACE_ID = "workspaceId";

    //constructor mocks
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private CoreLocalizationConstant locale;
    @Mock
    private WorkspaceServiceClient   workspaceServiceClient;
    @Mock
    private AppContext               appContext;

    //additional mocks
    @Mock
    private MachineDto                              machineDto;
    @Mock
    private MachineEntity                           machine;
    @Mock
    private MachineStateEvent.Handler               handler;
    @Mock
    private MachineConfigDto                        machineConfigDto;
    @Mock
    private MachineStatusChangedEvent               machineStatusChangedEvent;
    @Mock
    private Promise<WorkspaceDto>                   workspacePromise;
    @Mock
    private WorkspaceDto                            workspace;
    @Mock
    private WorkspaceRuntimeDto                     workspaceRuntime;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private RequestTransmitter                      transmitter;
    @Captor
    private ArgumentCaptor<Operation<WorkspaceDto>> workspaceCaptor;

    private EventBus eventBus = new SimpleEventBus();
    private MachineStatusHandler statusNotifier;

    @Before
    public void setUp() {
        statusNotifier = new MachineStatusHandler(eventBus, appContext, workspaceServiceClient, notificationManager, locale, transmitter);
        eventBus.addHandler(MachineStateEvent.TYPE, handler);

        when(machine.getDisplayName()).thenReturn(MACHINE_NAME);
        when(machineDto.getId()).thenReturn(MACHINE_ID);
        when(machineDto.getConfig()).thenReturn(machineConfigDto);
        when(machineConfigDto.getName()).thenReturn(MACHINE_NAME);
        when(workspace.getRuntime()).thenReturn(workspaceRuntime);
        when(workspaceRuntime.getMachines()).thenReturn(Collections.singletonList(machineDto));
        when(machineStatusChangedEvent.getMachineId()).thenReturn(MACHINE_ID);
        when(machineStatusChangedEvent.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(machineStatusChangedEvent.getMachineName()).thenReturn(MACHINE_NAME);
        when(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).thenReturn(workspacePromise);
    }

    @Test
    public void shouldNotifyWhenDevMachineStateIsCreating() throws Exception {
        when(machine.isDev()).thenReturn(true);

        when(machineStatusChangedEvent.getEventType()).thenReturn(CREATING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(appContext).setWorkspace(workspace);
        verify(handler).onMachineCreating(Matchers.anyObject());
    }

    @Test
    public void shouldNotifyWhenNonDevMachineStateIsCreating() throws Exception {
        when(machine.isDev()).thenReturn(false);

        when(machineStatusChangedEvent.getEventType()).thenReturn(CREATING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(appContext).setWorkspace(workspace);
        verify(handler).onMachineCreating(Matchers.anyObject());
    }

    @Test
    public void shouldHandleCaseWhenDevMachineStateIsRunning() throws Exception {
        when(machine.isDev()).thenReturn(true);

        when(machineStatusChangedEvent.getEventType()).thenReturn(RUNNING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(appContext).setWorkspace(workspace);
        verify(handler).onMachineRunning(Matchers.anyObject());
    }

    @Test
    public void shouldHandleCaseWhenNonDevMachineStateIsRunning() throws Exception {
        when(machine.isDev()).thenReturn(false);

        when(machineStatusChangedEvent.getEventType()).thenReturn(RUNNING);
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(appContext).setWorkspace(workspace);
        verify(handler).onMachineRunning(Matchers.anyObject());
    }

    @Test
    public void shouldNotifyWhenMachineStateIsError() throws Exception {
        when(machineStatusChangedEvent.getEventType()).thenReturn(ERROR);
        when(machineStatusChangedEvent.getErrorMessage()).thenReturn("error");
        statusNotifier.onMachineStatusChanged(machineStatusChangedEvent);

        verify(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(appContext).setWorkspace(workspace);
        verify(notificationManager).notify("error", FAIL, EMERGE_MODE);
    }
}
