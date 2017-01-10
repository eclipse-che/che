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
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import java.net.Socket;

/**
 * Verifies that agent was started successfully by checking that specified local port is listened in a machine.
 *
 * @author Alexander Garagatyi
 */
public class MappedPortIsListeningAgentChecker implements AgentLaunchingChecker {
    private final String exposedPort;

    public MappedPortIsListeningAgentChecker(String exposedPort) {
        // normalize port/transport value
        this.exposedPort = exposedPort.contains("/") ? exposedPort : exposedPort + "/tcp";
    }

    @Override
    public boolean isLaunched(Agent agent, InstanceProcess process, Instance machine) throws MachineException {
        Server server = machine.getRuntime().getServers().get(exposedPort);
        if (server != null) {
            try {
                String[] hostPort = server.getProperties().getInternalAddress().split(":");
                try (@SuppressWarnings("unused") Socket socket = new Socket(hostPort[0],
                                                                            Integer.parseInt(hostPort[1]))) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
