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

import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
public interface Agent {

    /**
     * @return the name of the agent
     */
    String getName();

    /**
     * @return the version of the agent
     */
    String getVersion();

    /**
     * @return the depending agents, that must be applied before
     */
    List<String> getDependencies();

    /**
     * @return the script to be applied when machine is started
     */
    String getScript();

    /**
     * @return any machine specific properties
     */
    Map<String, String> getProperties();
}
