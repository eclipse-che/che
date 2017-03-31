/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.model.workspace.config;

import java.util.List;
import java.util.Map;

/**
 * Machine configuration
 *
 * @author Alexander Garagatyi
 */
public interface MachineConfig {
    /**
     * Returns list of configured agents.
     */
    List<String> getAgents();

    /**
     * Returns mapping of references to configurations of servers deployed into machine.
     */
    Map<String, ? extends ServerConfig> getServers();

    /**
     * Returns attributes of resources of machine.
     */
    Map<String, String> getAttributes();
}
