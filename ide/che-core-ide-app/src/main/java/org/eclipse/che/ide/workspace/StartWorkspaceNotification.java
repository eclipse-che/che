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

  @UiField Button button;

  @Inject
  StartWorkspaceNotification(
      WorkspaceStatusNotification wsStatusNotification,
      StartWorkspaceNotificationUiBinder uiBinder,
      Provider<CurrentWorkspaceManager> currentWorkspaceManagerProvider,
      EventBus eventBus,
      AppContext appContext) {
    this.wsStatusNotification = wsStatusNotification;
    this.uiBinder = uiBinder;
    this.currentWorkspaceManagerProvider = currentWorkspaceManagerProvider;

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
    Widget widget = uiBinder.createAndBindUi(StartWorkspaceNotification.this);
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
