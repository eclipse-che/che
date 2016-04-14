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
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.util.UUID;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Manager for command operations.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandManager {

    private final DtoFactory                           dtoFactory;
    private final MachineServiceClient                 machineServiceClient;
    private final ConsolesPanelPresenter               consolesPanelPresenter;
    private final CommandConsoleFactory                commandConsoleFactory;
    private final NotificationManager                  notificationManager;
    private final MachineLocalizationConstant          localizationConstant;
    private final WorkspaceAgent                       workspaceAgent;
    private final AppContext                           appContext;
    private final CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry;

    @Inject
    public CommandManager(DtoFactory dtoFactory,
                          MachineServiceClient machineServiceClient,
                          ConsolesPanelPresenter consolesPanelPresenter,
                          CommandConsoleFactory commandConsoleFactory,
                          NotificationManager notificationManager,
                          MachineLocalizationConstant localizationConstant,
                          WorkspaceAgent workspaceAgent,
                          AppContext appContext,
                          CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry) {
        this.dtoFactory = dtoFactory;
        this.machineServiceClient = machineServiceClient;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.commandConsoleFactory = commandConsoleFactory;
        this.notificationManager = notificationManager;
        this.localizationConstant = localizationConstant;
        this.workspaceAgent = workspaceAgent;
        this.appContext = appContext;
        this.commandPropertyValueProviderRegistry = commandPropertyValueProviderRegistry;
    }

    /**
     * The method execute command in passed machine.
     *
     * @param command
     *         command which will be executed
     * @param machine
     *         machine in which command will be executed
     */
    public void execute(@NotNull CommandConfiguration command, @NotNull MachineDto machine) {
        executeCommand(command, machine.getId());
    }

    /** Execute the the given command configuration on the developer machine. */
    public void execute(@NotNull CommandConfiguration configuration) {
        final String devMachineId = appContext.getDevMachineId();

        executeCommand(configuration, devMachineId);
    }

    private void executeCommand(@NotNull CommandConfiguration configuration, @NotNull String machineId) {
        if (machineId == null) {
            notificationManager.notify(localizationConstant.failedToExecuteCommand(), localizationConstant.noDevMachine(), FAIL, true);
            return;
        }

        final String outputChannel = "process:output:" + UUID.uuid();

        final CommandOutputConsole console = commandConsoleFactory.create(configuration, machineId);
        console.listenToOutput(outputChannel);
        consolesPanelPresenter.addCommandOutput(machineId, console);
        workspaceAgent.setActivePart(consolesPanelPresenter);

        final String commandLine = substituteProperties(configuration.toCommandLine());

        final CommandDto command = dtoFactory.createDto(CommandDto.class)
                                             .withName(configuration.getName())
                                             .withCommandLine(commandLine)
                                             .withType(configuration.getType().getId());

        final Promise<MachineProcessDto> processPromise = machineServiceClient.executeCommand(machineId, command, outputChannel);
        processPromise.then(new Operation<MachineProcessDto>() {
            @Override
            public void apply(MachineProcessDto process) throws OperationException {
                console.attachToProcess(process);
            }
        });
    }

    /**
     * Substitutes all properties with the appropriate values in the given {@code commandLine}.
     *
     * @see CommandPropertyValueProvider
     */
    public String substituteProperties(final String commandLine) {
        String cmdLine = commandLine;

        for (CommandPropertyValueProvider provider : commandPropertyValueProviderRegistry.getProviders()) {
            cmdLine = cmdLine.replace(provider.getKey(), provider.getValue());
        }

        return cmdLine;
    }
}
