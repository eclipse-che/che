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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Client for Machine API.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public interface MachineServiceClient {

    /**
     * Get machine information by it's id.
     *
     * @param machineId
     *         ID of the machine
     * @return a promise that resolves to the {@link MachineDto}, or rejects with an error
     */
    Promise<MachineDto> getMachine(@NotNull String machineId);

    /**
     * Returns list of machines which are bounded to the specified workspace.
     *
     * @param workspaceId
     *         workspace id
     * @return a promise that will provide a list of {@link MachineDto}s for the given workspace ID, or rejects with an error
     */
    Promise<List<MachineDto>> getMachines(String workspaceId);

    /**
     * Destroy machine with the specified ID.
     *
     * @param machineId
     *         ID of machine that should be destroyed
     * @return a promise that will resolve when the machine has been destroyed, or rejects with an error
     */
    Promise<Void> destroyMachine(@NotNull String machineId);

    /**
     * Execute a command in machine.
     *
     * @param machineId
     *         ID of the machine where command should be executed
     * @param command
     *         the command that should be executed in the machine
     * @param outputChannel
     *         websocket chanel for execution logs
     * @return a promise that resolves to the {@link MachineProcessDto}, or rejects with an error
     */
    Promise<MachineProcessDto> executeCommand(@NotNull String machineId,
                                              @NotNull Command command,
                                              @Nullable String outputChannel);

    /**
     * Get processes from the specified machine.
     *
     * @param machineId
     *         ID of machine to get processes information from
     * @return a promise that will provide a list of {@link MachineProcessDto}s for the given machine ID
     */
    Promise<List<MachineProcessDto>> getProcesses(@NotNull String machineId);

    /**
     * Stop process in machine.
     *
     * @param machineId
     *         ID of the machine where process should be stopped
     * @param processId
     *         ID of the process to stop
     * @return a promise that will resolve when the process has been stopped, or rejects with an error
     */
    Promise<Void> stopProcess(@NotNull String machineId, int processId);

    /**
     * Get file content.
     *
     * @param machineId
     *         ID of the machine
     * @param path
     *         path to file on machine instance
     * @param startFrom
     *         line number to start reading from
     * @param limit
     *         limitation on line
     * @return a promise that will provide the file content, or rejects with an error
     */
    Promise<String> getFileContent(@NotNull String machineId, @NotNull String path, int startFrom, int limit);
}
