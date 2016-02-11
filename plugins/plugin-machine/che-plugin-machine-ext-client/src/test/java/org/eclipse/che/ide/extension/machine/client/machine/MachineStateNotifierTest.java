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

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelPresenter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.api.workspace.gwt.client.event.StartWorkspaceEvent;
import org.eclipse.che.api.workspace.gwt.client.event.StartWorkspaceHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineStateNotifierTest {

    private static final String SOME_TEXT = "someText";

    //constructor mocks
    @Mock
    private MessageBusProvider          messageBusProvider;
    @Mock
    private EventBus                    eventBus;
    @Mock
    private AppContext                  appContext;
    @Mock
    private DtoUnmarshallerFactory      dtoUnmarshallerFactory;
    @Mock
    private NotificationManager         notificationManager;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private MachinePanelPresenter       machinePanelPresenter;

    //additional mocks
    @Mock
    private Unmarshallable<MachineStatusEvent> unmarshaller;
    @Mock
    private MachineStateDto                    machineState;
    @Mock
    private MessageBus                         messageBus;
    @Mock
    private UsersWorkspaceDto                  workspace;
    @Mock
    private StatusNotification                 notification;

    @Captor
    private ArgumentCaptor<StatusNotification>    notificationCaptor;
    @Captor
    private ArgumentCaptor<StartWorkspaceHandler> startWorkspaceHandlerCaptor;

    @InjectMocks
    private MachineStatusNotifier stateNotifier;

    @Before
    public void setUp() {
        when(dtoUnmarshallerFactory.newWSUnmarshaller(MachineStatusEvent.class)).thenReturn(unmarshaller);

        when(locale.notificationCreatingMachine(SOME_TEXT)).thenReturn(SOME_TEXT);
        when(locale.notificationDestroyingMachine(SOME_TEXT)).thenReturn(SOME_TEXT);

        when(messageBusProvider.getMessageBus()).thenReturn(messageBus);

        verify(eventBus).addHandler(eq(StartWorkspaceEvent.TYPE), startWorkspaceHandlerCaptor.capture());
        startWorkspaceHandlerCaptor.getValue().onWorkspaceStarted(workspace);
    }

    @Test
    public void machineShouldBeTrackedWhenMachineStateIsCreating() throws Exception {
        UsersWorkspaceDto workspace = mock(UsersWorkspaceDto.class);
        when(appContext.getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn(SOME_TEXT);
        when(machineState.getName()).thenReturn(SOME_TEXT);
        when(notificationManager.notify(anyString(), eq(PROGRESS), anyBoolean())).thenReturn(notification);
        stateNotifier.trackMachine(machineState, MachineManager.MachineOperationType.START);

        verify(notification).setTitle(eq(SOME_TEXT));

        verify(locale).notificationCreatingMachine(SOME_TEXT);
        verify(locale, never()).notificationDestroyingMachine(SOME_TEXT);

        verify(messageBus).subscribe(anyString(), Matchers.<MessageHandler>anyObject());
    }

    @Test
    public void machineShouldBeTrackedWhenMachineStateIsDestroying() throws Exception {
        UsersWorkspaceDto workspace = mock(UsersWorkspaceDto.class);
        when(appContext.getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn(SOME_TEXT);
        when(machineState.getName()).thenReturn(SOME_TEXT);
        when(notificationManager.notify(anyString(), eq(PROGRESS), anyBoolean())).thenReturn(notification);
        stateNotifier.trackMachine(machineState, MachineManager.MachineOperationType.DESTROY);

        verify(notification).setTitle(eq(SOME_TEXT));

        verify(locale).notificationDestroyingMachine(SOME_TEXT);
        verify(locale, never()).notificationCreatingMachine(SOME_TEXT);

        verify(messageBus).subscribe(anyString(), Matchers.<MessageHandler>anyObject());
    }
}
