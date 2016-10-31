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
import org.eclipse.che.api.agent.server.model.impl.AgentKeyImpl;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Sort agents respecting dependencies between them.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class AgentSorter {

    private final AgentRegistry agentRegistry;

    @Inject
    public AgentSorter(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    /**
     * Sort agents respecting dependencies between them.
     * Handles circular dependencies.
     *
     * @see AgentKey
     * @see Agent#getDependencies()
     * @see AgentRegistry#getAgent(AgentKey)
     *
     * @param agentKeys list of agents to sort
     * @return list of created agents in proper order
     *
     * @throws AgentException
     *      if circular dependency found or agent creation failed or other unexpected error
     */
    public List<AgentKey> sort(@Nullable List<String> agentKeys) throws AgentException {
        List<AgentKey> sorted = new ArrayList<>();
        Set<String> pending = new HashSet<>();

        if (agentKeys != null) {
            for (String agentKey : agentKeys) {
                if (agentKey != null) {
                    doSort(AgentKeyImpl.parse(agentKey), sorted, pending);
                }
            }
        }

        return sorted;
    }

    private void doSort(AgentKey agentKey, List<AgentKey> sorted, Set<String> pending) throws AgentException {
        String agentId = agentKey.getId();

        Optional<AgentKey> alreadySorted = sorted.stream().filter(k -> k.getId().equals(agentId)).findFirst();
        if (alreadySorted.isPresent()) {
            return;
        }
        pending.add(agentId);

        Agent agent = agentRegistry.getAgent(agentKey);

        for (String dependency : agent.getDependencies()) {
            if (pending.contains(dependency)) {
                throw new AgentException(
                        String.format("Agents circular dependency found between '%s' and '%s'", dependency, agentId));
            }
            doSort(AgentKeyImpl.parse(dependency), sorted, pending);
        }

        sorted.add(agentKey);
        pending.remove(agentId);
    }
}
