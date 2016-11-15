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
package org.eclipse.che.api.agent.server.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Launches agent and waits while it is finished.
 *
 * This agents is suited only for those types of agents that install software
 * and finish working without launching any processes at the end.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class DefaultAgentLauncher implements AgentLauncher {
    @Inject
    public DefaultAgentLauncher() { }

    @Override
    public void launch(Instance machine, Agent agent) throws ServerException {
        if (isNullOrEmpty(agent.getScript())) {
            return;
        }
        final Command command = new CommandImpl(agent.getId(), agent.getScript(), "agent");
        final InstanceProcess process = machine.createProcess(command, null);
        final LineConsumer lineConsumer = new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                machine.getLogger().writeLine(line);
            }
        };

        try {
            process.start(lineConsumer);
        } catch (ConflictException e) {
            try {
                machine.getLogger().writeLine(format("[ERROR] %s", e.getMessage()));
            } catch (IOException ignored) {
            }
        } finally {
            try {
                lineConsumer.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public String getAgentId() {
        return "any";
    }

    @Override
    public String getMachineType() {
        return "any";
    }
}
