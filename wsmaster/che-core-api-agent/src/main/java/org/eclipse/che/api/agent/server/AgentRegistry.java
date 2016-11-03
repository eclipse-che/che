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
package org.eclipse.che.api.agent.server;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.exception.AgentNotFoundException;
import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.agent.shared.model.AgentKey;

import java.util.Collection;
import java.util.List;

/**
 * The registry for agents that might be injected into machine.
 *
 * @see  Agent
 * @see  AgentKey
 *
 * @author Anatoliy Bazko
 */
public interface AgentRegistry {

    /**
     * Gets {@link Agent}.
     *
     * @param agentKey
     *      the agent key
     * @return {@link Agent}
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if unexpected error occurred
     */
    Agent getAgent(AgentKey agentKey) throws AgentException;

    /**
     * Returns a list of the available versions of the specific agent.
     *
     * @param id
     *      the id of the agent
     * @return list of versions
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if unexpected error occurred
     */
    List<String> getVersions(String id) throws AgentException;


    /**
     * Returns the collection of available agents.
     *
     * @return collection of agents
     * @throws AgentException
     *      if unexpected error occurred
     */
    Collection<Agent> getAgents() throws AgentException;
}
