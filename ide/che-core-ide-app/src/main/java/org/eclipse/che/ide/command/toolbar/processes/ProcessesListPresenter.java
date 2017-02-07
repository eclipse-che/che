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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.mvp.Presenter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Presenter for processes list.
 */
@Singleton
public class ProcessesListPresenter implements Presenter, ProcessesListView.ActionDelegate {

    private final ProcessesListView         view;
    private final ExecAgentCommandManager   execAgentCommandManager;
    private final AppContext                appContext;
    private final CommandManager            commandManager;
    private final Provider<CommandExecutor> commandExecutorProvider;

    @Inject
    public ProcessesListPresenter(final ProcessesListView view,
                                  EventBus eventBus,
                                  final ExecAgentCommandManager execAgentCommandManager,
                                  final AppContext appContext,
                                  CommandManager commandManager,
                                  Provider<CommandExecutor> commandExecutorProvider) {
        this.view = view;
        this.execAgentCommandManager = execAgentCommandManager;
        this.appContext = appContext;
        this.commandManager = commandManager;
        this.commandExecutorProvider = commandExecutorProvider;

        view.setDelegate(this);

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                updateView();

                // TODO: listen for running/killing the processes and refresh processes list
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                view.clearList();
            }
        });

        eventBus.addHandler(ProcessFinishedEvent.TYPE, new ProcessFinishedEvent.Handler() {
            @Override
            public void onProcessFinished(ProcessFinishedEvent event) {
//                view.removeProcess(event.getProcessID());
                // TODO: remove process from the view
            }
        });
    }

    private void updateView() {
        final WorkspaceRuntime runtime = appContext.getWorkspace().getRuntime();

        if (runtime != null) {
            for (final Machine machine : runtime.getMachines()) {
                execAgentCommandManager.getProcesses(machine.getId(), true).then(new Operation<List<GetProcessesResponseDto>>() {
                    @Override
                    public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                        for (GetProcessesResponseDto process : arg) {
                            if (process.isAlive()) {
                                view.addProcess(new RunningProcess(process.getName(),
                                                                   process.getCommandLine(),
                                                                   process.getNativePid(),
                                                                   machine));
                            } else {
                                view.addProcess(new StoppedProcess(process.getName(), process.getCommandLine(), machine));
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onProcessChosen() {
        // TODO: open processes output
    }

    @Override
    public void onReRunProcess(StoppedProcess process) {
        final ContextualCommand command = commandManager.getCommand(process.getName());

        if (command != null) {
            commandExecutorProvider.get().executeCommand(command, process.getMachine());
        }
    }

    @Override
    public void onStopProcess(RunningProcess process) {
        execAgentCommandManager.killProcess(process.getMachine().getId(), process.getNativePid());
    }
}
