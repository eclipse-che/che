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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class EnvConfigAgentApplier {

    private final AgentConfigApplier agentConfigApplier;

    public EnvConfigAgentApplier(AgentConfigApplier agentConfigApplier) {
        this.agentConfigApplier = agentConfigApplier;
    }

    public void apply(Environment envConfig,
                      CheServicesEnvironmentImpl internalEnv) throws AgentException {
        for (Map.Entry<String, ? extends ExtendedMachine> machineEntry : envConfig.getMachines()
                                                                                  .entrySet()) {
            String machineName = machineEntry.getKey();
            ExtendedMachine machineConf = machineEntry.getValue();
            CheServiceImpl internalMachine = internalEnv.getServices().get(machineName);

            apply(machineConf, internalMachine);
        }
    }

    public void apply(@Nullable ExtendedMachine machineConf,
                      CheServiceImpl internalMachine) throws AgentException {
        if (machineConf != null) {
            agentConfigApplier.modify(internalMachine, machineConf.getAgents());
        }
    }
}
