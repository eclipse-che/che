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
package org.eclipse.che.workspace.infrastructure.docker.agents;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;
import org.eclipse.che.workspace.infrastructure.docker.old.DockerProcess;

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
    public boolean isLaunched(Agent agent, DockerProcess process, DockerService machine) throws
                                                                                         InfrastructureException {
//        Server server = machine.getRuntime().getServers().get(exposedPort);
//        if (server != null) {
//            try {
//
//                // ???
//                String[] hostPort = server.getProperties().getInternalAddress().split(":");
//                try (@SuppressWarnings("unused") Socket socket = new Socket(hostPort[0],
//                                                                            Integer.parseInt(hostPort[1]))) {
//                    return true;
//                }
//            } catch (Exception ignored) {
//            }
//        }
        return false;
    }
}
