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

import java.net.URL;

/**
 * @author Anatolii Bazko
 */
public interface AgentRegistryUrlProvider {
    /**
     * Returns url to download agent of the specific version.
     *
     * @param fqn
     *      the agent fqn
     * @param version
     *      the agent version
     * @return {@link URL}
     * @throws AgentException
     *      if unexpected error occurred
     */
    URL getAgentUrl(String fqn, String version) throws AgentException;

    /**
     * Returns url to download agent of the latest version.
     *
     * @param fqn
     *      the agent fqn
     * @return {@link URL}
     * @throws AgentException
     *      if unexpected error occurred
     */
    URL getAgentUrl(String fqn) throws AgentException;

    /**
     * Returns url to fetch available versions of the agent.

     * @param fqn
     *      the agent fqn
     * @return {@link URL}
     * @throws AgentException
     *      if unexpected error occurred
     */
    URL getAgentVersions(String fqn) throws AgentException;
}
