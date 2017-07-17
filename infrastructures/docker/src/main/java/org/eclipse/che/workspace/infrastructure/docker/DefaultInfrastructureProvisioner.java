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

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import javax.inject.Inject;
import java.util.Map;

/**
 * Implementation of CHE infrastructure provisioner that adds agent-specific infrastructure to internal environment representation.
 *
 * @author Alexander Garagatyi
 */
// TODO think about passing here full Agents config
public class DefaultInfrastructureProvisioner implements InfrastructureProvisioner {
    private final InstallerConfigApplier installerConfigApplier;

    @Inject
    public DefaultInfrastructureProvisioner(InstallerConfigApplier installerConfigApplier) {
        this.installerConfigApplier = installerConfigApplier;
    }

    @Override
    public void provision(Environment envConfig,
                          DockerEnvironment dockerEnvironment,
                          RuntimeIdentity runtimeIdentity) throws InfrastructureException {
        try {
            installerConfigApplier.apply(envConfig, dockerEnvironment);
        } catch (InstallerException e) {
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }

        for (Map.Entry<String, ? extends MachineConfig> entry : envConfig.getMachines().entrySet()) {
            String name = entry.getKey();
            DockerContainerConfig container = dockerEnvironment.getContainers().get(name);
            container.getLabels().putAll(Labels.newSerializer()
                                               .machineName(name)
                                               .runtimeId(runtimeIdentity)
                                               .servers(entry.getValue().getServers())
                                               .labels());
        }
    }
}
