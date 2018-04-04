/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.WORKSPACE_AGENT_STOPPED;

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
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.ui.window.WindowClientBundle;

/** Notification to show that workspace agent is shut down and may be restarted */
@Singleton
class StartWsAgentNotification {

  private final StartWsAgentNotificationUiBinder uiBinder;
  private final WorkspaceStatusNotification wsStatusNotification;
  private final Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider;
  private final EventBus eventBus;
  private final RestartingStateHolder restartingStateHolder;
  private final WindowClientBundle windowClientBundle;

  @UiField Button ignoreButton;
  @UiField Button restartButton;

  @Inject
  StartWsAgentNotification(
      WorkspaceStatusNotification wsStatusNotification,
      StartWsAgentNotificationUiBinder uiBinder,
      Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider,
      EventBus eventBus,
      RestartingStateHolder restartingStateHolder,
      WindowClientBundle windowClientBundle) {
    this.wsStatusNotification = wsStatusNotification;
    this.uiBinder = uiBinder;
    this.currentWorkspaceManagerProvider = currentWorkspaceManagerProvider;
    this.eventBus = eventBus;
    this.restartingStateHolder = restartingStateHolder;
    this.windowClientBundle = windowClientBundle;

    eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, e -> show());
  }

  void show() {
    Widget widget = uiBinder.createAndBindUi(StartWsAgentNotification.this);
    ignoreButton.addStyleName(windowClientBundle.getStyle().windowFrameFooterButtonPrimary());
    restartButton.addStyleName(windowClientBundle.getStyle().windowFrameFooterButtonPrimary());
    wsStatusNotification.show(WORKSPACE_AGENT_STOPPED, widget);
  }

  /** Hides a notification. */
  private void hide() {
    wsStatusNotification.setSuccess(WORKSPACE_AGENT_STOPPED);
  }

  @UiHandler("ignoreButton")
  void ignoreButtonClicked(ClickEvent e) {
    hide();
  }

  @UiHandler("restartButton")
  void restartButtonClicked(ClickEvent e) {
    hide();

    restartingStateHolder.setRestartingState(true);

    currentWorkspaceManagerProvider.get().stopWorkspace();

    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        ignore -> {
          if (restartingStateHolder.isRestarting()) {
            currentWorkspaceManagerProvider
                .get()
                .startWorkspace()
                .then(
                    ignoreAgain -> {
                      restartingStateHolder.setRestartingState(false);
                    });
          }
        });
  }

  interface StartWsAgentNotificationUiBinder extends UiBinder<Widget, StartWsAgentNotification> {}
}
