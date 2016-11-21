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
import org.eclipse.che.api.agent.shared.model.AgentKey;

import java.net.URL;

/**
 * @author Anatolii Bazko
 */
public interface AgentRegistryUrlProvider {
    /**
     * Returns url to download agent of the specific version.
     *
     * @param agentKey
     *      the agent name and version
     * @return {@link URL}
     * @throws AgentException
     *      if unexpected error occurred
     */
    URL getAgentUrl(AgentKey agentKey) throws AgentException;

    /**
     * Returns url to fetch available versions of the agent.

     * @param name
     *      the agent name
     * @return {@link URL}
     * @throws AgentException
     *      if unexpected error occurred
     */
    URL getAgentVersionsUrl(String name) throws AgentException;
}
