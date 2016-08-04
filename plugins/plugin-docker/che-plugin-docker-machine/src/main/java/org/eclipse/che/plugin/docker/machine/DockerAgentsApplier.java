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
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.Agent;
import org.eclipse.che.api.agent.server.AgentProvider;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.AgentConfig;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.CompositeLineConsumer;
import org.eclipse.che.api.core.util.ErrorConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class DockerAgentsApplier {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DockerAgentsApplier.class);

    private final AgentProvider agentProvider;

    @Inject
    public DockerAgentsApplier(AgentProvider agentProvider) {
        this.agentProvider = agentProvider;
    }

    /**
     * Applies agents {@link MachineConfig#getAgents()} over machine instance.
     * It basically means run agent script {@link AgentConfig#getScript()}.
     * Respects dependencies between agents.
     */
    public void apply(Instance machine) throws MachineException {
        HashSet<String> agentsCompleted = new HashSet<>();
        HashSet<String> agentsInProgress = new HashSet<>();

        for (String agentKey : machine.getConfig().getAgents()) {
            apply(machine, AgentKeyImpl.of(agentKey), agentsCompleted, agentsInProgress);
        }

        apply(machine, AgentKeyImpl.of("org.eclipse.che.ls.json"), agentsCompleted, agentsInProgress);
    }

    /**
     * @param machine
     *      the machine instance
     * @param agentKey
     *      the agent to be applied
     * @param agentsCompleted
     *      completed agents, to avoid applying the same agent twice
     * @param agentsInProgress
     *      pending agents to apply, to avoid circular dependency
     */
    private void apply(Instance machine,
                       AgentKeyImpl agentKey,
                       Set<String> agentsCompleted,
                       Set<String> agentsInProgress) throws MachineException {
        if (agentsCompleted.contains(agentKey.getFqn())) {
            return;
        }

        if (!agentsInProgress.add(agentKey.getFqn())) {
            throw new MachineException("Agents circular dependency found.");
        }

        Agent agent;
        try {
            agent = agentProvider.createAgent(agentKey);
        } catch (AgentException e) {
            throw new MachineException("Agent can't be created " + agentKey, e);
        }

        for (String dependency : agent.getDependencies()) {
            apply(machine, AgentKeyImpl.of(dependency), agentsCompleted, agentsInProgress);
        }

        LOG.info("Starting {} agent", agentKey.toString());
        startProcess(machine, new CommandImpl(agentKey.toString(), agent.getScript(), "agent"));

        agentsInProgress.remove(agentKey.getFqn());
        agentsCompleted.add(agentKey.getFqn());
    }

    private void startProcess(Instance machine, Command command) throws MachineException {
        ErrorConsumer statusDetector = new ErrorConsumer();

        InstanceProcess process = machine.createProcess(command, null);
        try {
            process.start(new CompositeLineConsumer(statusDetector, new AbstractLineConsumer() {
                @Override
                public void writeLine(String line) throws IOException {
                    LOG.debug(line);
                }
            }));
        } catch (ConflictException e) {
            throw new MachineException("Can't start process with command: " + command, e);
        }

        if (statusDetector.hasError()) {
            throw new MachineException(format("Agent %s failed: %s", command.getName(), statusDetector.getError()));
        }
    }
}
