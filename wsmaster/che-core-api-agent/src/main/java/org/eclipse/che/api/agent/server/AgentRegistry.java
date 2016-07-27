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
public interface AgentRegistry {

    /**
     * Fetches {@link AgentConfig}.
     *
     * @param fqn
     *      the fqn of the agent
     * @param version
     *      the version of the agent
     * @return {@link AgentConfig}
     * @throws AgentException
     *      if configuration can't be fetched or other unexpected error occurred
     */
    AgentConfig getConfig(String fqn, String version) throws AgentException;

    /**
     * Fetches the latest version {@link AgentConfig}.
     *
     * @param fqn
     *      the fqn of the agent
     * @return {@link AgentConfig}
     * @throws AgentException
     *      if configuration can't be fetched or other unexpected error occurred
     */
    AgentConfig getConfig(String fqn) throws AgentException;
}
