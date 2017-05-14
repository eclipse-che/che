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

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WsStatusChangedEvent;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.context.QueryParameters;
import org.eclipse.che.ide.jsonrpc.RequestTransmitter;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.ide.ui.loaders.LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME;

@Singleton
public class WorkspaceStarter {

    private static final String WS_STATUS_ERROR_MSG = "Tried to subscribe to workspace status events, but got error";
//    private static final String WS_AGENT_OUTPUT_ERROR_MSG = "Tried to subscribe to workspace agent output, but got error";
//    private static final String ENV_STATUS_ERROR_MSG      = "Tried to subscribe to environment status events, but got error";

    private final WorkspaceServiceClient             workspaceServiceClient;
    private final BrowserAddress                     browserAddress;
    private final RequestTransmitter                 transmitter;
    private final EventBus                           eventBus;
    private final AppContext                         appContext;
    private final LoaderPresenter                    wsStatusNotification;
    private final Provider<CreateWorkspacePresenter> createWsPresenter;
    private final QueryParameters                    queryParameters;

    @Inject
    WorkspaceStarter(WorkspaceServiceClient workspaceServiceClient,
                     BrowserAddress browserAddress,
                     RequestTransmitter transmitter,
                     EventBus eventBus,
                     AppContext appContext,
                     LoaderPresenter loader,
                     Provider<CreateWorkspacePresenter> createWorkspacePresenterProvider,
                     QueryParameters queryParameters) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.browserAddress = browserAddress;
        this.transmitter = transmitter;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.wsStatusNotification = loader;
        this.createWsPresenter = createWorkspacePresenterProvider;
        this.queryParameters = queryParameters;
    }

    public void startWorkspace() {
        workspaceServiceClient.getWorkspace(getWorkspaceKeyToStart())
                              .then(workspace -> {
//                                  browserAddress.setAddress(workspace.getNamespace(), workspace.getConfig().getName());
                                  appContext.setWorkspace(workspace);
                                  subscribeToWorkspaceEvents(workspace.getId());
                                  startWorkspace(workspace, false);
                              })
                              .catchError(err -> {
                                  Log.error(WorkspaceStarter.this.getClass(), err.getCause());
                                  createWs();
                              });
    }

    public void startWorkspace(String workspaceID, boolean restoreFromSnapshot) {
        workspaceServiceClient.getWorkspace(workspaceID).then(workspace -> {
            startWorkspace(workspace, restoreFromSnapshot);
        });
    }

    // TODO: factory
    private String getWorkspaceKeyToStart() {
        final String factoryParams = queryParameters.getByName("factory");

        return browserAddress.getWorkspaceKey();
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
//        subscribe(WS_AGENT_OUTPUT_ERROR_MSG, "event:ws-agent-output:subscribe", workspaceId);
//        subscribe(ENV_STATUS_ERROR_MSG, "event:environment-status:subscribe", workspaceId);
    }

    private void subscribe(String it, String methodName, String id) {
        workspaceServiceClient.getWorkspace(browserAddress.getWorkspaceKey())
                              .then((Operation<WorkspaceDto>)skip -> transmitter.transmitStringToNone("ws-master", methodName, id))
                              .catchError((Operation<PromiseError>)error -> Log.error(getClass(), it + ": " + error.getMessage()));
    }

    // temporary solution since dashboard doesn't work
    private void createWs() {
        workspaceServiceClient.getWorkspaces(0, 30).then(workspaces -> {
            createWsPresenter.get().show(workspaces, new Callback<Workspace, Exception>() {
                @Override
                public void onSuccess(Workspace result) {
                    startWorkspace();
                }

                @Override
                public void onFailure(Exception reason) {
                }
            });
        });
    }
}
