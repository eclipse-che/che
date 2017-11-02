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
package org.eclipse.che.ide.terminal;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.lib.terminal.client.TerminalResources;
import org.eclipse.che.requirejs.RequireJsLoader;

/** Terminal entry point. */
@Singleton
public class TerminalInitializer {

  private final PerspectiveManager perspectiveManager;

  @Inject
  public TerminalInitializer(
      final TerminalResources terminalResources,
      final EventBus eventBus,
      final PerspectiveManager perspectiveManager,
      final AppContext appContext,
      final TerminalInitializePromiseHolder terminalModule,
      final RequireJsLoader requireJsLoader) {
    this.perspectiveManager = perspectiveManager;
    terminalResources.getTerminalStyle().ensureInjected();

    eventBus.addHandler(WorkspaceStartingEvent.TYPE, event -> maximizeTerminal());
    eventBus.addHandler(WorkspaceRunningEvent.TYPE, event -> restoreTerminal());
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, event -> maximizeTerminal());

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        event -> {
          if (RUNNING != appContext.getWorkspace().getStatus()) {
            maximizeTerminal();
          }
        });

    Promise<Void> termInitPromise =
        AsyncPromiseHelper.createFromAsyncRequest(
            callback -> injectTerminal(requireJsLoader, callback));
    terminalModule.setInitializerPromise(termInitPromise);
  }

  private void injectTerminal(RequireJsLoader rJsLoader, final AsyncCallback<Void> callback) {
    rJsLoader.require(
        new Callback<JavaScriptObject[], Throwable>() {
          @Override
          public void onFailure(Throwable reason) {
            callback.onFailure(reason);
          }

          @Override
          public void onSuccess(JavaScriptObject[] result) {
            callback.onSuccess(null);
          }
        },
        new String[] {"term/xterm"},
        new String[] {"Xterm"});
  }

  /** Maximizes terminal. */
  private void maximizeTerminal() {
    Scheduler.get()
        .scheduleDeferred(
            () -> {
              Perspective perspective = perspectiveManager.getActivePerspective();
              if (perspective != null) {
                perspective.maximizeBottomPartStack();
              }
            });
  }

  /** Restores terminal to its default size. */
  private void restoreTerminal() {
    Perspective perspective = perspectiveManager.getActivePerspective();
    if (perspective != null) {
      perspective.restore();
    }
  }
}
