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
package org.eclipse.che.api.agent.server.launcher;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import static java.lang.String.format;

/**
 * Verifies if agent installed a command with specific name.
 * It is an indicator that agent start had been finished.
 *
 * @author Alexander Garagatyi
 */
public class CommandExistsAgentChecker implements AgentLaunchingChecker {

    private static final String CHECK_COMMAND = "command -v %1$s >/dev/null 2>&1 && echo 0 || echo 1";
    private final String checkingCommand;
    private       long   counter;

    public CommandExistsAgentChecker(String commandToCheck) {
        this.checkingCommand = format(CHECK_COMMAND, commandToCheck);
    }

    @Override
    public boolean isLaunched(Agent agent, InstanceProcess process, Instance machine) throws MachineException {
        Command command = new CommandImpl(format("Wait for %s, try %d", agent.getId(), ++counter),
                                          checkingCommand,
                                          "test");

        try (ListLineConsumer lineConsumer = new ListLineConsumer()) {
            InstanceProcess waitProcess = machine.createProcess(command, null);
            waitProcess.start(lineConsumer);
            return lineConsumer.getText().endsWith("[STDOUT] 0");
        } catch (ConflictException e) {
            throw new MachineException(e.getServiceError());
        }
    }
}
