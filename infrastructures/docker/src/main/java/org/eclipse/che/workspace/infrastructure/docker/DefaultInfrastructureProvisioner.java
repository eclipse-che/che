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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import javax.inject.Inject;

/**
 * Implementation of CHE infrastructure provisioner that adds agent-specific infrastructure to internal environment representation.
 *
 * @author Alexander Garagatyi
 */
// TODO think about passing here full Agents config
public class DefaultInfrastructureProvisioner implements InfrastructureProvisioner {
    private final AgentConfigApplier agentConfigApplier;

    @Inject
    public DefaultInfrastructureProvisioner(AgentConfigApplier agentConfigApplier) {
        this.agentConfigApplier = agentConfigApplier;
    }

    @Override
    public void provision(Environment envConfig,
                          DockerEnvironment dockerEnvironment,
                          RuntimeIdentity runtimeIdentity) throws InfrastructureException {
        try {
            agentConfigApplier.apply(envConfig, dockerEnvironment);
        } catch (AgentException e) {
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }
    }
}
