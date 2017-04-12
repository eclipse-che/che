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

import org.eclipse.che.api.agent.server.exception.AgentStartException;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

/**
 * Launches {@link Agent#getScript()} on the {@link Instance}.
 *
 * @see Instance
 * @see Agent#getScript()
 *
 * @author Anatolii Bazko
 */
public interface AgentLauncher {

    /**
     * @return the id of the agent that launcher is designed for
     */
    String getAgentId();

    /**
     * @return the machine type that launcher is designed for
     */
    String getMachineType();

    /**
     * Executes agents scripts over target machine.
     * The machine should be started.
     *
     * @see Agent#getScript()
     *
     * @param machine
     *      the machine instance
     * @param agent
     *      the agent
     * @throws org.eclipse.che.api.workspace.server.spi.InfrastructureException
     *      if script execution failed
     */
    void launch(DockerService machine, Agent agent) throws InfrastructureException, AgentStartException;
}
