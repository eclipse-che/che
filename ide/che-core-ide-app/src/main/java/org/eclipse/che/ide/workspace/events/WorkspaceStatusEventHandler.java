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
package org.eclipse.che.ide.workspace.events;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotNotifier;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WsStatusChangedEvent;
import org.eclipse.che.ide.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.start.StartWorkspaceNotification;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.CREATING_WORKSPACE_SNAPSHOT;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STOPPING_WORKSPACE;

@Singleton
public class WorkspaceStatusEventHandler {

    private final EventBus                            eventBus;
    private final LoaderPresenter                     wsStatusNotification;
    private final WorkspaceServiceClient              wsServiceClient;
    private final StartWorkspaceNotification          startWorkspaceNotification;
    private final Provider<WorkspaceSnapshotNotifier> snapshotNotifierProvider;
    private final CoreLocalizationConstant            messages;
    private final Provider<NotificationManager>       notificationManagerProvider;

    @Inject
    WorkspaceStatusEventHandler(RequestHandlerConfigurator configurator,
                                EventBus eventBus,
                                LoaderPresenter loader,
                                WorkspaceServiceClient workspaceServiceClient,
                                AppContext appContext,
                                StartWorkspaceNotification startWorkspaceNotification,
                                Provider<WorkspaceSnapshotNotifier> snapshotNotifierProvider,
                                CoreLocalizationConstant messages,
                                Provider<NotificationManager> notificationManagerProvider) {
        this.eventBus = eventBus;
        this.wsStatusNotification = loader;
        this.wsServiceClient = workspaceServiceClient;
        this.startWorkspaceNotification = startWorkspaceNotification;
        this.snapshotNotifierProvider = snapshotNotifierProvider;
        this.messages = messages;
        this.notificationManagerProvider = notificationManagerProvider;

        configurator.newConfiguration()
                    .methodName("event:workspace-status:changed")
                    .paramsAsDto(WorkspaceStatusEvent.class)
                    .noResult()
                    .withOperation((endpointId, event) -> {
                        Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

                        wsServiceClient.getWorkspace(event.getWorkspaceId()).then(workspace -> {
                            appContext.setWorkspace(workspace);
                            handleEvent(event);
                        });
                    });
    }

    private void handleEvent(WorkspaceStatusEvent event) {
        switch (event.getEventType()) {
            case STARTING:
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                break;
            case RUNNING:
                startWorkspaceNotification.hide();
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                break;
            case STOPPING:
                wsStatusNotification.show(STOPPING_WORKSPACE);
                break;
            case STOPPED:
                wsStatusNotification.setSuccess(STOPPING_WORKSPACE);
                startWorkspaceNotification.show(event.getWorkspaceId());
                break;
            case ERROR:
                notificationManagerProvider.get().notify(messages.workspaceStartFailed(), FAIL, FLOAT_MODE);
                startWorkspaceNotification.show(event.getWorkspaceId());
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

        eventBus.fireEvent(new WsStatusChangedEvent(event.getStatus()));
    }
}
