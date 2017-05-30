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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;

import javax.inject.Inject;

/**
 * Implementation of CHE infrastructure provisioner that adds agent-specific infrastructure to internal environment representation.
 *
 * @author Alexander Garagatyi
 */
public class DefaultInfrastructureProvisioner implements InfrastructureProvisioner {
    private final AgentConfigApplier agentConfigApplier;

    @Inject
    public DefaultInfrastructureProvisioner(AgentConfigApplier agentConfigApplier) {
        this.agentConfigApplier = agentConfigApplier;
    }

    @Override
    public void provision(EnvironmentImpl envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        try {
            agentConfigApplier.apply(envConfig, internalEnv);
        } catch (AgentException e) {
            throw new EnvironmentException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void provision(ExtendedMachineImpl machineConfig, CheServiceImpl internalMachine) throws EnvironmentException {
        try {
            agentConfigApplier.apply(machineConfig, internalMachine);
        } catch (AgentException e) {
            throw new EnvironmentException(e.getLocalizedMessage(), e);
        }
    }
}
