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

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.workspace.WorkspaceStatusHandler;

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

    private static final String WS_STATUS_ERROR_MSG = "Tried to subscribe to workspace status events, but got error";
//    private static final String WS_AGENT_OUTPUT_ERROR_MSG = "Tried to subscribe to workspace agent output, but got error";
//    private static final String ENV_STATUS_ERROR_MSG      = "Tried to subscribe to environment status events, but got error";

    private final WorkspaceServiceClient        workspaceServiceClient;
    private final BrowserAddress                browserAddress;
    private final RequestTransmitter            transmitter;
    private final LoaderPresenter               wsStatusNotification;
    private final Provider<NotificationManager> notificationManagerProvider;
    private final IdeInitializer                ideInitializer;
    private final CoreLocalizationConstant      messages;
    private final WorkspaceStatusHandler        wsStatusHandler;
    private final AppContext                    appContext;

    @Inject
    CurrentWorkspaceManager(WorkspaceServiceClient workspaceServiceClient,
                            BrowserAddress browserAddress,
                            RequestTransmitter transmitter,
                            LoaderPresenter loader,
                            Provider<NotificationManager> notificationManagerProvider,
                            IdeInitializer ideInitializer,
                            CoreLocalizationConstant messages,
                            WorkspaceStatusHandler wsStatusHandler,
                            AppContext appContext) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.browserAddress = browserAddress;
        this.transmitter = transmitter;
        this.wsStatusNotification = loader;
        this.notificationManagerProvider = notificationManagerProvider;
        this.ideInitializer = ideInitializer;
        this.messages = messages;
        this.wsStatusHandler = wsStatusHandler;
        this.appContext = appContext;
    }

    // TODO: handle errors while workspace starting (show message dialog)
    // to allow user to see the reason of failed start

    /** Start the current workspace. */
    void startWorkspace() {
        startWorkspace(false);
    }

    public void startWorkspace(boolean restoreFromSnapshot) {
        ideInitializer.getWorkspaceToStart().then(workspace -> {
            subscribeToEvents(workspace.getId());
            startWorkspace(workspace, restoreFromSnapshot);
        });
    }

    private void subscribeToEvents(String workspaceId) {
        subscribe(WS_STATUS_ERROR_MSG, "event:workspace-status:subscribe", workspaceId);
//        subscribe(WS_AGENT_OUTPUT_ERROR_MSG, "event:ws-agent-output:subscribe", workspaceId);
//        subscribe(ENV_STATUS_ERROR_MSG, "event:environment-status:subscribe", workspaceId);
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

    /** Starts the workspace with the default environment. */
    private void startWorkspace(Workspace workspace, boolean restoreFromSnapshot) {
        wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

        final WorkspaceStatus workspaceStatus = workspace.getStatus();

        if (workspaceStatus == RUNNING) {
            wsStatusHandler.handleWorkspaceStatusChanged();
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

    /** Stop the current workspace. */
    public void stopWorkspace() {
        workspaceServiceClient.stop(appContext.getWorkspaceId());
    }
}
