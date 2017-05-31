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

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.SubscriptionManagerClient;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.workspace.WorkspaceStatusHandler;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
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
                            SubscriptionManagerClient subscriptionManagerClient) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.wsStatusNotification = loader;
        this.notificationManagerProvider = notificationManagerProvider;
        this.messages = messages;
        this.wsStatusHandler = wsStatusHandler;
        this.appContext = appContext;
        this.subscriptionManagerClient = subscriptionManagerClient;
    }

    // TODO: handle errors while workspace starting (show message dialog)
    // to allow user to see the reason of failed start

    /** Start the current workspace with the default environment. */
    public void startWorkspace(boolean restoreFromSnapshot) {
        subscribeToEvents();

        wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

        final WorkspaceImpl workspace = appContext.getWorkspace();
        final WorkspaceStatus workspaceStatus = workspace.getStatus();

        if (workspaceStatus == RUNNING) {
            wsStatusHandler.handleWorkspaceRunning(workspace);
        } else if (workspaceStatus == STOPPED || workspaceStatus == STOPPING) {
            wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

            workspaceServiceClient.getSettings().then(settings -> {
                if (Boolean.parseBoolean(settings.getOrDefault(CHE_WORKSPACE_AUTO_START, "true"))) {
                    workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), restoreFromSnapshot)
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
    }

    private void subscribeToEvents() {
        subscribe("ws-master", "workspace/statusChanged");
    }

    private void subscribe(String endpointId, String methodName) {
        Map<String, String> scope = singletonMap("workspaceId", appContext.getWorkspaceId());
        subscriptionManagerClient.subscribe(endpointId, methodName, scope);
    }

    /** Stop the current workspace. */
    public void stopWorkspace() {
        workspaceServiceClient.stop(appContext.getWorkspaceId());

        // TODO: unsubscribe from events
    }
}
