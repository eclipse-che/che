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
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Verifies that agent was started successfully by calling verification of several specified verifiers.
 * </p>
 * Verification passes if and only if all verifiers pass verification.
 *
 * @author Alexander Garagatyi
 */
public class CompositeAgentLaunchingChecker implements AgentLaunchingChecker {

    private final List<AgentLaunchingChecker> agentLaunchingCheckers;

    public CompositeAgentLaunchingChecker(AgentLaunchingChecker... agentLaunchingCheckers) {
        checkArgument(agentLaunchingCheckers != null && agentLaunchingCheckers.length > 0,
                      "Array of agent launching checkers should neither be null nor empty");
        for (AgentLaunchingChecker agentLaunchingChecker : agentLaunchingCheckers) {
            checkArgument(agentLaunchingChecker != null,
                          "Array of agent launching checker should not contain null");
        }
        this.agentLaunchingCheckers = Arrays.asList(agentLaunchingCheckers);
    }

    @Override
    public boolean isLaunched(Agent agent, InstanceProcess process, Instance machine) throws MachineException {
        for (AgentLaunchingChecker agentLaunchingChecker : agentLaunchingCheckers) {
            if (!agentLaunchingChecker.isLaunched(agent, process, machine)) {
                return false;
            }
        }
        return true;
    }
}
