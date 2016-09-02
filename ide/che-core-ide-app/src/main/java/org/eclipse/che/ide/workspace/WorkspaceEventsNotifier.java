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

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.machine.OutputMessageUnmarshaller;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.OperationInfo;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.machine.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.WORKSPACE_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.ERROR;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;

/**
 * <ul> Notifies about the events which occur in the workspace:
 * <li> changing of the workspace status ({@link WorkspaceStartingEvent}, {@link WorkspaceStartedEvent}, {@link
 * WorkspaceStoppedEvent});</li>
 * <li> changing of environments status ({@link MachineStatusChangedEvent});</li>
 * <li> receiving Environment Output message from server ({@link EnvironmentOutputEvent});</li>
 *
 * @author Vitalii Parfonov
 * @author Roman Nikitenko
 */
@Singleton
public class WorkspaceEventsNotifier {
    private final static int SKIP_COUNT = 0;
    private final static int MAX_COUNT  = 10;

    private final EventBus                            eventBus;
    private final CoreLocalizationConstant            locale;
    private final NotificationManager                 notificationManager;
    private final InitialLoadingInfo                  initialLoadingInfo;
    private final DialogFactory                       dialogFactory;
    private final DtoUnmarshallerFactory              dtoUnmarshallerFactory;
    private final Provider<MachineManager>            machineManagerProvider;
    private final MessageLoader                       snapshotLoader;
    private final Provider<DefaultWorkspaceComponent> wsComponentProvider;
    private final WorkspaceSnapshotCreator            snapshotCreator;
    private final WorkspaceServiceClient              workspaceServiceClient;
    private final StartWorkspacePresenter             startWorkspacePresenter;

    private DefaultWorkspaceComponent      workspaceComponent;
    private Callback<Component, Exception> callback;
    private MessageBus                     messageBus;
    private MessageBusProvider             messageBusProvider;
    private String                         environmentStatusChannel;
    private String                         environmentOutputChannel;
    private String                         wsAgentLogChannel;
    private String                         workspaceStatusChannel;

    @VisibleForTesting
    WorkspaceStatusSubscriptionHandler   workspaceStatusSubscriptionHandler;
    @VisibleForTesting
    EnvironmentStatusSubscriptionHandler environmentStatusSubscriptionHandler;
    @VisibleForTesting
    EnvironmentOutputSubscriptionHandler environmentOutputSubscriptionHandler;
    @VisibleForTesting
    WsAgentOutputSubscriptionHandler     wsAgentLogSubscriptionHandler;

    @Inject
    WorkspaceEventsNotifier(final EventBus eventBus,
                            final CoreLocalizationConstant locale,
                            final DialogFactory dialogFactory,
                            final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            final InitialLoadingInfo initialLoadingInfo,
                            final NotificationManager notificationManager,
                            final MessageBusProvider messageBusProvider,
                            final Provider<MachineManager> machineManagerProvider,
                            final WorkspaceSnapshotCreator snapshotCreator,
                            final LoaderFactory loaderFactory,
                            final WorkspaceServiceClient workspaceServiceClient,
                            final StartWorkspacePresenter startWorkspacePresenter,
                            final Provider<DefaultWorkspaceComponent> wsComponentProvider) {
        this.eventBus = eventBus;
        this.locale = locale;
        this.messageBusProvider = messageBusProvider;
        this.snapshotCreator = snapshotCreator;
        this.initialLoadingInfo = initialLoadingInfo;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.machineManagerProvider = machineManagerProvider;
        this.workspaceServiceClient = workspaceServiceClient;
        this.startWorkspacePresenter = startWorkspacePresenter;
        this.wsComponentProvider = wsComponentProvider;

        this.snapshotLoader = loaderFactory.newLoader(locale.createSnapshotProgress());
    }

    /**
     * Start tracking workspace events and notify about changing.
     *
     * @param workspace
     *         workspace to track
     * @param callback
     *         callback which is necessary to notify that workspace component started or failed
     */
    void trackWorkspaceEvents(final WorkspaceDto workspace, final Callback<Component, Exception> callback) {
        this.callback = callback;
        this.workspaceComponent = wsComponentProvider.get();
        this.messageBus = messageBusProvider.getMessageBus();

        subscribeToWorkspaceStatusEvents(workspace);
        subscribeOnEnvironmentStatusChannel(workspace);
        subscribeOnEnvironmentOutputChannel(workspace);

        if (wsAgentLogSubscriptionHandler == null && workspace.getRuntime() != null) {
            subscribeOnWsAgentOutputChannel(workspace, getDevMachineName(workspace));
        }
    }

