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

import org.eclipse.che.api.agent.server.Agent;
import org.eclipse.che.api.agent.server.AgentException;
import org.eclipse.che.api.agent.server.AgentFactory;
import org.eclipse.che.api.agent.server.AgentProvider;
import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.shared.model.AgentConfig;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class AgentProviderImpl implements AgentProvider {

    private final AgentRegistry             agentRegistry;
    private final Map<String, AgentFactory> factories;

    @Inject
    public AgentProviderImpl(Set<AgentFactory> factories, AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
        this.factories = factories.stream().collect(Collectors.toMap(AgentFactory::getFqn, f -> f));
    }

    @Override
    public Agent createAgent(String fqn, String version) throws AgentException {
        AgentConfig agentConfig = agentRegistry.getConfig(fqn, version);
        return doCreateAgent(agentConfig);
    }

    @Override
    public Agent createAgent(String fqn) throws AgentException {
        AgentConfig agentConfig = agentRegistry.getConfig(fqn);
        return doCreateAgent(agentConfig);
    }

    private Agent doCreateAgent(AgentConfig agentConfig) throws AgentException {
        String fqn = agentConfig.getFqn();
        AgentFactory factory = factories.get(fqn);
        if (factory == null) {
            throw new AgentException("Agent factory " + fqn + " not found");
        }

        return factory.create(agentConfig);
    }
}
