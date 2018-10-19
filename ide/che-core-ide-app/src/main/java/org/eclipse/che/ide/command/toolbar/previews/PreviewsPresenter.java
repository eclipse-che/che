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
package org.eclipse.che.ide.command.toolbar.previews;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.agent.exec.shared.dto.GetProcessesResponseDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandsLoadedEvent;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ProcessFinishedEvent;
import org.eclipse.che.ide.api.command.exec.ProcessStartedEvent;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.util.browser.BrowserUtils;

/** Drives the UI for displaying preview URLs of the running processes. */
@Singleton
public class PreviewsPresenter implements Presenter, PreviewsView.ActionDelegate {

  private final PreviewsView view;
  private final ExecAgentCommandManager execAgentClient;
  private final CommandManager commandManager;
  private final AppContext appContext;
  private final Provider<MacroProcessor> macroProcessorProvider;
  private final PromiseProvider promiseProvider;
  private final ToolbarMessages messages;

  @Inject
  public PreviewsPresenter(
      PreviewsView view,
      ExecAgentCommandManager execAgentClient,
      CommandManager commandManager,
      AppContext appContext,
      EventBus eventBus,
      Provider<MacroProcessor> macroProcessorProvider,
      PromiseProvider promiseProvider,
      ToolbarMessages messages) {
    this.view = view;
    this.execAgentClient = execAgentClient;
    this.commandManager = commandManager;
    this.appContext = appContext;
    this.macroProcessorProvider = macroProcessorProvider;
    this.promiseProvider = promiseProvider;
    this.messages = messages;

    view.setDelegate(this);

    eventBus.addHandler(ProcessStartedEvent.TYPE, event -> updateView());
    eventBus.addHandler(ProcessFinishedEvent.TYPE, event -> updateView());
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, e -> updateView());
    eventBus.addHandler(CommandsLoadedEvent.getType(), e -> updateView());
  }

  /** Updates view with the preview URLs of running processes. */
  private void updateView() {
    view.removeAllURLs();

    final RuntimeImpl runtime = appContext.getWorkspace().getRuntime();

    if (runtime == null) {
      return;
    }

    runtime
        .getMachines()
        .keySet()
        .stream()
        .map(machineName -> execAgentClient.getProcesses(machineName, false))
        .forEach(
            promise ->
                promise.onSuccess(
                    processes ->
                        processes
                            .stream()
                            .map(GetProcessesResponseDto::getName)
                            .map(this::getPreviewUrlByName)
                            .forEach(it -> it.then(view::addUrl))));
  }

  /**
   * Returns promise that resolves preview URL for the given process. Returns promise that rejects
   * with an error if preview URL isn't available.
   */
  private Promise<String> getPreviewUrlByName(String name) {
    return commandManager
        .getCommand(name)
        .map(CommandImpl::getPreviewURL)
        .filter(it -> !it.isEmpty())
        .map(s -> macroProcessorProvider.get().expandMacros(s))
        .orElseGet(() -> promiseProvider.resolve(messages.previewsNotAvailableError()));
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @Override
  public void onUrlChosen(String previewUrl) {
    BrowserUtils.openInNewTab(previewUrl);
  }
}
