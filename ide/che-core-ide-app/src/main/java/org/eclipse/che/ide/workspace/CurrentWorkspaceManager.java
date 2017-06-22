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

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.SubscriptionManagerClient;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_AUTO_START;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.WORKSPACE_STOPPED;

/** Performs the routines required to start/stop the current workspace. */
@Singleton
public class CurrentWorkspaceManager {

    private final WorkspaceServiceClient        workspaceServiceClient;
    private final LoaderPresenter               wsStatusNotification;
    private final Provider<NotificationManager> notificationManagerProvider;
    private final CoreLocalizationConstant      messages;
    private final WorkspaceStatusHandler        wsStatusHandler;
    private final AppContext                    appContext;
    private final SubscriptionManagerClient     subscriptionManagerClient;

    @Inject
    CurrentWorkspaceManager(WorkspaceServiceClient workspaceServiceClient,
                            LoaderPresenter loader,
                            Provider<NotificationManager> notificationManagerProvider,
                            CoreLocalizationConstant messages,
                            WorkspaceStatusHandler wsStatusHandler,
                            AppContext appContext,
                            SubscriptionManagerClient subscriptionManagerClient,
                            EventBus eventBus) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.wsStatusNotification = loader;
        this.notificationManagerProvider = notificationManagerProvider;
        this.messages = messages;
        this.wsStatusHandler = wsStatusHandler;
        this.appContext = appContext;
        this.subscriptionManagerClient = subscriptionManagerClient;

        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> handleWorkspaceState());
    }

    /** Checks the current workspace status and does an appropriate action. */
    private void handleWorkspaceState() {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final WorkspaceStatus workspaceStatus = workspace.getStatus();

        wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

        if (workspaceStatus == STARTING) {
            subscribeToEvents();
        } else if (workspaceStatus == RUNNING) {
            subscribeToEvents();
            wsStatusHandler.handleWorkspaceRunning();
        } else if (workspaceStatus == STOPPED || workspaceStatus == STOPPING) {
            startWorkspace(false);
        }
    }

    // TODO: handle errors while workspace starting (show message dialog)
    // to allow user to see the reason of failed start

    /** Start the current workspace with a default environment. */
    void startWorkspace(boolean restoreFromSnapshot) {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final WorkspaceStatus workspaceStatus = workspace.getStatus();

        if (workspaceStatus != STOPPED && workspaceStatus != STOPPING) {
            return;
        }

        wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

        workspaceServiceClient.getSettings().then(settings -> {
            if (Boolean.parseBoolean(settings.getOrDefault(CHE_WORKSPACE_AUTO_START, "true"))) {
                subscribeToEvents();

                final String defEnvName = workspace.getConfig().getDefaultEnv();

                workspaceServiceClient.startById(workspace.getId(), defEnvName, restoreFromSnapshot)
                                      .catchError(error -> {
                                          notificationManagerProvider.get().notify(messages.startWsErrorTitle(),
                                                                                   error.getMessage(),
                                                                                   FAIL,
                                                                                   FLOAT_MODE);
                                          wsStatusNotification.setError(STARTING_WORKSPACE_RUNTIME);
                                      });
            } else {
                wsStatusNotification.show(WORKSPACE_STOPPED);
            }
        });
    }

    /** Stop the current workspace. */
    void stopWorkspace() {
        workspaceServiceClient.stop(appContext.getWorkspaceId());
    }

    private void subscribeToEvents() {
        Map<String, String> scope = singletonMap("workspaceId", appContext.getWorkspaceId());

        subscriptionManagerClient.subscribe("ws-master", "workspace/statusChanged", scope);
        subscriptionManagerClient.subscribe("ws-master", "machine/statusChanged", scope);
        subscriptionManagerClient.subscribe("ws-master", "server/statusChanged", scope);
    }
}
