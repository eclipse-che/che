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

import javax.inject.Named;

/**
 * Launches agent and waits while it is finished.
 *
 * This agents is suited only for those types of agents that install software
 * and finish working without launching any processes at the end.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class DefaultAgentLauncher extends AbstractAgentLauncher {
    @Inject
    public DefaultAgentLauncher(@Named("machine.agent.max_start_time_ms") long agentMaxStartTimeMs,
                                @Named("machine.agent.ping_delay_ms") long agentPingDelayMs) {
        super(agentMaxStartTimeMs, agentPingDelayMs, AgentLaunchingChecker.DEFAULT);
    }

    @Override
    public String getAgentName() {
        return "any";
    }

    @Override
    public String getMachineType() {
        return "any";
    }
}
