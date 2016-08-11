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

/**
 * @author Anatoliy Bazko
 */
public interface AgentRegistry {

    /**
     * Creates {@link Agent} of the specific version.
     *
     * @param name
     *      the name of the agent
     * @param version
     *      the version of the agent
     * @return {@link Agent}
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent createAgent(String name, String version) throws AgentException;


    /**
     * Creates {@link Agent}.
     *
     * @param agentKey
     *      the agent key
     * @return {@link Agent}
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent createAgent(AgentKey agentKey) throws AgentException;

    /**
     * Creates the {@link Agent} of the latest version.
     *
     * @param name
     *      the name of the agent
     * @return {@link Agent}
     * @throws AgentNotFoundException
     *      if agent not found in the registry
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent createAgent(String name) throws AgentException;

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
    Collection<String> getVersions(String name) throws AgentException;
}
