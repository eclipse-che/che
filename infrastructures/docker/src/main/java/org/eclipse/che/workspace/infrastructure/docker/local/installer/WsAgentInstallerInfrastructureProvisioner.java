/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.shared.Utils;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.workspace.infrastructure.docker.local.server.DockerExtConfBindingProvider;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides volumes configuration of machine for wsagent installer
 *
 * <p>On Windows MUST be locate in "user.home" directory in case limitation windows+docker.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WsAgentInstallerInfrastructureProvisioner implements ConfigurationProvisioner {
    private final WsAgentBinariesInfrastructureProvisioner binariesProvisioner;
    private final String                                   extConfBinding;

    @Inject
    public WsAgentInstallerInfrastructureProvisioner(WsAgentBinariesInfrastructureProvisioner binariesProvisioner,
                                                     DockerExtConfBindingProvider dockerExtConfBindingProvider) {

        this.extConfBinding = dockerExtConfBindingProvider.get();
        this.binariesProvisioner = binariesProvisioner;
    }

    @Override
    public void provision(EnvironmentImpl envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
            throws InfrastructureException {

        binariesProvisioner.provision(envConfig, internalEnv, identity);

        if (extConfBinding != null) {
            String devMachineName = Utils.getDevMachineName(envConfig);
            if (devMachineName == null) {
                throw new InternalInfrastructureException("Dev machine is not found in environment");
            }
            DockerContainerConfig containerConfig = internalEnv.getContainers().get(devMachineName);
            containerConfig.getVolumes().add(extConfBinding);
            containerConfig.getEnvironment().put(CheBootstrap.CHE_LOCAL_CONF_DIR,
                                                 DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
        }
    }

}
