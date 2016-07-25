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
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.event.HttpSessionDestroyedEvent;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPStatus;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.ExceptionUtils;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.WORKSPACE_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.ERROR;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Yevhenii Voevodin
 */
public abstract class WorkspaceComponent implements Component, WsAgentStateHandler {

    protected final static int SKIP_COUNT = 0;
    protected final static int MAX_COUNT  = 10;

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

    private final EventBus                 eventBus;
    private final LoaderPresenter          loader;
    private final Provider<MachineManager> machineManagerProvider;
    private final MessageBusProvider       messageBusProvider;
    private final InitialLoadingInfo       initialLoadingInfo;
    private final WorkspaceSnapshotCreator snapshotCreator;

    private MessageLoader snapshotLoader;

    protected Callback<Component, Exception> callback;
    protected boolean                        needToReloadComponents;
    private   MessageBus                     messageBus;

    public WorkspaceComponent(WorkspaceServiceClient workspaceServiceClient,
                              CreateWorkspacePresenter createWorkspacePresenter,
                              StartWorkspacePresenter startWorkspacePresenter,
                              CoreLocalizationConstant locale,
                              DtoUnmarshallerFactory dtoUnmarshallerFactory,
                              EventBus eventBus,
                              LoaderPresenter loader,
                              AppContext appContext,
                              Provider<MachineManager> machineManagerProvider,
                              NotificationManager notificationManager,
                              MessageBusProvider messageBusProvider,
                              BrowserQueryFieldRenderer browserQueryFieldRenderer,
                              DialogFactory dialogFactory,
                              PreferencesManager preferencesManager,
                              DtoFactory dtoFactory,
                              InitialLoadingInfo initialLoadingInfo,
                              WorkspaceSnapshotCreator snapshotCreator,
                              LoaderFactory loaderFactory) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.createWorkspacePresenter = createWorkspacePresenter;
        this.startWorkspacePresenter = startWorkspacePresenter;
        this.locale = locale;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.eventBus = eventBus;
        this.loader = loader;
        this.appContext = appContext;
        this.machineManagerProvider = machineManagerProvider;
        this.notificationManager = notificationManager;
        this.messageBusProvider = messageBusProvider;
        this.browserQueryFieldRenderer = browserQueryFieldRenderer;
        this.dialogFactory = dialogFactory;
        this.preferencesManager = preferencesManager;
        this.dtoFactory = dtoFactory;
        this.initialLoadingInfo = initialLoadingInfo;
        this.snapshotCreator = snapshotCreator;

        this.needToReloadComponents = true;

        this.snapshotLoader = loaderFactory.newLoader(locale.createSnapshotProgress());

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

