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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.pids;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Sets limit of PIDs into docker containers.
 *
 * @author Alexander Garagatyi
 */
public class PidLimitProvisioner implements ContainerSystemSettingsProvisioner {
    private final int pidsLimit;

    @Inject
    public PidLimitProvisioner(@Named("che.docker.pids_limit") int pidLimit) {
        this.pidsLimit = pidLimit;
    }

    @Override
    public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
        for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
            containerConfig.setPidsLimit(pidsLimit);
        }
    }
}
