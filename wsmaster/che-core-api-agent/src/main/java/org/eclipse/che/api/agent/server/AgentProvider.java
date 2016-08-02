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

import org.eclipse.che.api.agent.shared.model.AgentConfig;

/**
 * @author Anatoliy Bazko
 */
public interface AgentProvider {

    /**
     * Creates a new agent. {@link AgentConfig} might be located at some remote storage.
     * How to fetch config is implementation specific.
     *
     * @param agentKey
     *      {@link AgentKey}
     * @return a new {@link Agent} instance
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent createAgent(AgentKey agentKey) throws AgentException;

    /**
     * Creates a new agent. {@link AgentConfig} might be located at some remote storage.
     * How to fetch config is implementation specific.
     *
     * @param fqn
     *      the agent fqn
     * @param version
     *      the agent version
     * @return a new {@link Agent} instance
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent createAgent(String fqn, String version) throws AgentException;

    /**
     * Creates a new agent. {@link AgentConfig} might be located at some remote storage.
     * How to fetch config is implementation specific.
     * The agent version is not defined, that's mean the latest agent version.
     *
     * @param fqn
     *      the agent fqn
     * @return a new {@link Agent} instance
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent createAgent(String fqn) throws AgentException;
}
