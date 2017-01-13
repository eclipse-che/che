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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.model.machine.MachineConfig;

import java.util.Set;

/**
 * Provides {@link AgentLauncher} for specific agent to be run on instance.
 * Returning agent depends on machine type. If no agent found then the default one
 * will be returned.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class AgentLauncherFactory {

    private final Set<AgentLauncher> launchers;
    private final AgentLauncher      defaultLauncher;

    @Inject
    public AgentLauncherFactory(Set<AgentLauncher> launchers, DefaultAgentLauncher defaultLauncher) {
        this.launchers = launchers;
        this.defaultLauncher = defaultLauncher;
    }

    /**
     * Find launcher for given agent independently of version.
     * If the specific {@link AgentLauncher} isn't registered then the default one will be used.
     *
     * @see Agent#getId()
     * @see MachineConfig#getType()
     *
     * @param agentName
     *      the agent name
     * @param machineType
     *      the machine type
     * @return {@link AgentLauncher}
     */
    public AgentLauncher find(String agentId, String machineType) {
        return launchers.stream()
                        .filter(l -> l.getAgentId().equals(agentId))
                        .filter(l -> l.getMachineType().equals(machineType))
                        .findAny()
                        .orElse(defaultLauncher);
    }
}
