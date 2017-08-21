/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.actions.WorkspaceSnapshotNotifier;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStatusChangedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.start.StartWorkspaceNotification;

/**
 *
 *
 * <ul>
 *   Notifies about the events which occur in the workspace:
 *   <li>changing of the workspace status ({@link WorkspaceStartingEvent}, {@link
 *       WorkspaceStartedEvent}, {@link WorkspaceStoppedEvent});
 *   <li>changing of environments status ({@link MachineStatusChangedEvent});
 *   <li>receiving Environment Output message from server ({@link EnvironmentOutputEvent});
 *
 * @author Vitalii Parfonov
 * @author Roman Nikitenko
 */
@Singleton
public class WorkspaceEventsHandler implements WorkspaceStatusChangedEvent.Handler {

  private static final int SKIP_COUNT = 0;
  private static final int MAX_COUNT = 10;

  private final EventBus eventBus;
  private final CoreLocalizationConstant locale;
  private final NotificationManager notificationManager;
  private final DialogFactory dialogFactory;
  private final WorkspaceSnapshotNotifier snapshotNotifier;
  private final WorkspaceServiceClient workspaceServiceClient;
  private final StartWorkspaceNotification startWorkspaceNotification;
  private final LoaderPresenter loader;
  private final AppContext appContext;
  private final BrowserAddress browserAddress;

  private DelayedTask wsStartedNotification;

  @Inject
  WorkspaceEventsHandler(
      EventBus eventBus,
      CoreLocalizationConstant locale,
      DialogFactory dialogFactory,
      NotificationManager notificationManager,
      WorkspaceSnapshotNotifier snapshotNotifier,
      WorkspaceServiceClient workspaceServiceClient,
      StartWorkspaceNotification startWorkspaceNotification,
      LoaderPresenter loader,
      AppContext appContext,
      BrowserAddress browserAddress) {
    this.eventBus = eventBus;
    this.locale = locale;
    this.snapshotNotifier = snapshotNotifier;
    this.notificationManager = notificationManager;
    this.dialogFactory = dialogFactory;
    this.workspaceServiceClient = workspaceServiceClient;
    this.startWorkspaceNotification = startWorkspaceNotification;
    this.loader = loader;
    this.appContext = appContext;
    this.browserAddress = browserAddress;

    eventBus.addHandler(WorkspaceStatusChangedEvent.TYPE, this);
  }

  private void onWorkspaceStarting(final String workspaceId) {
    // TODO timer is a workaround. Is needed because for some reason after receiving of event workspace starting
    // get workspace event should contain runtime but it doesn't
    new Timer() {
      @Override
      public void run() {
        workspaceServiceClient
            .getWorkspace(workspaceId)
            .then(
                new Operation<WorkspaceDto>() {
                  @Override
                  public void apply(WorkspaceDto workspace) throws OperationException {
                    if (appContext.getActiveRuntime() != null
                        && appContext.getDevMachine() != null) {
                      Operation<PromiseError> failedWorkspaceGetOperation =
                          it ->
                              Log.error(
                                  WorkspaceEventsHandler.this.getClass(),
                                  "Tried to subscribe to environment status events, but got error"
                                      + ": "
                                      + it.getMessage());

                      Operation<WorkspaceDto> successfulWorkspaceGetOperation = it -> {};

                      workspaceServiceClient
                          .getWorkspace(browserAddress.getWorkspaceKey())
                          .then(successfulWorkspaceGetOperation)
                          .catchError(failedWorkspaceGetOperation);
                    }

                    appContext.setWorkspace(workspace);

                    loader.show(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME);
                    eventBus.fireEvent(new WorkspaceStartingEvent(workspace));
                  }
                });
      }
    }.schedule(1000);
  }

  private void onWorkspaceStarted(final String workspaceId) {
    startWorkspaceNotification.hide();
    workspaceServiceClient
        .getWorkspace(workspaceId)
        .then(
            new Operation<WorkspaceDto>() {
              @Override
              public void apply(WorkspaceDto workspace) throws OperationException {
                appContext.setWorkspace(workspace);
                loader.setSuccess(LoaderPresenter.Phase.STARTING_WORKSPACE_RUNTIME);
                eventBus.fireEvent(new WorkspaceStartedEvent(workspace));
              }
            });
  }

  private Operation<List<WorkspaceDto>> showErrorDialog(
      final String wsName, final String errorMessage) {
    return new Operation<List<WorkspaceDto>>() {
      @Override
      public void apply(final List<WorkspaceDto> workspaces) throws OperationException {
        dialogFactory
            .createMessageDialog(
                locale.startWsErrorTitle(),
                locale.startWsErrorContent(wsName, errorMessage),
                new ConfirmCallback() {
                  @Override
                  public void accepted() {
                    // Disables workspace create/start view in IDE
                    // startWorkspacePresenter.show(workspaces, callback);
                  }
                })
            .show();
      }
    };
  }

  @Override
  public void onWorkspaceStatusChangedEvent(WorkspaceStatusChangedEvent event) {
    WorkspaceStatusEvent statusEvent = event.getWorkspaceStatusEvent();
    final String workspaceId = statusEvent.getWorkspaceId();
    switch (statusEvent.getEventType()) {
      case STARTING:
        onWorkspaceStarting(workspaceId);
        break;

      case RUNNING:
        onWorkspaceStarted(workspaceId);
        break;

      case ERROR:
        notificationManager.notify(locale.workspaceStartFailed(), FAIL, FLOAT_MODE);
        startWorkspaceNotification.show(workspaceId);
        final String workspaceName = appContext.getWorkspace().getConfig().getName();
        final String error = statusEvent.getError();
        workspaceServiceClient
            .getWorkspaces(SKIP_COUNT, MAX_COUNT)
            .then(showErrorDialog(workspaceName, error));
        eventBus.fireEvent(new WorkspaceStoppedEvent(appContext.getWorkspace()));
        eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        break;

      case STOPPING:
        loader.show(LoaderPresenter.Phase.STOPPING_WORKSPACE);

        if (wsStartedNotification != null) {
          wsStartedNotification.cancel();
          wsStartedNotification = null;
        }

        break;

      case STOPPED:
        loader.setSuccess(LoaderPresenter.Phase.STOPPING_WORKSPACE);
        startWorkspaceNotification.show(statusEvent.getWorkspaceId());
        eventBus.fireEvent(WsAgentStateEvent.createWsAgentStoppedEvent());
        eventBus.fireEvent(new WorkspaceStoppedEvent(appContext.getWorkspace()));
        break;

      case SNAPSHOT_CREATING:
        loader.show(LoaderPresenter.Phase.CREATING_WORKSPACE_SNAPSHOT);
        snapshotNotifier.creationStarted();
        break;

      case SNAPSHOT_CREATED:
        loader.setSuccess(LoaderPresenter.Phase.CREATING_WORKSPACE_SNAPSHOT);
        snapshotNotifier.successfullyCreated();

        wsStartedNotification =
            new DelayedTask() {
              @Override
              public void onExecute() {

                /*
                 Workaround. When ws is in snapshotting state we can get only running state after. But we know that stopping
                 state may occur after running, so when user is entering ide when the last one is stopping we haven't init
                 ide then.
                */

                onWorkspaceStarted(workspaceId);
              }
            };
        wsStartedNotification.delay(500);

        break;

      case SNAPSHOT_CREATION_ERROR:
        loader.setError(LoaderPresenter.Phase.CREATING_WORKSPACE_SNAPSHOT);
        snapshotNotifier.creationError("Snapshot creation error: " + statusEvent.getError());
        break;
    }
  }
}
