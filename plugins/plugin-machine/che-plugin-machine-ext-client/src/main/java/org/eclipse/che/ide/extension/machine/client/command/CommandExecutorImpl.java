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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

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

    @Inject
    public CommandExecutorImpl(MacroProcessor macroProcessor,
                               CommandConsoleFactory commandConsoleFactory,
                               ProcessesPanelPresenter processesPanelPresenter,
                               ExecAgentCommandManager execAgentCommandManager) {
        this.macroProcessor = macroProcessor;
        this.commandConsoleFactory = commandConsoleFactory;
        this.processesPanelPresenter = processesPanelPresenter;
        this.execAgentCommandManager = execAgentCommandManager;
    }

    @Override
    public void executeCommand(final CommandImpl command, final Machine machine) {
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
}
