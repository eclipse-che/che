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
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Yevhenii Voevodin
 */
public abstract class WorkspaceComponent implements Component, WsAgentStateHandler {

    protected final WorkspaceServiceClient    workspaceServiceClient;
    protected final CoreLocalizationConstant  locale;
    protected final CreateWorkspacePresenter  createWorkspacePresenter;
    protected final DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    protected final AppContext                appContext;
    protected final BrowserQueryFieldRenderer browserQueryFieldRenderer;
    protected final DialogFactory             dialogFactory;
    protected final PreferencesManager        preferencesManager;
    protected final DtoFactory                dtoFactory;
    protected final NotificationManager       notificationManager;
    protected final StartWorkspacePresenter   startWorkspacePresenter;

    private final   EventBus                  eventBus;
    private final   Provider<MachineManager>  machineManagerProvider;
    private final   MessageBusProvider        messageBusProvider;
    private final   WorkspaceEventsHandler    workspaceEventsHandler;
    private final   LoaderPresenter           loader;

    protected Callback<Component, Exception> callback;
    protected boolean                        needToReloadComponents;
    private   MessageBus                     messageBus;

    public WorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                              CreateWorkspacePresenter createWorkspacePresenter,
                              StartWorkspacePresenter startWorkspacePresenter,
                              CoreLocalizationConstant locale,
                              DtoUnmarshallerFactory dtoUnmarshallerFactory,
                              EventBus eventBus,
                              AppContext appContext,
                              Provider<MachineManager> machineManagerProvider,
                              NotificationManager notificationManager,
                              MessageBusProvider messageBusProvider,
                              BrowserQueryFieldRenderer browserQueryFieldRenderer,
                              DialogFactory dialogFactory,
                              PreferencesManager preferencesManager,
                              DtoFactory dtoFactory,
                              WorkspaceEventsHandler workspaceEventsHandler,
                              LoaderPresenter loader) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.createWorkspacePresenter = createWorkspacePresenter;
        this.startWorkspacePresenter = startWorkspacePresenter;
        this.locale = locale;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.machineManagerProvider = machineManagerProvider;
        this.notificationManager = notificationManager;
        this.messageBusProvider = messageBusProvider;
        this.browserQueryFieldRenderer = browserQueryFieldRenderer;
        this.dialogFactory = dialogFactory;
        this.preferencesManager = preferencesManager;
        this.dtoFactory = dtoFactory;
        this.workspaceEventsHandler = workspaceEventsHandler;
        this.loader = loader;

        this.needToReloadComponents = true;

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        notificationManager.notify(locale.extServerStarted(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
    }

    /** {@inheritDoc} */
    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    abstract void tryStartWorkspace();

    /**
     * Sets workspace to app context as current.
     *
     * @param workspace
     *         workspace which will be current
     */
    public void setCurrentWorkspace(WorkspaceDto workspace) {
        appContext.setWorkspace(workspace);

        if (needToReloadComponents) {
            callback.onSuccess(WorkspaceComponent.this);
            needToReloadComponents = false;
        }

        browserQueryFieldRenderer.setQueryField(workspace.getNamespace(), workspace.getConfig().getName(), "");
    }

    public void handleWorkspaceEvents(final WorkspaceDto workspace, final Callback<Component, Exception> callback) {
        this.callback = callback;
        if (messageBus != null) {
            messageBus.cancelReconnection();
        }
        messageBus = messageBusProvider.createMessageBus(workspace.getId());

        messageBus.addOnOpenHandler(new ConnectionOpenedHandler() {
            @Override
            public void onOpen() {
                loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.LOADING);

                messageBus.removeOnOpenHandler(this);

                setCurrentWorkspace(workspace);
                workspaceEventsHandler.trackWorkspaceEvents(workspace, callback);

                final WorkspaceStatus workspaceStatus = workspace.getStatus();
                switch (workspaceStatus) {
                    case STARTING:
                        eventBus.fireEvent(new WorkspaceStartingEvent(workspace));
                        break;
                    case RUNNING:
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.SUCCESS);

                                notificationManager.notify(locale.startedWs(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
                                eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
                                machineManagerProvider.get();//start instance of machine manager
                            }
                        });
                        break;
                    default:
                        checkWorkspaceForSnapshots(workspace);
                }
            }
        });
    }

    /**
     * Starts workspace by id when web socket connected.
     *
     * @param workspace
     *         workspace which will be started
     * @param callback
     *         callback to be executed
     */
    public void startWorkspace(final WorkspaceDto workspace, final Callback<Component, Exception> callback) {
        workspaceServiceClient.getWorkspace(workspace.getId()).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspace) throws OperationException {
                handleWorkspaceEvents(workspace, callback);
            }
        });
    }

    /**
     * Checks workspace for snapshots and asks the uses for an action.
     *
     * @param workspace
     *         workspace
     */
    private void checkWorkspaceForSnapshots(final WorkspaceDto workspace) {
        workspaceServiceClient.getSnapshot(workspace.getId()).then(new Operation<List<SnapshotDto>>() {
            @Override
            public void apply(List<SnapshotDto> snapshots) throws OperationException {
                if (snapshots.isEmpty()) {
                    loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.LOADING);
                    workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), false);
                } else {
                    showRecoverWorkspaceConfirmDialog(workspace);
                }
            }
        });
    }

    /**
     * Sends a message to the parent frame to inform that IDE application can be shown.
     */
    private native void notifyShowIDE() /*-{
        $wnd.parent.postMessage("show-ide", "*");
    }-*/;

    /**
     * Shows workspace recovering confirm dialog.
     */
    private void showRecoverWorkspaceConfirmDialog(final WorkspaceDto workspace) {
        dialogFactory.createConfirmDialog(locale.workspaceRecoveringDialogTitle(),
                                          locale.workspaceRecoveringDialogText(),
                                          locale.yesButtonTitle(),
                                          locale.noButtonTitle(),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.LOADING);
                                                  workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), true);
                                              }
                                          },
                                          new CancelCallback() {
                                              @Override
                                              public void cancelled() {
                                                  loader.setProgress(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME, LoaderPresenter.Status.LOADING);
                                                  workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), false);
                                              }
                                          }).show();

        notifyShowIDE();
    }

    /**
     * Starts specified workspace if it's {@link WorkspaceStatus} different of {@code RUNNING}
     */
    protected Operation<WorkspaceDto> startWorkspace() {
        return new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspaceToStart) throws OperationException {
                startWorkspace(workspaceToStart, callback);
            }
        };
    }

}
