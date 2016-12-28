/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.command;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.machine.chooser.MachineChooser;

import java.util.Map;

/**
 * Implementation of {@link CommandExecutor}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandExecutorImpl implements CommandExecutor {

    private final MacroProcessor          macroProcessor;
    private final CommandConsoleFactory   commandConsoleFactory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final ExecAgentCommandManager execAgentCommandManager;
    private final MachineChooser          machineChooser;
    private final SelectionAgent          selectionAgent;

    @Inject
    public CommandExecutorImpl(MacroProcessor macroProcessor,
                               CommandConsoleFactory commandConsoleFactory,
                               ProcessesPanelPresenter processesPanelPresenter,
                               ExecAgentCommandManager execAgentCommandManager,
                               MachineChooser machineChooser,
                               SelectionAgent selectionAgent) {
        this.macroProcessor = macroProcessor;
        this.commandConsoleFactory = commandConsoleFactory;
        this.processesPanelPresenter = processesPanelPresenter;
        this.execAgentCommandManager = execAgentCommandManager;
        this.machineChooser = machineChooser;
        this.selectionAgent = selectionAgent;
    }

    @Override
    public void executeCommand(Command command, final Machine machine) {
        final String name = command.getName();
        final String type = command.getType();
        final String commandLine = command.getCommandLine();
        final Map<String, String> attributes = command.getAttributes();

        macroProcessor.expandMacros(commandLine).then(new Operation<String>() {
            @Override
            public void apply(String expandedCommandLine) throws OperationException {
                CommandImpl expandedCommand = new CommandImpl(name, expandedCommandLine, type, attributes);

                final CommandOutputConsole console = commandConsoleFactory.create(expandedCommand, machine);
                final String machineId = machine.getId();

                processesPanelPresenter.addCommandOutput(machineId, console);

                execAgentCommandManager.startProcess(machineId, expandedCommand)
                                       .thenIfProcessStartedEvent(console.getProcessStartedOperation())
                                       .thenIfProcessDiedEvent(console.getProcessDiedOperation())
                                       .thenIfProcessStdOutEvent(console.getStdOutOperation())
                                       .thenIfProcessStdErrEvent(console.getStdErrOperation());
            }
        });
    }

    @Override
    public void executeCommand(final ContextualCommand command) {
        final Machine selectedMachine = getSelectedMachine();

        if (selectedMachine != null) {
            executeCommand(command, selectedMachine);
        } else {
            machineChooser.show().then(new Operation<Machine>() {
                @Override
                public void apply(Machine arg) throws OperationException {
                    executeCommand(command, arg);
                }
            });
        }
    }

    /** Returns the currently selected machine or {@code null} if none. */
    @Nullable
    private Machine getSelectedMachine() {
        final Selection<?> selection = selectionAgent.getSelection();

        if (selection != null && !selection.isEmpty() && selection.isSingleSelection()) {
            final Object possibleNode = selection.getHeadElement();

            if (possibleNode instanceof Machine) {
                return (Machine)possibleNode;
            }
        }

        return null;
    }
}
