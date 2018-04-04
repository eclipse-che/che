/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.execute;

import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.CommandOutputConsole;
import org.eclipse.che.ide.machine.chooser.MachineChooser;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/** Implementation of {@link CommandExecutor}. */
public class CommandExecutorImpl implements CommandExecutor {

  private final MacroProcessor macroProcessor;
  private final CommandConsoleFactory commandConsoleFactory;
  private final ProcessesPanelPresenter processesPanelPresenter;
  private final ExecAgentCommandManager execAgentClient;
  private final MachineChooser machineChooser;
  private final SelectionAgent selectionAgent;

  @Inject
  public CommandExecutorImpl(
      MacroProcessor macroProcessor,
      CommandConsoleFactory commandConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter,
      ExecAgentCommandManager execAgentClient,
      MachineChooser machineChooser,
      SelectionAgent selectionAgent) {
    this.macroProcessor = macroProcessor;
    this.commandConsoleFactory = commandConsoleFactory;
    this.processesPanelPresenter = processesPanelPresenter;
    this.execAgentClient = execAgentClient;
    this.machineChooser = machineChooser;
    this.selectionAgent = selectionAgent;
  }

  @Override
  public void executeCommand(Command command, String machineName) {
    final String name = command.getName();
    final String type = command.getType();
    final String commandLine = command.getCommandLine();
    final Map<String, String> attributes = command.getAttributes();

    macroProcessor
        .expandMacros(commandLine)
        .then(
            expandedCommandLine -> {
              final CommandImpl expandedCommand =
                  new CommandImpl(name, expandedCommandLine, type, attributes);
              final CommandOutputConsole console =
                  commandConsoleFactory.create(expandedCommand, machineName);

              processesPanelPresenter.addCommandOutput(machineName, console, true);

              execAgentClient
                  .startProcess(machineName, expandedCommand)
                  .thenIfProcessStartedEvent(console.getProcessStartedConsumer())
                  .thenIfProcessDiedEvent(console.getProcessDiedConsumer())
                  .thenIfProcessStdOutEvent(console.getStdOutConsumer())
                  .thenIfProcessStdErrEvent(console.getStdErrConsumer());
            });
  }

  @Override
  public void executeCommand(CommandImpl command) {
    final MachineImpl selectedMachine = getSelectedMachine();

    if (selectedMachine != null) {
      executeCommand(command, selectedMachine.getName());
    } else {
      machineChooser
          .show()
          .then(
              machine -> {
                executeCommand(command, machine.getName());
              });
    }
  }

  /** Returns the currently selected machine or {@code null} if none. */
  @Nullable
  private MachineImpl getSelectedMachine() {
    final Selection<?> selection = selectionAgent.getSelection();

    if (selection != null && !selection.isEmpty() && selection.isSingleSelection()) {
      final Object possibleNode = selection.getHeadElement();

      if (possibleNode instanceof MachineImpl) {
        return (MachineImpl) possibleNode;
      }
    }

    return null;
  }
}