    private void onWorkspaceStarting(final String workspaceId) {
        // TODO timer is a workaround. Is needed because for some reason after receiving of event workspace starting
        // get workspace event should contain runtime but it doesn't
        new Timer() {
            @Override
            public void run() {
                workspaceServiceClient.getWorkspace(workspaceId).then(new Operation<WorkspaceDto>() {
                    @Override
                    public void apply(WorkspaceDto workspace) throws OperationException {
                        String devMachineName = getDevMachineName(workspace);
                        if (devMachineName != null) {
                            subscribeOnWsAgentOutputChannel(workspace, devMachineName);
                        }

                        workspaceComponent.setCurrentWorkspace(workspace);
                        machineManagerProvider.get();

                        initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), IN_PROGRESS);
                        eventBus.fireEvent(new WorkspaceStartingEvent(workspace));
                    }
                });
            }
        }.schedule(1000);
    }

    private void onWorkspaceStarted(final String workspaceId) {
        workspaceServiceClient.getWorkspace(workspaceId).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspace) throws OperationException {
                workspaceComponent.setCurrentWorkspace(workspace);
                notificationManager.notify(locale.startedWs(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
                initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), OperationInfo.Status.SUCCESS);
                eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
            }
        });
    }

    private void subscribeToWorkspaceStatusEvents(final WorkspaceDto workspace) {
        final Link workspaceEventsLink = workspace.getLink(LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL);
        if (workspaceEventsLink == null) {
            //should never be
            notificationManager.notify(locale.workspaceSubscribeOnEventsFailed(), FAIL, EMERGE_MODE);
            Log.error(getClass(),
                      "Link " + LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL + " not found in workspace links. So events will be not handle");
            return;
        }
        workspaceStatusChannel = workspaceEventsLink.getParameter("channel").getDefaultValue();
        if (isNullOrEmpty(workspaceStatusChannel)) {
            //should never be
            notificationManager.notify(locale.workspaceSubscribeOnEventsFailed(), FAIL, EMERGE_MODE);
            Log.error(getClass(), "Channel for handling Workspace events not provide. So events will be not handle");
            return;
        }

        try {
            workspaceStatusSubscriptionHandler = new WorkspaceStatusSubscriptionHandler(workspace);
            messageBus.subscribe(workspaceStatusChannel, workspaceStatusSubscriptionHandler);
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    private void subscribeOnEnvironmentOutputChannel(WorkspaceDto workspace) {
        final Link link = workspace.getLink(LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL);
        final LinkParameter logsChannelLinkParameter = link.getParameter("channel");
        if (logsChannelLinkParameter == null) {
            return;
        }

        environmentOutputChannel = logsChannelLinkParameter.getDefaultValue();
        environmentOutputSubscriptionHandler = new EnvironmentOutputSubscriptionHandler();
        subscribeToChannel(environmentOutputChannel, environmentOutputSubscriptionHandler);
    }

    private void subscribeOnEnvironmentStatusChannel(WorkspaceDto workspace) {
        final Link link = workspace.getLink(LINK_REL_ENVIRONMENT_STATUS_CHANNEL);
        final LinkParameter statusChannelLinkParameter = link.getParameter("channel");
        if (statusChannelLinkParameter == null) {
            return;
        }

        environmentStatusChannel = statusChannelLinkParameter.getDefaultValue();
        environmentStatusSubscriptionHandler = new EnvironmentStatusSubscriptionHandler();
        subscribeToChannel(environmentStatusChannel, environmentStatusSubscriptionHandler);
    }

    private void subscribeOnWsAgentOutputChannel(final WorkspaceDto workspace, final String wsMachineName) {
        wsAgentLogChannel = "workspace:" + workspace.getId() + ":ext-server:output";
        wsAgentLogSubscriptionHandler = new WsAgentOutputSubscriptionHandler(wsMachineName);
        subscribeToChannel(wsAgentLogChannel, wsAgentLogSubscriptionHandler);
    }

    private void subscribeToChannel(String chanel, SubscriptionHandler handler) {
        try {
            messageBus.subscribe(chanel, handler);
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    private void unSubscribeFromChannel(String chanel, SubscriptionHandler handler) {
        try {
            messageBus.unsubscribe(chanel, handler);
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    private void unSubscribeHandlers() {
        unSubscribeFromChannel(workspaceStatusChannel, workspaceStatusSubscriptionHandler);
        unSubscribeFromChannel(environmentStatusChannel, environmentStatusSubscriptionHandler);
        unSubscribeFromChannel(environmentOutputChannel, environmentOutputSubscriptionHandler);
        unSubscribeFromChannel(wsAgentLogChannel, wsAgentLogSubscriptionHandler);
    }

    private String getDevMachineName(final WorkspaceDto workspace) {
        WorkspaceRuntime runtime = workspace.getRuntime();
        if (runtime == null) {
            return null;
        }

        String activeEnv = runtime.getActiveEnv();
        EnvironmentDto environment = workspace.getConfig().getEnvironments().get(activeEnv);
        if (environment != null) {
            for (Map.Entry<String, ExtendedMachineDto> machineEntry : environment.getMachines()
                                                                                 .entrySet()) {
                if (machineEntry.getValue().getAgents().contains("ws-agent")) {
                    return machineEntry.getKey();
                }
            }
        }

        // if no machine with ws-agent found
        return null;
    }

    private Operation<List<WorkspaceDto>> showErrorDialog(final String wsName, final String errorMessage) {
        return new Operation<List<WorkspaceDto>>() {
            @Override
            public void apply(final List<WorkspaceDto> workspaces) throws OperationException {
                dialogFactory.createMessageDialog(locale.startWsErrorTitle(),
                                                  locale.startWsErrorContent(wsName, errorMessage),
                                                  new ConfirmCallback() {
                                                      @Override
                                                      public void accepted() {
                                                          // Disables workspace create/start view in IDE
                                                          // startWorkspacePresenter.show(workspaces, callback);
                                                      }
                                                  }).show();
            }
        };
    }

    @VisibleForTesting
    protected class WorkspaceStatusSubscriptionHandler extends SubscriptionHandler<WorkspaceStatusEvent> {
        private final WorkspaceDto workspace;

        public WorkspaceStatusSubscriptionHandler(final WorkspaceDto workspace) {
            super(dtoUnmarshallerFactory.newWSUnmarshaller(WorkspaceStatusEvent.class));
            this.workspace = workspace;
        }

        @Override
        protected void onMessageReceived(WorkspaceStatusEvent statusEvent) {
            final String workspaceId = statusEvent.getWorkspaceId();
            switch (statusEvent.getEventType()) {
                case STARTING:
                    onWorkspaceStarting(workspaceId);
                    break;
                case RUNNING:
                    onWorkspaceStarted(workspaceId);
                    break;
                case ERROR:
                    unSubscribeHandlers();

                    notificationManager.notify(locale.workspaceStartFailed(), FAIL, FLOAT_MODE);
                    initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), ERROR);

                    final String workspaceName = workspace.getConfig().getName();
                    final String error = statusEvent.getError();
                    workspaceServiceClient.getWorkspaces(SKIP_COUNT, MAX_COUNT).then(showErrorDialog(workspaceName, error));

                    eventBus.fireEvent(new WorkspaceStoppedEvent(workspace));
                    break;
                case STOPPED:
                    unSubscribeHandlers();

                    notificationManager.notify(locale.extServerStopped(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
                    eventBus.fireEvent(new WorkspaceStoppedEvent(workspace));
                    break;
                case SNAPSHOT_CREATING:
                    snapshotLoader.show();
                    break;
                case SNAPSHOT_CREATED:
                    snapshotLoader.hide();
                    snapshotCreator.successfullyCreated();
                    break;
                case SNAPSHOT_CREATION_ERROR:
                    snapshotLoader.hide();
                    snapshotCreator.creationError("Snapshot creation error: " + statusEvent.getError());
                    break;
            }
        }

        @Override
        protected void onErrorReceived(Throwable exception) {
            notificationManager.notify(exception.getMessage(), FAIL, NOT_EMERGE_MODE);
        }
    }

    @VisibleForTesting
    protected class WsAgentOutputSubscriptionHandler extends SubscriptionHandler<String> {
        private final String wsMachineName;

        public WsAgentOutputSubscriptionHandler(final String wsMachineName) {
            super(new OutputMessageUnmarshaller());
            this.wsMachineName = wsMachineName;
        }

        @Override
        protected void onMessageReceived(String wsAgentLog) {
            eventBus.fireEvent(new EnvironmentOutputEvent(wsAgentLog, wsMachineName));
        }

        @Override
        protected void onErrorReceived(Throwable exception) {
            Log.error(WorkspaceEventsNotifier.class, exception);
        }
    }

    @VisibleForTesting
    protected class EnvironmentStatusSubscriptionHandler extends SubscriptionHandler<MachineStatusEvent> {
        public EnvironmentStatusSubscriptionHandler() {
            super(dtoUnmarshallerFactory.newWSUnmarshaller(MachineStatusEvent.class));
        }

        @Override
        protected void onMessageReceived(MachineStatusEvent event) {
            eventBus.fireEvent(new MachineStatusChangedEvent(event));
        }

        @Override
        protected void onErrorReceived(Throwable exception) {
            Log.error(WorkspaceEventsNotifier.class, exception);
        }
    }

    @VisibleForTesting
    protected class EnvironmentOutputSubscriptionHandler extends SubscriptionHandler<MachineLogMessageDto> {
        public EnvironmentOutputSubscriptionHandler() {
            super(dtoUnmarshallerFactory.newWSUnmarshaller(MachineLogMessageDto.class));
        }

        @Override
        protected void onMessageReceived(MachineLogMessageDto machineLogMessageDto) {
            eventBus.fireEvent(new EnvironmentOutputEvent(machineLogMessageDto.getContent(), machineLogMessageDto.getMachineName()));
        }

        @Override
        protected void onErrorReceived(Throwable exception) {
            Log.error(WorkspaceEventsNotifier.class, exception);
        }
    }
}
