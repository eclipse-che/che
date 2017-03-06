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
package org.eclipse.che.plugin.nativeaccessexample.machine.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.execagent.ExecAgentPromise;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.UUID;

import javax.validation.constraints.NotNull;

/**
 * Simple command manager which allows to run native commands within the workspace Docker container.
 * Please note that the actual call is delegated to the MachineServiceClient service.
 *
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Singleton
public class CommandManager {

    private final DtoFactory              dtoFactory;
    private final ExecAgentCommandManager commandManager;
    private final NotificationManager     notificationManager;
    private final AppContext              appContext;

    @Inject
    public CommandManager(DtoFactory dtoFactory, ExecAgentCommandManager commandManager, NotificationManager notificationManager,
                          AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.commandManager = commandManager;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
    }

    /**
     * Execute the the given command command within the workspace Docker container.
     */
    public void execute(String commandLine) {
        final Machine machine = appContext.getDevMachine().getDescriptor();
        if (machine == null) {
            return;
        }
        String machineID = machine.getId();
        final CommandDto command = dtoFactory.createDto(CommandDto.class)
                .withName("some-command")
                .withCommandLine(commandLine)
                .withType("arbitrary-type");
        executeCommand(command, machineID);
    }

    public void executeCommand(final CommandDto command, @NotNull final String machineID) {
        final ExecAgentPromise<ProcessStartResponseDto> promise = commandManager.startProcess(machineID, command);

        promise.then(new Operation<ProcessStartResponseDto>() {
            @Override
            public void apply(ProcessStartResponseDto arg) throws OperationException {
                //Do nothing in this example
            }
        });

    }
}