    /**
     * Starts workspace by id when web socket connected.
     *
     * @param workspace
     *         workspace which will be started
     * @param callback
     *         callback to be executed
     */
    public void startWorkspaceById(final WorkspaceDto workspace, final Callback<Component, Exception> callback) {
        this.callback = callback;
        workspaceServiceClient.getWorkspace(workspace.getId()).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto arg) throws OperationException {
                loader.show(initialLoadingInfo);
                initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), IN_PROGRESS);

                if (messageBus != null) {
                    messageBus.cancelReconnection();
                }
                messageBus = messageBusProvider.createMessageBus(workspace.getId());

                messageBus.addOnOpenHandler(new ConnectionOpenedHandler() {
                    @Override
                    public void onOpen() {
                        messageBus.removeOnOpenHandler(this);
                        subscribeToWorkspaceStatusWebSocket(workspace);

                        WorkspaceStatus workspaceStatus = workspace.getStatus();

                        switch (workspaceStatus) {
                            case STARTING:
                                handleWsStart(workspace);
                                break;

                            case RUNNING:
                                setCurrentWorkspace(workspace);
                                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                                    @Override
                                    public void execute() {
                                        initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), SUCCESS);
                                        notificationManager.notify(locale.startedWs(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
                                        eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
                                    }
                                });
                                break;

                            default:
                                checkWorkspaceForSnapshots(workspace);
                        }
                    }
                });
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError err) throws OperationException {
                Log.error(getClass(), err.getCause());
                if (ExceptionUtils.getStatusCode(err.getCause()) == HTTPStatus.FORBIDDEN) {
                    eventBus.fireEvent(new HttpSessionDestroyedEvent());
                }
            }
        });
    }


    /**
     * Checks workspace for snapshots and asks the uses for an action.
     *
     * @param workspace
     *          workspace
     */
    private void checkWorkspaceForSnapshots(final WorkspaceDto workspace) {
        workspaceServiceClient.getSnapshot(workspace.getId()).then(new Operation<List<SnapshotDto>>() {
            @Override
            public void apply(List<SnapshotDto> snapshots) throws OperationException {
                if (snapshots.isEmpty()) {
                    handleWsStart(workspaceServiceClient.startById(workspace.getId(), workspace.getConfig().getDefaultEnv(), false));
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
        dialogFactory.createConfirmDialog("Workspace recovering",
                                          "Do you want to recover the workspace from snapshot?",
                                          "Yes",
                                          "No",
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  handleWsStart(workspaceServiceClient.startById(workspace.getId(),
                                                                                                        workspace.getConfig()
                                                                                                                 .getDefaultEnv(),
                                                                                                        true));
                                              }
                                          },
                                          new CancelCallback() {
                                              @Override
                                              public void cancelled() {
                                                  handleWsStart(workspaceServiceClient.startById(workspace.getId(),
                                                                                                 workspace.getConfig()
                                                                                                          .getDefaultEnv(),
                                                                                                 false));
                                              }
                                          })
                     .show();

        notifyShowIDE();
    }

    /**
     * Handles workspace start or recovering.
     */
    private void handleWsStart(final Promise<WorkspaceDto> promise) {
        promise.then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspace) throws OperationException {
                handleWsStart(workspace);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), ERROR);
                callback.onFailure(new Exception(arg.getCause()));
            }
        });
    }

    private void handleWsStart(WorkspaceDto workspace) {
        initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), SUCCESS);
        setCurrentWorkspace(workspace);
        EnvironmentDto currentEnvironment = null;
        for (EnvironmentDto environment : workspace.getConfig().getEnvironments()) {
            if (environment.getName().equals(workspace.getConfig().getDefaultEnv())) {
                currentEnvironment = environment;
                break;
            }
        }
        List<MachineConfigDto> machineConfigs =
                currentEnvironment != null ? currentEnvironment.getMachineConfigs() : Collections.<MachineConfigDto>emptyList();

        for (MachineConfigDto machineConfig : machineConfigs) {
            if (machineConfig.isDev()) {
                MachineManager machineManager = machineManagerProvider.get();
                machineManager.onDevMachineCreating(machineConfig);
            }
        }
    }

    private void subscribeToWorkspaceStatusWebSocket(final WorkspaceDto workspace) {
        Unmarshallable<WorkspaceStatusEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(WorkspaceStatusEvent.class);

        try {
            messageBus.subscribe("workspace:" + workspace.getId(), new SubscriptionHandler<WorkspaceStatusEvent>(unmarshaller) {
                @Override
                protected void onMessageReceived(WorkspaceStatusEvent statusEvent) {
                    String workspaceName = workspace.getConfig().getName();
                    switch (statusEvent.getEventType()) {
                        case STARTING:
                            eventBus.fireEvent(new WorkspaceStartingEvent(workspace));
                            break;

                        case RUNNING:
                            setCurrentWorkspace(workspace);
                            notificationManager.notify(locale.startedWs(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
                            eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
                            break;

                        case ERROR:
                            unSubscribeWorkspace(statusEvent.getWorkspaceId(), this);
                            notificationManager.notify(locale.workspaceStartFailed(), FAIL, FLOAT_MODE);
                            initialLoadingInfo.setOperationStatus(WORKSPACE_BOOTING.getValue(), ERROR);
                            showErrorDialog(workspaceName, statusEvent.getError());
                            eventBus.fireEvent(new WorkspaceStoppedEvent(workspace));
                            break;

                        case STOPPED:
                            unSubscribeWorkspace(statusEvent.getWorkspaceId(), this);
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
            });
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    private void showErrorDialog(final String wsName, final String errorMessage) {
        workspaceServiceClient.getWorkspaces(SKIP_COUNT, MAX_COUNT).then(new Operation<List<WorkspaceDto>>() {
            @Override
            public void apply(final List<WorkspaceDto> workspaces) throws OperationException {
                dialogFactory.createMessageDialog(locale.startWsErrorTitle(),
                                                  locale.startWsErrorContent(wsName, errorMessage),
                                                  new ConfirmCallback() {
                                                      @Override
                                                      public void accepted() {
                                                          startWorkspacePresenter.show(workspaces, callback);
                                                      }
                                                  }).show();

            }
        });

    }

    private void unSubscribeWorkspace(String workspaceId, MessageHandler handler) {
        try {
            messageBus.unsubscribe("workspace:" + workspaceId, handler);
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    /**
     * Starts specified workspace if it's {@link WorkspaceStatus} different of {@code RUNNING}
     */
    protected Operation<WorkspaceDto> startWorkspace() {
        return new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto workspaceToStart) throws OperationException {
                startWorkspaceById(workspaceToStart, callback);
            }
        };
    }
}
