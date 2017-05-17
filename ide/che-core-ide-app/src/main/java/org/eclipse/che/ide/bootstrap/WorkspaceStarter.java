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
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WsStatusChangedEvent;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME;

/** Performs the routines required to run the workspace. */
@Singleton
public class WorkspaceStarter {

    private static final String WS_STATUS_ERROR_MSG = "Tried to subscribe to workspace status events, but got error";

    private final WorkspaceServiceClient workspaceServiceClient;
    private final BrowserAddress         browserAddress;
    private final RequestTransmitter     transmitter;
    private final EventBus               eventBus;
    private final LoaderPresenter        wsStatusNotification;
    private final IdeInitializer         ideInitializer;

    @Inject
    WorkspaceStarter(WorkspaceServiceClient workspaceServiceClient,
                     BrowserAddress browserAddress,
                     RequestTransmitter transmitter,
                     EventBus eventBus,
                     LoaderPresenter loader,
                     IdeInitializer ideInitializer) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.browserAddress = browserAddress;
        this.transmitter = transmitter;
        this.eventBus = eventBus;
        this.wsStatusNotification = loader;
        this.ideInitializer = ideInitializer;
    }

    // TODO: handle errors while workspace starting (show message dialog)
    // to allow user to see the reason of failed start
    void startWorkspace() {
        ideInitializer.getWorkspaceToStart().then(workspace -> {
            subscribeToWorkspaceEvents(workspace.getId());
            startWorkspace(workspace, false);
        });
    }

    public void startWorkspace(String workspaceID, boolean restoreFromSnapshot) {
        workspaceServiceClient.getWorkspace(workspaceID).then(workspace -> {
            startWorkspace(workspace, restoreFromSnapshot);
        });
    }

    /** Starts the workspace with the default environment. */
    private void startWorkspace(Workspace workspace, boolean restoreFromSnapshot) {
        wsStatusNotification.show(STARTING_WORKSPACE_RUNTIME);

        final WorkspaceStatus workspaceStatus = workspace.getStatus();

        if (workspaceStatus == RUNNING) {
            wsStatusNotification.setSuccess(STARTING_WORKSPACE_RUNTIME);
            eventBus.fireEvent(new WsStatusChangedEvent(workspace.getStatus()));
        } else if (workspaceStatus == STOPPED || workspaceStatus == STOPPING) {
            workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), restoreFromSnapshot);
        }
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

}
