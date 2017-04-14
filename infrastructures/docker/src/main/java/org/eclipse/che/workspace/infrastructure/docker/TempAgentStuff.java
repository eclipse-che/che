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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.agent.shared.model.impl.AgentKeyImpl;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alexander Garagatyi
 */
public class TempAgentStuff {
    // TODO move to agents resolving components
    static final Map<String, String> runCommands = new HashMap<>();
    static {
        runCommands.put("org.eclipse.che.ws-agent", "export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh jpda run");
        runCommands.put("org.eclipse.che.terminal", "$HOME/che/terminal/che-websocket-terminal " +
                                                    "-addr :4411 " +
                                                    "-cmd ${SHELL_INTERPRETER}");
        runCommands.put("org.eclipse.che.exec", "$HOME/che/exec-agent/che-exec-agent " +
                                                "-addr :4412 " +
                                                "-cmd ${SHELL_INTERPRETER} " +
                                                "-logs-dir $HOME/che/exec-agent/logs");
    }

    private final AgentSorter   agentSorter;
    private final AgentRegistry agentRegistry;

    @Inject
    public TempAgentStuff(AgentSorter agentSorter, AgentRegistry agentRegistry) {
        this.agentSorter = agentSorter;
        this.agentRegistry = agentRegistry;
    }

    public List<String> sortAgents(MachineConfig machineConfig) throws ValidationException {
        try {
            return agentSorter.sort(machineConfig.getAgents())
                              .stream()
                              .map(AgentKey::getId)
                              .collect(Collectors.toList());
        } catch (AgentException e) {
            throw new ValidationException(e.getLocalizedMessage(), e);
        }
    }

    public String getAgentScript(String agent) throws InfrastructureException {
        try {
            String agentScript = agentRegistry.getAgent(new AgentKeyImpl(agent)).getScript();
            if (runCommands.containsKey(agent)) {
                agentScript = agentScript + '\n' + runCommands.get(agent);
            }
            return agentScript;
        } catch (AgentException e) {
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }
    }
}
