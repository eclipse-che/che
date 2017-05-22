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
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotNotifier;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.start.StartWorkspaceNotification;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.CREATING_WORKSPACE_SNAPSHOT;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STOPPING_WORKSPACE;

/** Performs the routines required to run the workspace. */
@Singleton
public class WorkspaceStarter {

    private static final String WS_STATUS_ERROR_MSG = "Tried to subscribe to workspace status events, but got error";

    private final WorkspaceServiceClient               workspaceServiceClient;
    private final BrowserAddress                       browserAddress;
    private final RequestTransmitter                   transmitter;
    private final EventBus                             eventBus;
    private final LoaderPresenter                      wsStatusNotification;
    private final Provider<StartWorkspaceNotification> startWorkspaceNotificationProvider;
    private final Provider<NotificationManager>        notificationManagerProvider;
    private final Provider<WorkspaceSnapshotNotifier>  snapshotNotifierProvider;
    private final IdeInitializer                       ideInitializer;
    private final WsAgentStateController               wsAgentStateController;
    private final WsAgentURLModifier                   wsAgentURLModifier;
    private final AppContext                           appContext;
    private final CoreLocalizationConstant             messages;

    @Inject
    WorkspaceStarter(WorkspaceServiceClient workspaceServiceClient,
                     BrowserAddress browserAddress,
                     RequestTransmitter transmitter,
                     EventBus eventBus,
                     LoaderPresenter loader,
                     Provider<StartWorkspaceNotification> startWorkspaceNotificationProvider,
                     Provider<NotificationManager> notificationManagerProvider,
                     Provider<WorkspaceSnapshotNotifier> snapshotNotifierProvider,
                     IdeInitializer ideInitializer,
                     WsAgentStateController wsAgentStateController,
                     WsAgentURLModifier wsAgentURLModifier,
                     AppContext appContext,
                     CoreLocalizationConstant messages) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.browserAddress = browserAddress;
        this.transmitter = transmitter;
        this.eventBus = eventBus;
        this.wsStatusNotification = loader;
        this.startWorkspaceNotificationProvider = startWorkspaceNotificationProvider;
        this.notificationManagerProvider = notificationManagerProvider;
        this.snapshotNotifierProvider = snapshotNotifierProvider;
        this.ideInitializer = ideInitializer;
        this.wsAgentStateController = wsAgentStateController;
        this.wsAgentURLModifier = wsAgentURLModifier;
        this.appContext = appContext;
        this.messages = messages;
    }

    // TODO: handle errors while workspace starting (show message dialog)
    // to allow user to see the reason of failed start
    void startWorkspace() {
        startWorkspace(false);
    }

    public void startWorkspace(boolean restoreFromSnapshot) {
        ideInitializer.getWorkspaceToStart().then(workspace -> {
            subscribeToWorkspaceEvents(workspace.getId());
            startWorkspace(workspace, restoreFromSnapshot);
        });
    }

    /** Starts the workspace with the default environment. */
    private void startWorkspace(Workspace workspace, boolean restoreFromSnapshot) {
        wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

        final WorkspaceStatus workspaceStatus = workspace.getStatus();

        if (workspaceStatus == RUNNING) {
            checkWorkspaceStatus(null);
        } else if (workspaceStatus == STOPPED || workspaceStatus == STOPPING) {
            workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), restoreFromSnapshot);
        }
    }

    public void checkWorkspaceStatus(@Nullable WorkspaceStatusEvent serverEvent) {
        workspaceServiceClient.getWorkspace(appContext.getWorkspaceId()).then(workspace -> {
            appContext.setWorkspace(workspace);

            // FIXME: spi
            ((AppContextImpl)appContext).setProjectsRoot(Path.valueOf("/projects"));

            if (workspace.getStatus() == RUNNING) {
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                wsAgentStateController.initialize(appContext.getDevMachine());
                wsAgentURLModifier.initialize(appContext.getDevMachine());

                eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
            } else if (workspace.getStatus() == STARTING) {
                eventBus.fireEvent(new WorkspaceStartingEvent(workspace));
            } else if (workspace.getStatus() == STOPPED) {
                eventBus.fireEvent(new WorkspaceStoppedEvent(workspace));
            }

            if (serverEvent != null) {
                WorkspaceStarter.this.notify(serverEvent);
            }
        });
    }

    private void subscribeToWorkspaceEvents(String workspaceId) {
        subscribe(WS_STATUS_ERROR_MSG, "event:workspace-status:subscribe", workspaceId);
    }

    private void subscribe(String it, String methodName, String id) {
        workspaceServiceClient.getWorkspace(browserAddress.getWorkspaceKey())
                              .then((Operation<WorkspaceDto>)skip -> transmitter.newRequest()
                                                                                .endpointId("ws-master")
                                                                                .methodName(methodName)
                                                                                .paramsAsString(id)
                                                                                .sendAndSkipResult())
                              .catchError((Operation<PromiseError>)error -> Log.error(getClass(), it + ": " + error.getMessage()));
    }

    // TODO: should be separate component
    private void notify(WorkspaceStatusEvent event) {
        switch (event.getEventType()) {
            case STARTING:
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                break;
            case RUNNING:
                startWorkspaceNotificationProvider.get().hide();
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                break;
            case STOPPING:
                wsStatusNotification.show(STOPPING_WORKSPACE);
                break;
            case STOPPED:
                wsStatusNotification.setSuccess(STOPPING_WORKSPACE);
                startWorkspaceNotificationProvider.get().show();
                break;
            case ERROR:
                notificationManagerProvider.get().notify(messages.workspaceStartFailed(), FAIL, FLOAT_MODE);
                startWorkspaceNotificationProvider.get().show();
                break;
            case SNAPSHOT_CREATING:
                wsStatusNotification.show(CREATING_WORKSPACE_SNAPSHOT);
                snapshotNotifierProvider.get().creationStarted();
                break;
            case SNAPSHOT_CREATED:
                wsStatusNotification.setSuccess(CREATING_WORKSPACE_SNAPSHOT);
                snapshotNotifierProvider.get().successfullyCreated();
                break;
            case SNAPSHOT_CREATION_ERROR:
                wsStatusNotification.setError(CREATING_WORKSPACE_SNAPSHOT);
                snapshotNotifierProvider.get().creationError("Snapshot creation error: " + event.getError());
                break;
            default:
                break;
        }
    }
}
