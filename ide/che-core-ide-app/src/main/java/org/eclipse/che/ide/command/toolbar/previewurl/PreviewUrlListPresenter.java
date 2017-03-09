/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.previewurl;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessResponseDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.ProcessStartedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.mvp.Presenter;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/** Drives the UI for displaying list of preview URLs of running processes. */
@Singleton
public class PreviewUrlListPresenter implements Presenter, PreviewUrlListView.ActionDelegate {

    private final PreviewUrlListView       view;
    private final ExecAgentCommandManager  execAgentCmdManager;
    private final CommandManager           commandManager;
    private final AppContext               appContext;
    private final Provider<MacroProcessor> macroProcessorProvider;
    private final PromiseProvider          promiseProvider;

    @Inject
    public PreviewUrlListPresenter(PreviewUrlListView view,
                                   ExecAgentCommandManager execAgentCommandManager,
                                   CommandManager commandManager,
                                   AppContext appContext,
                                   EventBus eventBus,
                                   Provider<MacroProcessor> macroProcessorProvider,
                                   PromiseProvider promiseProvider) {
        this.view = view;
        this.execAgentCmdManager = execAgentCommandManager;
        this.commandManager = commandManager;
        this.appContext = appContext;
        this.macroProcessorProvider = macroProcessorProvider;
        this.promiseProvider = promiseProvider;

        view.setDelegate(this);

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                updateView();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                view.clearList();
            }
        });

        eventBus.addHandler(ProcessStartedEvent.TYPE, event -> updateView());
        eventBus.addHandler(ProcessFinishedEvent.TYPE, event -> updateView());
    }

    /** Updates view with preview URLs of all running processes. */
    private void updateView() {
        view.clearList();

        final WorkspaceRuntime runtime = appContext.getActiveRuntime();

        if (runtime != null) {
            runtime.getMachines()
                   .forEach(machine -> execAgentCmdManager.getProcesses(machine.getId(), false)
                                                          .then(processes -> {
                                                              processes.forEach(process -> getPreviewUrl(process.getPid(), machine)
                                                                      .then(view::addUrl));
                                                          }));
        }
    }

    /**
     * Returns promise that resolves preview URL of the command which has launched
     * the process with the given {@code pid} on the specified {@code machine}.
     * Returns promise that rejects with an error if preview URL isn't available.
     */
    private Promise<String> getPreviewUrl(int pid, Machine machine) {
        return execAgentCmdManager.getProcess(machine.getId(), pid)
                                  .then((Function<GetProcessResponseDto, String>)arg -> {
                                      final Optional<ContextualCommand> commandOptional = commandManager.getCommand(arg.getName());

                                      return commandOptional.map(command -> command.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME))
                                                            .orElse(null);
                                  })
                                  .thenPromise(previewUrl -> {
                                      if (isNullOrEmpty(previewUrl)) {
                                          return promiseProvider.reject(new Exception("Preview URL is not available."));
                                      }

                                      return macroProcessorProvider.get().expandMacros(previewUrl);
                                  });
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onUrlChosen(String url) {
        Window.open(url, "_blank", null);
    }
}
