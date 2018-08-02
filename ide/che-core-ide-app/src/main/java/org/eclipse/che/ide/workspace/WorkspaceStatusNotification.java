/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.STARTING_WORKSPACE_RUNTIME;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.STOPPING_WORKSPACE;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.WORKSPACE_STOPPED;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppingEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.loaders.DownloadWorkspaceOutputEvent;
import org.eclipse.che.ide.ui.loaders.PopupLoader;
import org.eclipse.che.ide.ui.loaders.PopupLoaderFactory;
import org.eclipse.che.ide.ui.loaders.PopupLoaderMessages;

/**
 * Manages the notifications of workspace status.
 *
 * @author Vitaliy Guliy
 */
@Singleton
class WorkspaceStatusNotification implements PopupLoader.ActionDelegate {

  private PopupLoaderFactory popupLoaderFactory;
  private PopupLoaderMessages locale;
  private EventBus eventBus;

  private Map<Phase, PopupLoader> popups = new HashMap<>();

  @Inject
  WorkspaceStatusNotification(
      PopupLoaderFactory popupLoaderFactory, PopupLoaderMessages locale, EventBus eventBus) {
    this.popupLoaderFactory = popupLoaderFactory;
    this.locale = locale;
    this.eventBus = eventBus;
  }

  @Inject
  private void registerEventHandlers(
      EventBus eventBus,
      AppContext appContext,
      CoreLocalizationConstant messages,
      Provider<NotificationManager> notificationManagerProvider,
      DialogFactory dialogFactory,
      RestartingStateHolder restartingStateHolder) {
    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        e -> {
          WorkspaceStatus status = appContext.getWorkspace().getStatus();

          if (status == STARTING) {
            setSuccess(WORKSPACE_STOPPED);
          } else if (status == STOPPING) {
            show(STOPPING_WORKSPACE);
          }
        });

    eventBus.addHandler(
        WorkspaceStartingEvent.TYPE,
        e -> {
          setSuccess(WORKSPACE_STOPPED);
          if (restartingStateHolder.isRestarting()) {
            show(STARTING_WORKSPACE_RUNTIME);
          }
        });

    eventBus.addHandler(WorkspaceRunningEvent.TYPE, e -> setSuccess(STARTING_WORKSPACE_RUNTIME));

    eventBus.addHandler(
        WorkspaceStoppingEvent.TYPE,
        e -> {
          setSuccess(STARTING_WORKSPACE_RUNTIME);
          show(STOPPING_WORKSPACE);
        });

    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          setSuccess(STOPPING_WORKSPACE);
          setSuccess(STARTING_WORKSPACE_RUNTIME);

          if (e.isError()) {
            notificationManagerProvider
                .get()
                .notify(messages.workspaceStartFailed(), FAIL, FLOAT_MODE);

            String errorMessage = e.getErrorMessage();
            if (!errorMessage.isEmpty()) {
              WorkspaceImpl currentWorkspace = appContext.getWorkspace();
              String workspaceName = currentWorkspace.getConfig().getName();

              dialogFactory
                  .createMessageDialog(
                      messages.startWsErrorTitle(),
                      messages.startWsErrorContent(workspaceName, errorMessage),
                      null)
                  .show();
            }
          }
        });
  }

  /**
   * Displays a loader with a message.
   *
   * @param phase corresponding phase
   * @return loader instance
   */
  PopupLoader show(Phase phase) {
    return show(phase, null);
  }

  /**
   * Displays a loader with a message and a widget.
   *
   * @param phase corresponding phase
   * @param widget additional widget to display
   * @return loader instance
   */
  PopupLoader show(Phase phase, Widget widget) {
    PopupLoader popup = popups.get(phase);
    if (popup != null) {
      return popup;
    }

    // Create and show a popup
    switch (phase) {
      case STARTING_WORKSPACE_RUNTIME:
        popup =
            popupLoaderFactory.getPopup(
                locale.startingWorkspaceRuntime(), locale.startingWorkspaceRuntimeDescription());
        popup.showDownloadButton();
        break;
      case STARTING_WORKSPACE_AGENT:
        popup =
            popupLoaderFactory.getPopup(
                locale.startingWorkspaceAgent(), locale.startingWorkspaceAgentDescription());
        break;
      case CREATING_PROJECT:
        popup =
            popupLoaderFactory.getPopup(
                locale.creatingProject(), locale.creatingProjectDescription());
        break;
      case STOPPING_WORKSPACE:
        popup =
            popupLoaderFactory.getPopup(
                locale.stoppingWorkspace(), locale.stoppingWorkspaceDescription());
        break;
      case WORKSPACE_STOPPED:
        popup =
            popupLoaderFactory.getPopup(
                locale.workspaceStopped(), locale.workspaceStoppedDescription(), widget);
        break;
      case WORKSPACE_AGENT_STOPPED:
        popup =
            popupLoaderFactory.getPopup(
                locale.wsAgentStopped(), locale.wsAgentStoppedDescription(), widget);
        break;
    }

    popup.setDelegate(this);
    popups.put(phase, popup);
    return popup;
  }

  /**
   * Sets phase succeeded and hides corresponding loader.
   *
   * @param phase corresponding phase
   */
  void setSuccess(Phase phase) {
    PopupLoader popup = popups.get(phase);
    if (popup != null) {
      // Hide the loader if status is SUCCESS
      popups.remove(phase);
      popup.setSuccess();
    }
  }

  /**
   * Sets phase filed.
   *
   * @param phase corresponding phase
   */
  void setError(Phase phase) {
    PopupLoader popup = popups.get(phase);
    if (popup != null) {
      // Don't hide the loader with status ERROR
      popups.remove(phase);
      popup.setError();
    }
  }

  @Override
  public void onDownloadLogs() {
    eventBus.fireEvent(new DownloadWorkspaceOutputEvent());
  }

  public enum Phase {
    STARTING_WORKSPACE_RUNTIME,
    STARTING_WORKSPACE_AGENT,
    CREATING_PROJECT,
    STOPPING_WORKSPACE,
    WORKSPACE_STOPPED,
    WORKSPACE_AGENT_STOPPED
  }
}
