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
package org.eclipse.che.ide.extension.machine.client.command;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link CommandManager}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandManagerImpl implements CommandManager {

    public static final String PREVIEW_URL_ATTR = "previewUrl";

    private final CommandTypeRegistry     commandTypeRegistry;
    private final AppContext              appContext;
    private final WorkspaceServiceClient  workspaceServiceClient;
    private final DtoFactory              dtoFactory;
    private final MacroProcessor          macroProcessor;
    private final CommandConsoleFactory   commandConsoleFactory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final ExecAgentCommandManager execAgentCommandManager;

    private final Map<String, CommandImpl>    commands;
    private final Set<CommandChangedListener> commandChangedListeners;

    @Inject
    public CommandManagerImpl(CommandTypeRegistry commandTypeRegistry,
                              AppContext appContext,
                              WorkspaceServiceClient workspaceServiceClient,
                              DtoFactory dtoFactory,
                              MacroProcessor macroProcessor,
                              CommandConsoleFactory commandConsoleFactory,
                              ProcessesPanelPresenter processesPanelPresenter,
                              ExecAgentCommandManager execAgentCommandManager) {
        this.commandTypeRegistry = commandTypeRegistry;
        this.appContext = appContext;
        this.workspaceServiceClient = workspaceServiceClient;
        this.dtoFactory = dtoFactory;
        this.macroProcessor = macroProcessor;
        this.commandConsoleFactory = commandConsoleFactory;
        this.processesPanelPresenter = processesPanelPresenter;
        this.execAgentCommandManager = execAgentCommandManager;

        commands = new HashMap<>();
        commandChangedListeners = new HashSet<>();
        retrieveAllCommands();
    }

    private void retrieveAllCommands() {
        workspaceServiceClient.getCommands(appContext.getWorkspaceId()).then(new Operation<List<CommandDto>>() {
            @Override
            public void apply(List<CommandDto> arg) throws OperationException {
                for (Command command : arg) {
                    commands.put(command.getName(), new CommandImpl(command));
                }
            }
        });
    }

    @Override
    public List<CommandImpl> getCommands() {
        // return copy of the commands in order to prevent it modification directly
        List<CommandImpl> list = new ArrayList<>(commands.size());
        for (CommandImpl command : commands.values()) {
            list.add(new CommandImpl(command));
        }

        return list;
    }

    @Override
    public Promise<CommandImpl> create(String type) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(type);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(PREVIEW_URL_ATTR, commandType.getPreviewUrlTemplate());

        final CommandImpl command = new CommandImpl(getUniqueCommandName(type, null),
                                                    commandType.getCommandLineTemplate(),
                                                    type,
                                                    attributes);
        return add(command);
    }

    @Override
    public Promise<CommandImpl> create(String desirableName, String commandLine, String type, Map<String, String> attributes) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(type);

        Map<String, String> attr = (attributes != null) ? attributes : new HashMap<String, String>();
        attr.put(PREVIEW_URL_ATTR, commandType.getPreviewUrlTemplate());

        final CommandImpl command = new CommandImpl(getUniqueCommandName(type, desirableName),
                                                    commandLine != null ? commandLine : commandType.getCommandLineTemplate(),
                                                    type,
                                                    attr);
        return add(command);
    }

    private Promise<CommandImpl> add(final CommandImpl command) {
        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(getUniqueCommandName(command.getType(), command.getName()))
                                                .withCommandLine(command.getCommandLine())
                                                .withType(command.getType())
                                                .withAttributes(command.getAttributes());

        return workspaceServiceClient.addCommand(appContext.getWorkspaceId(), commandDto).then(new Function<WorkspaceDto, CommandImpl>() {
            @Override
            public CommandImpl apply(WorkspaceDto arg) throws FunctionException {
                final CommandImpl newCommand = new CommandImpl(command);
                newCommand.setName(commandDto.getName());

                commands.put(newCommand.getName(), newCommand);

                fireCommandAdded(newCommand);

                return newCommand;
            }
        });
    }

    @Override
    public Promise<CommandImpl> update(final String commandName, final CommandImpl command) {
        final String name;
        if (commandName.equals(command.getName())) {
            name = commandName;
        } else {
            name = getUniqueCommandName(command.getType(), command.getName());
        }

        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(name)
                                                .withCommandLine(command.getCommandLine())
                                                .withType(command.getType())
                                                .withAttributes(command.getAttributes());

        return workspaceServiceClient.updateCommand(appContext.getWorkspaceId(), commandName, commandDto)
                                     .then(new Function<WorkspaceDto, CommandImpl>() {
                                         @Override
                                         public CommandImpl apply(WorkspaceDto arg) throws FunctionException {
                                             final CommandImpl updatedCommand = new CommandImpl(commandDto.getName(),
                                                                                                command.getCommandLine(),
                                                                                                command.getType(),
                                                                                                command.getAttributes());
                                             commands.remove(commandName);
                                             commands.put(updatedCommand.getName(), updatedCommand);

                                             fireCommandUpdated(updatedCommand);

                                             return updatedCommand;
                                         }
                                     });
    }

    @Override
    public Promise<Void> remove(final String name) {
        return workspaceServiceClient.deleteCommand(appContext.getWorkspaceId(), name).then(new Function<WorkspaceDto, Void>() {
            @Override
            public Void apply(WorkspaceDto arg) throws FunctionException {
                fireCommandRemoved(commands.remove(name));
                return null;
            }
        });
    }

    @Override
    public List<CommandPage> getPages(String type) {
        CommandType commandType = commandTypeRegistry.getCommandTypeById(type);
        return commandType != null ? commandType.getPages() : Collections.<CommandPage>emptyList();
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

    @Override
    public void addCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.add(listener);
    }

    @Override
    public void removeCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.remove(listener);
    }

    private void fireCommandAdded(CommandImpl command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandAdded(command);
        }
    }

    private void fireCommandRemoved(CommandImpl command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandRemoved(command);
        }
    }

    private void fireCommandUpdated(CommandImpl command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandUpdated(command);
        }
    }

    private String getUniqueCommandName(String customType, String customName) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(customType);
        final Set<String> commandNames = commands.keySet();

        final String newCommandName;

        if (customName == null || customName.isEmpty()) {
            newCommandName = "new" + commandType.getDisplayName();
        } else {
            if (!commandNames.contains(customName)) {
                return customName;
            }
            newCommandName = customName + " copy";
        }

        if (!commandNames.contains(newCommandName)) {
            return newCommandName;
        }

        for (int count = 1; count < 1000; count++) {
            if (!commandNames.contains(newCommandName + "-" + count)) {
                return newCommandName + "-" + count;
            }
        }

        return newCommandName;
    }
}
