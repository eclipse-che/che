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
package org.eclipse.che.ide.command.toolbar.previews;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessResponseDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.ProcessStartedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/** Drives the UI for displaying preview URLs of the running processes. */
@Singleton
public class PreviewsPresenter implements Presenter, PreviewsView.ActionDelegate {

    private final PreviewsView             view;
    private final ExecAgentCommandManager  execAgentClient;
    private final CommandManager           commandManager;
    private final AppContext               appContext;
    private final Provider<MacroProcessor> macroProcessorProvider;
    private final PromiseProvider          promiseProvider;
    private final ToolbarMessages          messages;

    @Inject
    public PreviewsPresenter(PreviewsView view,
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

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                updateView();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                view.removeAll();
            }
        });

        eventBus.addHandler(ProcessStartedEvent.TYPE, event -> updateView());
        eventBus.addHandler(ProcessFinishedEvent.TYPE, event -> updateView());
    }

    /** Updates view with preview URLs of all running processes. */
    private void updateView() {
        view.removeAll();

        final WorkspaceRuntime runtime = appContext.getActiveRuntime();

        if (runtime != null) {
            runtime.getMachines().forEach(machine -> execAgentClient.getProcesses(machine.getId(), false).then(processes -> {
                processes.forEach(process -> getPreviewUrl(process.getPid(), machine).then(view::addUrl));
            }));
        }
    }

    /**
     * Returns promise that resolves preview URL of the command which has launched
     * the process with the given {@code pid} on the specified {@code machine}.
     * Returns promise that rejects with an error if preview URL isn't available.
     */
    private Promise<PreviewUrl> getPreviewUrl(int pid, Machine machine) {
        return execAgentClient.getProcess(machine.getId(), pid)
                              // get command's preview URL
                              .then((Function<GetProcessResponseDto, String>)process -> {
                                  final Optional<CommandImpl> commandOptional = commandManager.getCommand(process.getName());

                                  return commandOptional.map(command -> command.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME))
                                                        .orElse(null);
                              })
                              // expand macros used in preview URL
                              .thenPromise(previewUrl -> {
                                  if (!isNullOrEmpty(previewUrl)) {
                                      return macroProcessorProvider.get().expandMacros(previewUrl);
                                  }
                                  return promiseProvider.reject(new Exception(messages.previewsNotAvailableError()));
                              })
                              // compose preview URL's display name
                              .then((Function<String, PreviewUrl>)previewUrl -> new PreviewUrl(previewUrl,
                                                                                               getPreviewUrlDisplayName(previewUrl)
                                                                                                       .orElse(previewUrl)));
    }

    private Optional<String> getPreviewUrlDisplayName(String previewUrl) {
        final DevMachine devMachine = appContext.getDevMachine();
        final Map<String, ? extends Server> servers = devMachine.getRuntime().getServers();

        for (Map.Entry<String, ? extends Server> entry : servers.entrySet()) {
            Server server = entry.getValue();
            String serverUrl = server.getUrl();

            if (previewUrl.startsWith(serverUrl)) {
                String displayName = previewUrl.replace(serverUrl, devMachine.getDisplayName() + ':' + entry.getKey());

                // cut protocol from display name
                final int protocolIndex = displayName.lastIndexOf('/');
                if (protocolIndex > -1) {
                    displayName = displayName.substring(0, protocolIndex);
                }

                return Optional.of(displayName);
            }
        }

        return Optional.empty();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onUrlChosen(PreviewUrl previewUrl) {
        Window.open(previewUrl.getUrl(), "_blank", null);
    }
}
