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
package org.eclipse.che.ide.workspace;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotNotifier;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStatusChangedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppingEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.CREATING_WORKSPACE_SNAPSHOT;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STOPPING_WORKSPACE;

/**
 * Handles changes of the workspace status and fires the corresponded
 * events to notify all interested subscribers (IDE extensions).
 */
@Singleton
public class WorkspaceStatusHandler {

    private final WorkspaceServiceClient        workspaceServiceClient;
    private final AppContext                    appContext;
    private final StartWorkspaceNotification    startWorkspaceNotificationProvider;
    private final Provider<NotificationManager> notificationManagerProvider;
    private final WorkspaceSnapshotNotifier     snapshotNotifierProvider;
    private final LoaderPresenter               wsStatusNotification;
    private final EventBus                      eventBus;
    private final CoreLocalizationConstant      messages;

    @Inject
    WorkspaceStatusHandler(WorkspaceServiceClient workspaceServiceClient,
                           AppContext appContext,
                           StartWorkspaceNotification startWorkspaceNotification,
                           Provider<NotificationManager> notificationManagerProvider,
                           WorkspaceSnapshotNotifier snapshotNotifier,
                           LoaderPresenter wsStatusNotification,
                           EventBus eventBus,
                           CoreLocalizationConstant messages) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.appContext = appContext;
        this.startWorkspaceNotificationProvider = startWorkspaceNotification;
        this.notificationManagerProvider = notificationManagerProvider;
        this.snapshotNotifierProvider = snapshotNotifier;
        this.wsStatusNotification = wsStatusNotification;
        this.eventBus = eventBus;
        this.messages = messages;
    }

    public void handleWorkspaceStatusChanged(WorkspaceStatusEvent serverEvent) {

        Log.info(WorkspaceStatusHandler.class, "Workspace from context:  " + appContext.getWorkspaceId());

        // fire deprecated WorkspaceStatusChangedEvent for backward compatibility with IDE 5.x
        eventBus.fireEvent(new WorkspaceStatusChangedEvent(serverEvent));

        workspaceServiceClient.getWorkspace(appContext.getWorkspaceId()).then(workspace -> {
            // Update workspace model in AppContext before firing an event.
            // Because AppContext always must return an actual workspace model.
            ((AppContextImpl)appContext).setWorkspace(workspace);

            if (workspace.getStatus() == STARTING) {
                eventBus.fireEvent(new WorkspaceStartingEvent(workspace));
            } else if (workspace.getStatus() == RUNNING) {
                handleWorkspaceRunning();

                eventBus.fireEvent(new WorkspaceRunningEvent());
                // fire deprecated WorkspaceStatusChangedEvent for backward compatibility with IDE 5.x
                eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
            } else if (workspace.getStatus() == STOPPING) {
                eventBus.fireEvent(new WorkspaceStoppingEvent());
            } else if (workspace.getStatus() == STOPPED) {
                eventBus.fireEvent(new WorkspaceStoppedEvent(workspace));
            }

            notify(serverEvent);
        });
    }

    // FIXME: spi ide
    // should be bound to WsAgentServerRunningEvent
    void handleWorkspaceRunning() {
        ((AppContextImpl)appContext).setProjectsRoot(Path.valueOf("/projects"));

        wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
    }

    // FIXME: spi ide
    // move to the separate component that should listen appropriate events
    private void notify(WorkspaceStatusEvent event) {
        switch (event.getEventType()) {
            case STARTING:
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                break;
            case RUNNING:
                startWorkspaceNotificationProvider.hide();
                wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
                break;
            case STOPPING:
                wsStatusNotification.show(STOPPING_WORKSPACE);
                break;
            case STOPPED:
                wsStatusNotification.setSuccess(STOPPING_WORKSPACE);
                startWorkspaceNotificationProvider.show();
                break;
            case ERROR:
                notificationManagerProvider.get().notify(messages.workspaceStartFailed(), FAIL, FLOAT_MODE);
                startWorkspaceNotificationProvider.show();
                break;
            case SNAPSHOT_CREATING:
                wsStatusNotification.show(CREATING_WORKSPACE_SNAPSHOT);
                snapshotNotifierProvider.creationStarted();
                break;
            case SNAPSHOT_CREATED:
                wsStatusNotification.setSuccess(CREATING_WORKSPACE_SNAPSHOT);
                snapshotNotifierProvider.successfullyCreated();
                break;
            case SNAPSHOT_CREATION_ERROR:
                wsStatusNotification.setError(CREATING_WORKSPACE_SNAPSHOT);
                snapshotNotifierProvider.creationError("Snapshot creation error: " + event.getError());
                break;
            default:
                break;
        }
    }
}
