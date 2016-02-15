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
package org.eclipse.che.plugin.docker.machine.local;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Gets predefined docker containers host for machine servers instead of evaluating it from docker configuration
 *
 * <p>Value of host can be retrieved from property ${code machine.docker.local_node_host} or
 * from environment variable {@code DOCKER_MACHINE_HOST}.<br>
 * Environment variable has lower priority.
 *
 * @author Alexander Garagatyi
 * @see org.eclipse.che.plugin.docker.machine.ServerConf
 */
public class LocalDockerInstanceMetadata extends DockerInstanceRuntimeInfo {
    /**
     * Env variable that shows host (or IP) where docker machines are deployed
     */
    public static final String DOCKER_MACHINE_HOST = "DOCKER_MACHINE_HOST";

    @Inject
    public LocalDockerInstanceMetadata(@Assisted ContainerInfo containerInfo,
                                       @Assisted String containerHost,
                                       @Nullable @Named("machine.docker.local_node_host") String dockerNodeHost) {
        super(containerInfo, dockerNodeHost != null ? dockerNodeHost :
                             (System.getenv(DOCKER_MACHINE_HOST) != null ? System.getenv(DOCKER_MACHINE_HOST) : containerHost));
    }
}
