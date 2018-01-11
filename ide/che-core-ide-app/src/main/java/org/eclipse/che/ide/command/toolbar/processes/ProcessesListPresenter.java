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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.agent.exec.shared.dto.GetProcessesResponseDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ProcessFinishedEvent;
import org.eclipse.che.ide.api.command.exec.ProcessStartedEvent;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.command.toolbar.CommandCreationGuide;

/** Drives the UI for displaying processes list. */
@Singleton
public class ProcessesListPresenter implements Presenter, ProcessesListView.ActionDelegate {

  private final ProcessesListView view;
  private final EventBus eventBus;
  private final ExecAgentCommandManager execAgentClient;
  private final AppContext appContext;
  private final CommandManager commandManager;
  private final Provider<CommandExecutor> commandExecutorProvider;
  private final CommandCreationGuide commandCreationGuide;

  private final Map<Integer, Process> runningProcesses;

  @Inject
  public ProcessesListPresenter(
      ProcessesListView view,
      EventBus eventBus,
      ExecAgentCommandManager execAgentClient,
      AppContext appContext,
      CommandManager commandManager,
      Provider<CommandExecutor> commandExecutorProvider,
      CommandCreationGuide commandCreationGuide) {
    this.view = view;
    this.eventBus = eventBus;
    this.execAgentClient = execAgentClient;
    this.appContext = appContext;
    this.commandManager = commandManager;
    this.commandExecutorProvider = commandExecutorProvider;
    this.commandCreationGuide = commandCreationGuide;

    view.setDelegate(this);

    runningProcesses = new HashMap<>();

    addEventHandlers();
  }

  private void addEventHandlers() {
    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> updateView());

    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          runningProcesses.clear();
          view.clearList();
        });

    eventBus.addHandler(
        ProcessStartedEvent.TYPE,
        event -> addProcessToList(event.getProcessID(), event.getMachineName()));

    eventBus.addHandler(
        ProcessFinishedEvent.TYPE,
        event -> {
          Process process = runningProcesses.get(event.getProcessID());

          if (process != null) {
            view.processStopped(process);
          }
        });

    eventBus.addHandler(
        ProcessOutputClosedEvent.TYPE,
        event -> {
          Process process = runningProcesses.get(event.getPid());

          if (process != null) {
            view.removeProcess(process);
          }
        });
  }

  /** Updates view with all running processes. */
  private void updateView() {
    view.clearList();
    runningProcesses.clear();

    final WorkspaceImpl workspace = appContext.getWorkspace();
    final RuntimeImpl runtime = workspace.getRuntime();

    if (runtime == null) {
      return;
    }

    for (MachineImpl machine : runtime.getMachines().values()) {
      execAgentClient
          .getProcesses(machine.getName(), false)
          .onSuccess(
              processes -> {
                for (GetProcessesResponseDto p : processes) {
                  final Process process =
                      new ProcessImpl(
                          p.getName(),
                          p.getCommandLine(),
                          p.getPid(),
                          p.isAlive(),
                          machine.getName());
                  runningProcesses.put(process.getPid(), process);

                  view.addProcess(process);
                }
              });
    }
  }

  /**
   * Adds process to the view.
   *
   * @param pid PID of the process to add to the view
   * @param machineName machine where process were run or currently running
   */
  private void addProcessToList(int pid, String machineName) {
    execAgentClient
        .getProcess(machineName, pid)
        .onSuccess(
            processDto -> {
              final Process process =
                  new ProcessImpl(
                      processDto.getName(),
                      processDto.getCommandLine(),
                      processDto.getPid(),
                      processDto.isAlive(),
                      machineName);
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
    commandManager
        .getCommand(process.getName())
        .ifPresent(
            command ->
                commandExecutorProvider.get().executeCommand(command, process.getMachineName()));
  }

  @Override
  public void onStopProcess(Process process) {
    execAgentClient.killProcess(process.getMachineName(), process.getPid());
  }

  @Override
  public void onCreateCommand() {
    commandCreationGuide.guide();
  }
}
