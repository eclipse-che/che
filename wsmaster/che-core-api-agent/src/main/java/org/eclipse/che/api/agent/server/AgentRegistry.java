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

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public interface AgentRegistry {

    /**
     * Gets {@link Agent} of the specific version.
     *
     * @param name
     *      the name of the agent
     * @param version
     *      the version of the agent
     * @return {@link Agent}
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if unexpected error occurred
     */
    Agent getAgent(String name, String version) throws AgentException;

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
     * Gets the {@link Agent} by its name or by url.
     * In case of name the latest version of the agent will be created.
     * In case of url the agent will be retrieved by specific url.
     *
     * @param name
     *      the name of the agent or url
     * @return {@link Agent}
     * @throws AgentNotFoundException
     *      if agent not found
     * @throws AgentException
     *      if unexpected error occurred
     */
    Agent getAgent(String name) throws AgentException;

    /**
     * Returns a list of the available versions of the specific agent.
     *
     * @param name
     *      the name of the agent
     * @return list of versions
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if unexpected error occurred
     */
    List<String> getVersions(String name) throws AgentException;


    /**
     * Returns the list of available agents.
     *
     * @return list of agents
     * @throws AgentException
     */
    List<String> getAgents() throws AgentException;
}
