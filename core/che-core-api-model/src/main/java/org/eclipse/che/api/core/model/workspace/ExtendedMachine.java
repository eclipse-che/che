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
package org.eclipse.che.api.core.model.workspace;

import java.util.List;
import java.util.Map;

/**
 * Additional information about machine which is needed for purposes of CHE.
 *
 * @author Alexander Garagatyi
 */
public interface ExtendedMachine {
    /**
     * Returns list of agents that should be deployed into machine.
     */
    List<String> getAgents();

    /**
     * Returns mapping of references to configurations of servers deployed into machine.
     */
    Map<String, ? extends ServerConf2> getServers();

    /**
     * Returns attributes of resources of machine.
     */
    Map<String, String> getAttributes();
}
