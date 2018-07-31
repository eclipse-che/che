/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.statepersistance;

import static elemental.events.Event.BLUR;
import static org.eclipse.che.ide.util.dom.Elements.getWindow;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import elemental.events.EventRemover;
import elemental.json.JsonObject;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.WindowActionEvent;
import org.eclipse.che.ide.api.WindowActionHandler;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppingEvent;

/**
 * Contains handlers to track app state and persist/restore IDE state across sessions.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class AppStateTracker
    implements WindowActionHandler,
        PartStackStateChangedEvent.Handler,
        WorkspaceStoppingEvent.Handler {

  private EventBus eventBus;
  private AppContext appContext;
  private AppStateSyncWriter appStateSyncWriter;
  private AppStateManager appStateManager;
  private AppStateBackwardCompatibility appStateBackwardCompatibility;

  private EventRemover blurEventRemover;
  private HandlerRegistration windowEventsRemover;
  private HandlerRegistration partStackStateEventRemover;
  private HandlerRegistration workspaceStoppingEventRemover;

  private final DelayedTask restoreStateTask =
      new DelayedTask() {
        @Override
        public void onExecute() {
          restoreState();
        }
      };

  private final DelayedTask persistStateTask =
      new DelayedTask() {
        @Override
        public void onExecute() {
          appStateManager.persistState();
        }
      };

  @Inject
  public AppStateTracker(
      EventBus eventBus,
      AppContext appContext,
      AppStateSyncWriter appStateSyncWriter,
      AppStateManager appStateManager,
      AppStateBackwardCompatibility appStateBackwardCompatibility) {
    this.eventBus = eventBus;
    this.appContext = appContext;
    this.appStateSyncWriter = appStateSyncWriter;
    this.appStateManager = appStateManager;
    this.appStateBackwardCompatibility = appStateBackwardCompatibility;

    eventBus.addHandler(WorkspaceReadyEvent.getType(), this::onWorkspaceReady);
  }

  private void onWorkspaceReady(WorkspaceReadyEvent event) {
    // delay is required because we need to wait some time while different components initialized
    restoreStateTask.delay(1_000);
  }

  @Override
  public void onWorkspaceStopping(WorkspaceStoppingEvent event) {
    cleanUpHandlers();
  }

  @Override
  public void onWindowClosing(WindowActionEvent event) {
    if (appContext.getWorkspace() == null) {
      return;
    }

    JsonObject appState = appStateManager.collectAppStateData();
    if (appState.keys().length > 0) {
      appStateSyncWriter.saveState(appState);
    }
  }

  @Override
  public void onPartStackStateChanged(PartStackStateChangedEvent event) {
    persistStateTask.delay(500);
  }

  private void restoreState() {
    JsonObject appStateFromPreferences = appStateBackwardCompatibility.getAppState();
    if (appStateFromPreferences != null) {
      appStateManager
          .restoreState(appStateFromPreferences)
          .then(
              arg -> {
                addHandlers();
              });

      appStateBackwardCompatibility.removeAppState();
    } else {
      appStateManager
          .readState()
          .then(
              arg -> {
                appStateManager
                    .restoreState()
                    .then(
                        aVoid -> {
                          addHandlers();
                        });
              });
    }
  }

  private void addHandlers() {
    cleanUpHandlers();

    partStackStateEventRemover = eventBus.addHandler(PartStackStateChangedEvent.TYPE, this);
    blurEventRemover = getWindow().addEventListener(BLUR, evt -> appStateManager.persistState());
    windowEventsRemover = eventBus.addHandler(WindowActionEvent.TYPE, this);
    workspaceStoppingEventRemover = eventBus.addHandler(WorkspaceStoppingEvent.TYPE, this);
  }

  private void cleanUpHandlers() {
    if (partStackStateEventRemover != null) {
      partStackStateEventRemover.removeHandler();
    }

    if (blurEventRemover != null) {
      blurEventRemover.remove();
    }

    if (windowEventsRemover != null) {
      windowEventsRemover.removeHandler();
    }

    if (workspaceStoppingEventRemover != null) {
      workspaceStoppingEventRemover.removeHandler();
    }
  }

  @Override
  public void onWindowClosed(WindowActionEvent event) {}
}
