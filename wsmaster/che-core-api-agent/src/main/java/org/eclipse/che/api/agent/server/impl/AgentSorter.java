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
package org.eclipse.che.api.agent.server.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl.parse;

/**
 * Sorts and creates agents.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class AgentSorter {

    private final AgentRegistry agentRegistry;

    @Inject
    public AgentSorter(AgentRegistry agentRegistry) {this.agentRegistry = agentRegistry;}

    /**
     * Sort and created agents respecting dependencies between them.
     * Handles circular dependencies.
     *
     * @see AgentKey
     * @see Agent#getDependencies()
     * @see AgentRegistry#createAgent(AgentKey)
     *
     * @param agentKeys list of agents to sort
     * @return list of created agents in proper order
     *
     * @throws AgentException
     *      if circular dependency found or agent creation failed
     * @throws AgentNotFoundException
     *      if agent not found
     */
    public List<Agent> sort(List<String> agentKeys) throws AgentException {
        Map<String, Agent> sorted = new HashMap<>();
        Set<String> pending = new HashSet<>();

        for (String agentKey : agentKeys) {
            doSort(parse(agentKey), sorted, pending);
        }
        doSort(parse("org.eclipse.che.terminal"), sorted, pending);
        doSort(parse("org.eclipse.che.ws-agent"), sorted, pending);
        doSort(parse("org.eclipse.che.ssh"), sorted, pending);

        return new ArrayList<>(sorted.values());
    }

    private void doSort(AgentKey agentKey, Map<String, Agent> sorted, Set<String> pending) throws AgentException {
        String agentName = agentKey.getName();

        if (sorted.containsKey(agentName)) {
            return;
        }
        if (!pending.add(agentName)) {
            throw new AgentException("Agents circular dependency found.");
        }

        Agent agent = agentRegistry.createAgent(agentKey);

        for (String dependency : agent.getDependencies()) {
            doSort(parse(dependency), sorted, pending);
        }

        sorted.put(agentName, agent);
        pending.remove(agentName);
    }
}
