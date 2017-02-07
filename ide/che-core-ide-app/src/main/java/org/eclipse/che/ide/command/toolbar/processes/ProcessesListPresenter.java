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
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.ProcessStartedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.mvp.Presenter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<Integer, RunningProcess> runningProcesses;
    private final Map<String, StoppedProcess>  stoppedProcesses;

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

        runningProcesses = new HashMap<>();
        stoppedProcesses = new HashMap<>();

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

        eventBus.addHandler(ProcessStartedEvent.TYPE, event -> {
            updateView();
        });

        eventBus.addHandler(ProcessFinishedEvent.TYPE, event -> {
//            final RunningProcess process = runningProcesses.remove(event.getProcessID());

//            if (process != null) {
//                view.removeProcess(process);
//            }

            updateView();
        });
    }

    private void updateViewOld() {
        runningProcesses.clear();
        stoppedProcesses.clear();

        view.clearList();

        final WorkspaceRuntime runtime = appContext.getWorkspace().getRuntime();

        if (runtime != null) {
            for (final Machine machine : runtime.getMachines()) {
                execAgentCommandManager.getProcesses(machine.getId(), true).then(new Operation<List<GetProcessesResponseDto>>() {
                    @Override
                    public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                        for (GetProcessesResponseDto process : arg) {
                            if (process.isAlive()) {
                                final RunningProcess runningProcess = new RunningProcess(process.getName(),
                                                                                         process.getCommandLine(),
                                                                                         process.getNativePid(),
                                                                                         machine);
                                runningProcesses.put(runningProcess.getNativePid(), runningProcess);

                                view.addProcess(runningProcess);

                                // TODO: subscribe to running process in order to be notified when it will be stopped
                            } else {
                                if (!stoppedProcesses.containsKey(process.getName())) {
                                    final StoppedProcess stoppedProcess = new StoppedProcess(process.getName(),
                                                                                             process.getCommandLine(),
                                                                                             machine);
                                    stoppedProcesses.put(stoppedProcess.getName(), stoppedProcess);

//                                    view.addProcess(stoppedProcess);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void updateView() {
        view.clearList();

        final WorkspaceRuntime runtime = appContext.getWorkspace().getRuntime();

        if (runtime != null) {
            for (final Machine machine : runtime.getMachines()) {
                updateViewWithRunningProcesses(machine).then(new Operation<List<GetProcessesResponseDto>>() {
                    @Override
                    public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                        updateViewWithStoppedProcesses(machine);
                    }
                });
            }
        }
    }

    private Promise<List<GetProcessesResponseDto>> updateViewWithRunningProcesses(Machine machine) {
        runningProcesses.clear();

        return execAgentCommandManager.getProcesses(machine.getId(), false).then(new Operation<List<GetProcessesResponseDto>>() {
            @Override
            public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                for (GetProcessesResponseDto process : arg) {
                    final RunningProcess runningProcess = new RunningProcess(process.getName(),
                                                                             process.getCommandLine(),
                                                                             process.getNativePid(),
                                                                             machine);
                    runningProcesses.put(runningProcess.getNativePid(), runningProcess);

                    view.addProcess(runningProcess);

                    // TODO: subscribe to running process in order to be notified when it will be stopped
                }
            }
        });
    }

    private void updateViewWithStoppedProcesses(Machine machine) {
        stoppedProcesses.clear();

        execAgentCommandManager.getProcesses(machine.getId(), true).then(new Operation<List<GetProcessesResponseDto>>() {
            @Override
            public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                for (GetProcessesResponseDto process : arg) {
                    if (!process.isAlive()) {
                        if (!isCommandRunning(process.getName()) && (!stoppedProcesses.containsKey(process.getName()))) {
                            final StoppedProcess stoppedProcess = new StoppedProcess(process.getName(),
                                                                                     process.getCommandLine(),
                                                                                     machine);
                            stoppedProcesses.put(stoppedProcess.getName(), stoppedProcess);

                            view.addProcess(stoppedProcess);
                        }
                    }
                }
            }
        });
    }

    private boolean isCommandRunning(String commandName) {
        for (RunningProcess process : runningProcesses.values()) {
            if (commandName.equals(process.getName())) {
                return true;
            }
        }

        return false;
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
