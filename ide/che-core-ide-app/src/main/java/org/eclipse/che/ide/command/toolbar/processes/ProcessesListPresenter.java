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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.ProcessStartedEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.mvp.Presenter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/** Drives the UI for displaying list of running and stopped processes. */
@Singleton
public class ProcessesListPresenter implements Presenter, ProcessesListView.ActionDelegate {

    private final ProcessesListView         view;
    private final EventBus                  eventBus;
    private final ExecAgentCommandManager   execAgentClient;
    private final AppContext                appContext;
    private final CommandManager            commandManager;
    private final Provider<CommandExecutor> commandExecutorProvider;

    private final Map<Integer, Process> runningProcesses;

    @Inject
    public ProcessesListPresenter(final ProcessesListView view,
                                  EventBus eventBus,
                                  final ExecAgentCommandManager execAgentClient,
                                  final AppContext appContext,
                                  CommandManager commandManager,
                                  Provider<CommandExecutor> commandExecutorProvider) {
        this.view = view;
        this.eventBus = eventBus;
        this.execAgentClient = execAgentClient;
        this.appContext = appContext;
        this.commandManager = commandManager;
        this.commandExecutorProvider = commandExecutorProvider;

        view.setDelegate(this);

        runningProcesses = new HashMap<>();

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                updateView();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                runningProcesses.clear();
                view.clearList();
            }
        });

        eventBus.addHandler(ProcessStartedEvent.TYPE, event -> addProcessToList(event.getProcessID(),
                                                                                event.getMachine()));

        eventBus.addHandler(ProcessFinishedEvent.TYPE, event -> {
            final Process process = runningProcesses.get(event.getProcessID());

            if (process != null) {
                view.processStopped(process);
            }
        });

        eventBus.addHandler(ProcessOutputClosedEvent.TYPE, event -> {
            final Process process = runningProcesses.get(event.getPid());

            if (process != null) {
                view.removeProcess(process);
            }
        });
    }

    /** Updates view with all running processes. */
    private void updateView() {
        view.clearList();
        runningProcesses.clear();

        final WorkspaceRuntime runtime = appContext.getActiveRuntime();

        if (runtime != null) {
            for (Machine machine : runtime.getMachines()) {
                execAgentClient.getProcesses(machine.getId(), false).then(processes -> {
                    for (GetProcessesResponseDto p : processes) {
                        final Process process = new ProcessImpl(p.getName(),
                                                                p.getCommandLine(),
                                                                p.getPid(),
                                                                p.isAlive(),
                                                                machine);
                        runningProcesses.put(process.getPid(), process);

                        view.addProcess(process);
                    }
                });
            }
        }
    }

    /**
     * Adds process to the view.
     *
     * @param pid
     *         PID of the process to add to the view
     * @param machine
     *         machine where process were run or currently running
     */
    private void addProcessToList(int pid, Machine machine) {
        execAgentClient.getProcess(machine.getId(), pid).then(processDto -> {
            final Process process = new ProcessImpl(processDto.getName(),
                                                    processDto.getCommandLine(),
                                                    processDto.getPid(),
                                                    processDto.isAlive(),
                                                    machine);
            runningProcesses.put(process.getPid(), process);

            view.addProcess(process);
        });
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onProcessChosen(Process process) {
        eventBus.fireEvent(new ActivateProcessOutputEvent(process.getPid()));
    }

    @Override
    public void onReRunProcess(Process process) {
        commandManager.getCommand(process.getName()).ifPresent(command -> {
            view.removeProcess(process);
            commandExecutorProvider.get().executeCommand(command, process.getMachine());
        });
    }

    @Override
    public void onStopProcess(Process process) {
        execAgentClient.killProcess(process.getMachine().getId(), process.getPid());
    }
}
