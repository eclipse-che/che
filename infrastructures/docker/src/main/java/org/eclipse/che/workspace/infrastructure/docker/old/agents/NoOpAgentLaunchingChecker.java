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
package org.eclipse.che.workspace.infrastructure.docker.old.agents;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

/**
 * Agent launching checker that does nothing. Useful when agent does not run in a machine as daemon.
 *
 * @author Alexander Garagatyi
 */
public class NoOpAgentLaunchingChecker implements AgentLaunchingChecker {
    @Override
    public boolean isLaunched(Agent agent, /*DockerProcess process, */DockerService machine) throws
                                                                                         InfrastructureException {
        return true;
    }
}
