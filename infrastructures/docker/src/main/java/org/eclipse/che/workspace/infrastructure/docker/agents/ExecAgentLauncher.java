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
import org.eclipse.che.api.agent.shared.model.impl.AgentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Starts exec agent.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class ExecAgentLauncher extends AbstractAgentLauncher {
    private final String runCommand;

    @Inject
    public ExecAgentLauncher(@Named("che.agent.dev.max_start_time_ms") long agentMaxStartTimeMs,
                             @Named("che.agent.dev.ping_delay_ms") long agentPingDelayMs,
                             @Named("machine.exec_agent.run_command") String runCommand) {
        super(agentMaxStartTimeMs, agentPingDelayMs, new ProcessIsLaunchedChecker("che-exec-agent"));
        this.runCommand = runCommand;
    }

    @Override
    public void launch(DockerService machine, Agent agent) throws InfrastructureException {
        final AgentImpl agentCopy = new AgentImpl(agent);
        agentCopy.setScript(agent.getScript() + "\n" + runCommand);
        super.launch(machine, agentCopy);
    }

    @Override
    public String getMachineType() {
        return "docker";
    }

    @Override
    public String getAgentId() {
        return "org.eclipse.che.exec";
    }
}
