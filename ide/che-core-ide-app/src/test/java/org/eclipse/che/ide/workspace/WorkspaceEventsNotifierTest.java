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
package org.eclipse.che.ide.workspace;

import com.google.gwt.core.client.Callback;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.OperationInfo;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.Pair;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.ERROR;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.RUNNING;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATED;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATING;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.SNAPSHOT_CREATION_ERROR;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.STARTING;
import static org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType.STOPPED;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.WORKSPACE_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkspaceEventsNotifierTest {
    private static final String MACHINE_NAME             = "machineName";
    private static final String WORKSPACE_ID             = "workspaceId";
    private static final String WORKSPACE_STATUS_CHANNEL = "channel";
    private static final String WS_AGENT_LOG_CHANNEL     = "workspace:" + WORKSPACE_ID + ":ext-server:output";
    private static final String ACTIVE_ENV               = "activeEnv";

    //constructor mocks
    @Mock
    private EventBus                            eventBus;
    @Mock
    private CoreLocalizationConstant            locale;
    @Mock
    private NotificationManager                 notificationManager;
    @Mock
    private InitialLoadingInfo                  initialLoadingInfo;
    @Mock
    private DialogFactory                       dialogFactory;
    @Mock
    private DtoUnmarshallerFactory              dtoUnmarshallerFactory;
    @Mock
    private Provider<MachineManager>            machineManagerProvider;
    @Mock
    private MessageLoader                       snapshotLoader;
    @Mock
    private Provider<DefaultWorkspaceComponent> wsComponentProvider;
    @Mock
    private WorkspaceSnapshotCreator            snapshotCreator;
    @Mock
    private WorkspaceServiceClient              workspaceServiceClient;
    @Mock
    private StartWorkspacePresenter             startWorkspacePresenter;


    //additional mocks
    @Mock
    private Callback<Component, Exception>       callback;
    @Mock
    private DefaultWorkspaceComponent            workspaceComponent;
    @Mock
    private MessageBusProvider                   messageBusProvider;
    @Mock
    private WorkspaceDto                         workspace;
    @Mock
    private Link                                 workspaceEventsLink;
    @Mock
    private LinkParameter                        linkParameter;
    @Mock
    private WorkspaceStatusEvent                 workspaceStatusEvent;
    @Mock
    private MachineStatusEvent    machineStatusEvent;
    @Mock
    private Message                              message;
    @Mock
    private Pair                                 header;
    @Mock
    private LoaderFactory                        loaderFactory;
    @Mock
    private Unmarshallable<WorkspaceStatusEvent> unmarshallable;
    @Mock
    private MessageBus                           messageBus;
    @Mock
    private Promise<WorkspaceDto>                workspacePromise;
    @Mock
    private Promise<List<WorkspaceDto>>          workspacesPromise;

    @Captor
    private ArgumentCaptor<Operation<WorkspaceDto>>       workspaceCaptor;
    @Captor
    private ArgumentCaptor<Operation<List<WorkspaceDto>>> workspacesCaptor;

    private WorkspaceEventsNotifier workspaceEventsNotifier;

    @Before
    public void setUp() {
        when(loaderFactory.newLoader(anyString())).thenReturn(snapshotLoader);
        workspaceEventsNotifier = new WorkspaceEventsNotifier(eventBus, locale, dialogFactory, dtoUnmarshallerFactory, initialLoadingInfo,
                                                              notificationManager, messageBusProvider, machineManagerProvider,
                                                              snapshotCreator, loaderFactory, workspaceServiceClient,
                                                              startWorkspacePresenter, wsComponentProvider);
        when(wsComponentProvider.get()).thenReturn(workspaceComponent);
        when(workspace.getId()).thenReturn(WORKSPACE_ID);
        when(workspaceStatusEvent.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(workspaceServiceClient.getWorkspace(WORKSPACE_ID)).thenReturn(workspacePromise);
        when(messageBusProvider.getMessageBus()).thenReturn(messageBus);
        when(workspace.getLink(anyString())).thenReturn(workspaceEventsLink);
        when(workspaceEventsLink.getParameter("channel")).thenReturn(linkParameter);
        when(linkParameter.getDefaultValue()).thenReturn(WORKSPACE_STATUS_CHANNEL);
    }

//    @Test disabled because of GWT timer usage
    public void shouldSubscribesOnWsAgentOutputWhenWorkspaceIsStarting() throws Exception {
        WorkspaceRuntimeDto runtime = mock(WorkspaceRuntimeDto.class);
        WorkspaceConfigDto workspaceConfig = mock(WorkspaceConfigDto.class);
        when(workspaceStatusEvent.getEventType()).thenReturn(STARTING);
        when(workspace.getRuntime()).thenReturn(runtime);
        when(runtime.getActiveEnv()).thenReturn(ACTIVE_ENV);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        Map<String, EnvironmentDto> environments = new HashMap<>(3);
        EnvironmentDto environment = mock(EnvironmentDto.class);
        environments.put(ACTIVE_ENV, environment);
        when(workspaceConfig.getEnvironments()).thenReturn(environments);
        MachineConfigDto devMachineConfig = mock(MachineConfigDto.class);
        when(devMachineConfig.getName()).thenReturn(MACHINE_NAME);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(workspacePromise).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(messageBus, times(2)).subscribe(eq(WS_AGENT_LOG_CHANNEL), (MessageHandler)anyObject());
    }

    @Test
    public void shouldSubscribeOnWsAgentOutputWhenWorkspaceIsRunningAfterRefreshPage() throws Exception {
        WorkspaceRuntimeDto runtime = mock(WorkspaceRuntimeDto.class);
        WorkspaceConfigDto workspaceConfig = mock(WorkspaceConfigDto.class);
        when(workspace.getRuntime()).thenReturn(runtime);
        when(runtime.getActiveEnv()).thenReturn(ACTIVE_ENV);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        Map<String, EnvironmentDto> environments = new HashMap<>(3);
        EnvironmentDto environment = mock(EnvironmentDto.class);
        environments.put(ACTIVE_ENV, environment);
        when(workspaceConfig.getEnvironments()).thenReturn(environments);
        MachineConfigDto devMachineConfig = mock(MachineConfigDto.class);
        when(devMachineConfig.getName()).thenReturn(MACHINE_NAME);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);

        verify(messageBus).subscribe(eq(WS_AGENT_LOG_CHANNEL), (MessageHandler)anyObject());
    }

//    @Test disabled because of GWT timer usage
    public void onWorkspaceStartingTest() throws Exception {
        when(workspaceStatusEvent.getEventType()).thenReturn(STARTING);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(workspacePromise).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(workspaceServiceClient).getWorkspace(WORKSPACE_ID);
        verify(workspaceComponent).setCurrentWorkspace(workspace);
        verify(machineManagerProvider).get();
        verify(initialLoadingInfo).setOperationStatus(eq(WORKSPACE_BOOTING.getValue()), eq(IN_PROGRESS));
        verify(eventBus).fireEvent(Matchers.<WorkspaceStartingEvent>anyObject());
    }

    @Test
    public void onWorkspaceStartedTest() throws Exception {
        when(workspaceStatusEvent.getEventType()).thenReturn(RUNNING);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(workspacePromise).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(workspaceServiceClient).getWorkspace(WORKSPACE_ID);
        verify(workspaceComponent).setCurrentWorkspace(workspace);
        verify(initialLoadingInfo).setOperationStatus(eq(WORKSPACE_BOOTING.getValue()), eq(SUCCESS));
        verify(eventBus).fireEvent(Matchers.<WorkspaceStartedEvent>anyObject());
        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.SUCCESS), eq(FLOAT_MODE));
    }

    @Test
    public void onErrorEventReceivedTest() throws Exception {
        WorkspaceConfigDto workspaceConfig = mock(WorkspaceConfigDto.class);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        when(workspaceServiceClient.getWorkspaces(anyInt(), anyInt())).thenReturn(workspacesPromise);
        List<WorkspaceDto> workspaces = new ArrayList<>(1);
        workspaces.add(workspace);
        MessageDialog errorDialog = mock(MessageDialog.class);
        when(dialogFactory.createMessageDialog(anyString(), anyString(), (ConfirmCallback)anyObject())).thenReturn(errorDialog);
        when(workspaceStatusEvent.getEventType()).thenReturn(ERROR);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(workspacesPromise).then(workspacesCaptor.capture());
        workspacesCaptor.getValue().apply(workspaces);

        verify(messageBus, times(4)).unsubscribe(anyString(), (MessageHandler)anyObject());
        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.FAIL), eq(FLOAT_MODE));
        verify(initialLoadingInfo).setOperationStatus(eq(WORKSPACE_BOOTING.getValue()), eq(OperationInfo.Status.ERROR));
        verify(eventBus).fireEvent(Matchers.<WorkspaceStoppedEvent>anyObject());
        verify(errorDialog).show();
    }

    @Test
    public void onWorkspaceStoppedTest() throws Exception {
        when(workspaceStatusEvent.getEventType()).thenReturn(STOPPED);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(messageBus, times(4)).unsubscribe(anyString(), (MessageHandler)anyObject());
        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.SUCCESS), eq(FLOAT_MODE));
        verify(eventBus).fireEvent(Matchers.<WorkspaceStoppedEvent>anyObject());
    }

    @Test
    public void onSnapshotCreatingEventReceivedTest() throws Exception {
        when(workspaceStatusEvent.getEventType()).thenReturn(SNAPSHOT_CREATING);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(snapshotLoader).show();
    }

    @Test
    public void onSnapshotCreatedEventReceivedTest() throws Exception {
        when(workspaceStatusEvent.getEventType()).thenReturn(SNAPSHOT_CREATED);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(snapshotLoader).hide();
        verify(snapshotCreator).successfullyCreated();
    }

    @Test
    public void onSnapshotCreationErrorEventReceivedTest() throws Exception {
        when(workspaceStatusEvent.getEventType()).thenReturn(SNAPSHOT_CREATION_ERROR);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.workspaceStatusSubscriptionHandler.onMessageReceived(workspaceStatusEvent);

        verify(snapshotLoader).hide();
        verify(snapshotCreator).creationError(anyString());
    }

    @Test
    public void onEnvironmentStatusEventReceivedTest() throws Exception {
        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.environmentStatusSubscriptionHandler.onMessageReceived(machineStatusEvent);

        verify(eventBus).fireEvent(Matchers.<MachineStatusChangedEvent>anyObject());
    }

    @Test
    public void onEnvironmentOutputEventReceivedTest() throws Exception {
        MachineLogMessageDto machineLogMessage = mock(MachineLogMessageDto.class);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.environmentOutputSubscriptionHandler.onMessageReceived(machineLogMessage);

        verify(eventBus).fireEvent(Matchers.<EnvironmentOutputEvent>anyObject());
        verify(machineLogMessage).getContent();
        verify(machineLogMessage).getMachineName();
    }

    @Test
    public void onWsAgentOutputEventReceivedTest() throws Exception {
        WorkspaceRuntimeDto runtime = mock(WorkspaceRuntimeDto.class);
        WorkspaceConfigDto workspaceConfig = mock(WorkspaceConfigDto.class);
        when(workspace.getRuntime()).thenReturn(runtime);
        when(runtime.getActiveEnv()).thenReturn(ACTIVE_ENV);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        Map<String, EnvironmentDto> environments = new HashMap<>(3);
        EnvironmentDto environment = mock(EnvironmentDto.class);
        environments.put(ACTIVE_ENV, environment);
        when(workspaceConfig.getEnvironments()).thenReturn(environments);
        MachineConfigDto devMachineConfig = mock(MachineConfigDto.class);
        when(devMachineConfig.getName()).thenReturn(MACHINE_NAME);

        workspaceEventsNotifier.trackWorkspaceEvents(workspace, callback);
        workspaceEventsNotifier.wsAgentLogSubscriptionHandler.onMessageReceived("");

        verify(eventBus).fireEvent(Matchers.<EnvironmentOutputEvent> anyObject());
    }
}
