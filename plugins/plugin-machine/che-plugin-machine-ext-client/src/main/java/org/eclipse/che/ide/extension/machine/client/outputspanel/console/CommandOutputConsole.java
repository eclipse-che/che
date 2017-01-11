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
package org.eclipse.che.ide.extension.machine.client.outputspanel.console;

import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessSubscribeResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;

/**
 * Describes requirements for the console for command output.
 *
 */
public interface CommandOutputConsole extends OutputConsole {

    /**
     * Get command that this output console is associated with
     *
     * @return corresponding command
     */
    CommandImpl getCommand();

    /**
     * Start listening to the output on the given WebSocket channel.
     */
    @Deprecated
    void listenToOutput(String wsChannel);

    /**
     * Attaches to the process launched by the command.
     */
    @Deprecated
    void attachToProcess(MachineProcessDto process);

    /**
     * Get an output console related operations that should be performed when
     * an standard error message received
     *
     * @return operation
     */
    Operation<ProcessStdErrEventDto> getStdErrOperation();

    /**
     * Get an output console related operations that should be performed when
     * an standard output message received
     *
     * @return operation
     */
    Operation<ProcessStdOutEventDto> getStdOutOperation();

    /**
     * Get an output console related operations that should be performed when
     * a process started event caught
     *
     * @return operation
     */
    Operation<ProcessStartedEventDto> getProcessStartedOperation();

    /**
     * Get an output console related operations that should be performed when
     * a process died event caught
     *
     * @return operation
     */
    Operation<ProcessDiedEventDto> getProcessDiedOperation();

    /**
     * Get an output console related operations that should be performed when
     * a subscription to a process is performed
     *
     * @return operation
     */
    Operation<ProcessSubscribeResponseDto> getProcessSubscribeOperation();

    /**
     * Print raw string data inside the output console
     *
     * @param output
     *         output string
     */
    void printOutput(String output);
}
