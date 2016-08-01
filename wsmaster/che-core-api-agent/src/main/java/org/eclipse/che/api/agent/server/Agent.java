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

import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
public interface Agent {
    /**
     * @return the fqn of the agent
     */
    String getFqn();

    /**
     * @return the version of the agent
     */
    String getVersion();

    /**
     * @return environment variables to be set
     */
    Map<String, String> getEnvVariables();

    /**
     * @return volumes to be mounted
     */
    List<String> getVolumes();

    /**
     * @return ports to be exposed
     */
    List<String> getPorts();

    /**
     * @return depending agents to be started before
     */
    List<String> getDependencies();

    /**
     * @return script to execute inside machine
     */
    String getScript();
}
