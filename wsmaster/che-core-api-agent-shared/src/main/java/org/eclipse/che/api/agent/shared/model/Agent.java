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
package org.eclipse.che.api.agent.shared.model;

import org.eclipse.che.api.core.model.workspace.ServerConf2;

import java.util.List;
import java.util.Map;

/**
 * An entity that might additionally injected into machine and brings functionality.
 *
 * @author Anatoliy Bazko
 */
public interface Agent {

    /**
     * Returns the id of the agent.
     */
    String getId();

    /**
     * Returns the name of the agent.
     */
    String getName();

    /**
     * Returns the version of the agent.
     */
    String getVersion();

    /**
     * Returns the description of the agent.
     */
    String getDescription();

    /**
     * Returns the depending agents, that must be applied before.
     */
    List<String> getDependencies();

    /**
     * Returns the script to be applied when machine is started.
     */
    String getScript();

    /**
     * Returns any machine specific properties.
     */
    Map<String, String> getProperties();

    /**
     * Returns Che servers in the machine.
     */
    Map<String, ? extends ServerConf2> getServers();
}
