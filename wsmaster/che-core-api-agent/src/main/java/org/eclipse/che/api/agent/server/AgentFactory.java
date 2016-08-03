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
import org.eclipse.che.api.agent.shared.model.AgentConfig;

/**
 * @author Anatoliy Bazko
 */
public interface AgentFactory {

    /**
     * The fully qualified name of the agent that factory is responsible to create.
     * {@link #getFqn()} must correspond to {@link AgentConfig#getFqn()} in the {@link #create(AgentConfig)} method.
     *
     * @return the fqn
     */
    String getFqn();

    /**
     * Creates corresponding {@link Agent} with given config.
     *
     * @param agentConfig
     * @return a new {@link Agent} instance
     * @throws AgentException
     *      if agent can't be created or other unexpected error occurred
     */
    Agent create(AgentConfig agentConfig) throws AgentException;
}
