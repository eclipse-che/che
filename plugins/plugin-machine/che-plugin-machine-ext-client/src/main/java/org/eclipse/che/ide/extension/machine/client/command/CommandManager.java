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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
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
import java.util.Iterator;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Manager for command operations.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
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
    public void execute(@NotNull CommandConfiguration command, @NotNull Machine machine) {
        executeCommand(command, machine);
    }

    /** Execute the the given command configuration on the developer machine. */
    public void execute(@NotNull CommandConfiguration configuration) {
        final Machine devMachine = appContext.getDevMachine().getDescriptor();
        executeCommand(configuration, devMachine);
    }

    public void executeCommand(@NotNull final CommandConfiguration configuration, @NotNull final Machine machine) {
        if (machine == null) {
            notificationManager.notify(localizationConstant.failedToExecuteCommand(),
                                       localizationConstant.noDevMachine(),
                                       FAIL,
                                       FLOAT_MODE);
            return;
        }

        final String outputChannel = "process:output:" + UUID.uuid();

        final CommandOutputConsole console = commandConsoleFactory.create(configuration, machine);
        console.listenToOutput(outputChannel);
        consolesPanelPresenter.addCommandOutput(machine.getId(), console);
        workspaceAgent.setActivePart(consolesPanelPresenter);

        substituteProperties(configuration.toCommandLine()).then(new Operation<String>() {
            @Override
            public void apply(String arg) throws OperationException {
                final CommandDto command = dtoFactory.createDto(CommandDto.class)
                                                     .withName(configuration.getName())
                                                     .withCommandLine(arg)
                                                     .withType(configuration.getType().getId());

                final Promise<MachineProcessDto> processPromise = machineServiceClient.executeCommand(machine.getId(), command, outputChannel);
                processPromise.then(new Operation<MachineProcessDto>() {
                    @Override
                    public void apply(MachineProcessDto process) throws OperationException {
                        console.attachToProcess(process);
                    }
                });
            }
        });
    }

    /**
     * Substitutes all properties with the appropriate values in the given {@code commandLine}.
     *
     * @see CommandPropertyValueProvider
     */
    public Promise<String> substituteProperties(String commandLine) {
        Promise<String> promise = Promises.resolve(null);
        CommandLineContainer commandLineContainer = new CommandLineContainer(commandLine);
        return replaceParameters(promise, commandLineContainer, commandPropertyValueProviderRegistry.getProviders().iterator());
    }

    private Promise<String> replaceParameters(Promise<String> promise,
                                              CommandLineContainer commandLineContainer,
                                              Iterator<CommandPropertyValueProvider> iterator) {
        if (!iterator.hasNext()) {
            return promise;
        }

        final CommandPropertyValueProvider provider = iterator.next();

        Promise<String> derivedPromise = promise.thenPromise(proceedRefactoringMove(commandLineContainer, provider));

        return replaceParameters(derivedPromise, commandLineContainer, iterator);
    }

    private Function<String, Promise<String>> proceedRefactoringMove(final CommandLineContainer commandLineContainer,
                                                                     final CommandPropertyValueProvider provider) {
        return new Function<String, Promise<String>>() {
            @Override
            public Promise<String> apply(String arg) throws FunctionException {
                return provider.getValue().thenPromise(new Function<String, Promise<String>>() {
                    @Override
                    public Promise<String> apply(String arg) throws FunctionException {
                        commandLineContainer.setCommandLine(commandLineContainer.getCommandLine().replace(provider.getKey(), arg));
                        return Promises.resolve(commandLineContainer.getCommandLine());
                    }
                });
            }
        };
    }

    private class CommandLineContainer {
        private String commandLine;

        public CommandLineContainer(String commandLine) {
            this.commandLine = commandLine;
        }

        public String getCommandLine() {
            return commandLine;
        }

        public void setCommandLine(String commandLine) {
            this.commandLine = commandLine;
        }
    }
}
