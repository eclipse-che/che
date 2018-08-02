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

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.WORKSPACE_STOPPED;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.ui.window.WindowClientBundle;

/**
 * Toast notification appearing on the top of the IDE and containing a proposal message to start
 * current workspace and the button to perform the operation.
 *
 * @author Vitaliy Guliy
 */
@Singleton
class StartWorkspaceNotification {

  private final StartWorkspaceNotificationUiBinder uiBinder;
  private final WorkspaceStatusNotification wsStatusNotification;
  private final Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider;
  private final RestartingStateHolder restartingStateHolder;
  private final WindowClientBundle windowClientBundle;

  @UiField Button button;

  @Inject
  StartWorkspaceNotification(
      WorkspaceStatusNotification wsStatusNotification,
      StartWorkspaceNotificationUiBinder uiBinder,
      Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider,
      EventBus eventBus,
      AppContext appContext,
      RestartingStateHolder restartingStateHolder,
      WindowClientBundle windowClientBundle) {
    this.wsStatusNotification = wsStatusNotification;
    this.uiBinder = uiBinder;
    this.currentWorkspaceManagerProvider = currentWorkspaceManagerProvider;
    this.restartingStateHolder = restartingStateHolder;
    this.windowClientBundle = windowClientBundle;

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        e -> {
          WorkspaceStatus status = appContext.getWorkspace().getStatus();

          if (status == STOPPED) {
            show();
          }
        });

    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, e -> show());
  }

  /** Displays a notification with a proposal to start the current workspace. */
  void show() {
    if (restartingStateHolder.isRestarting()) {
      return;
    }
    Widget widget = uiBinder.createAndBindUi(StartWorkspaceNotification.this);
    button.addStyleName(windowClientBundle.getStyle().windowFrameFooterButtonPrimary());
    wsStatusNotification.show(WORKSPACE_STOPPED, widget);
  }

  /** Hides a notification. */
  private void hide() {
    wsStatusNotification.setSuccess(WORKSPACE_STOPPED);
  }

  @UiHandler("button")
  void startClicked(ClickEvent e) {
    hide();

    currentWorkspaceManagerProvider.get().startWorkspace();
  }

  interface StartWorkspaceNotificationUiBinder
      extends UiBinder<Widget, StartWorkspaceNotification> {}
}
