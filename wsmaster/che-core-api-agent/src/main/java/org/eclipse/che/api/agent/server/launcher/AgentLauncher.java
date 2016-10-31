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
package org.eclipse.che.api.agent.server.launcher;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.spi.Instance;

/**
 * Launches {@link Agent#getScript()} on the {@link Instance}.
 *
 * @see Instance
 * @see Agent#getScript()
 *
 * @author Anatolii Bazko
 */
public interface AgentLauncher {

    /**
     * @return the id of the agent that launcher is designed for
     */
    String getAgentId();

    /**
     * @return the machine type that launcher is designed for
     */
    String getMachineType();

    /**
     * Executes agents scripts over target machine.
     * The machine should be started.
     *
     * @see Agent#getScript()
     *
     * @param machine
     *      the machine instance
     * @param agent
     *      the agent
     * @throws ServerException
     *      if script execution failed
     */
    void launch(Instance machine, Agent agent) throws ServerException;
}
