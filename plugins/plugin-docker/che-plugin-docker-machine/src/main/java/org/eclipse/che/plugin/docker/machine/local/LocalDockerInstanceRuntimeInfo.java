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

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * Gets predefined docker containers host (internal and external addresses) for machine servers instead of evaluating it
 * from docker configuration. External address is needed when clients (UD, IDE, etc...) can't ping machine servers
 * directly (e.g. when running on Docker for Mac or behind a NAT). If the external address is not provided it defaults
 * to the internal one.
 *
 * <p>Value of host can be retrieved from property ${code machine.docker.local_node_host} or
 * from environment variable {@code CHE_DOCKER_MACHINE_HOST}.<br>
 * <p>Value of external hostname can be retrieved from property ${code machine.docker.local_node_host.external} or
 * from environment variable {@code CHE_DOCKER_MACHINE_HOST_EXTERNAL}.<br>
 * Environment variables have higher priority.
 *
 * @author Alexander Garagatyi
 * @see org.eclipse.che.api.core.model.machine.ServerConf
 */
public class LocalDockerInstanceRuntimeInfo extends DockerInstanceRuntimeInfo {
    /**
     * Env variable that provides host (or IP) of the Docker host.
     * This value is used by wsmaster to communicate with servers running inside containers.
     */
    public static final String CHE_DOCKER_MACHINE_HOST_INTERNAL = "CHE_DOCKER_MACHINE_HOST";

    /**
     * Env variable that provides the external host (or IP) of the Docker host.
     * The value is used by UD, IDE and other clients to communicate with the servers running inside containers.
     */
    public static final String CHE_DOCKER_MACHINE_HOST_EXTERNAL = "CHE_DOCKER_MACHINE_HOST_EXTERNAL";

    @Inject
    public LocalDockerInstanceRuntimeInfo(@Assisted ContainerInfo containerInfo,
                                          @Assisted("externalhost") @Nullable String containerExternalHostname,
                                          @Assisted("internalhost") String containerInternalHostname,
                                          @Assisted MachineConfig machineConfig,
                                          @Nullable @Named("machine.docker.local_node_host") String dockerNodeInternalHostname,
                                          @Nullable @Named("machine.docker.local_node_host.external") String dockerNodeExternalHostname,
                                          @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                          @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers) {


        super(containerInfo,
              externalHostnameWithPrecedence(dockerNodeExternalHostname,
                                           containerExternalHostname,
                                           dockerNodeInternalHostname,
                                           containerInternalHostname),
              internalHostnameWithPrecedence(dockerNodeInternalHostname,
                                           containerInternalHostname),
              machineConfig,
              devMachineServers,
              allMachinesServers);
    }

    private static String externalHostnameWithPrecedence(String externalHostnameProperty,
                                                              String externalHostnameAssisted,
                                                              String internalHostnameProperty,
                                                              String internalHostnameAssisted) {

        String externalHostnameEnvVar = System.getenv(CHE_DOCKER_MACHINE_HOST_EXTERNAL);
        if (externalHostnameEnvVar != null) {
            return externalHostnameEnvVar;
        } else if (externalHostnameProperty != null) {
            return externalHostnameProperty;
        } else if (externalHostnameAssisted != null) {
            return externalHostnameAssisted;
        } else {
            return internalHostnameWithPrecedence(internalHostnameProperty,internalHostnameAssisted);
        }
    }

    private static String internalHostnameWithPrecedence(String internalHostnameProperty,
                                                              String internalHostnameAssisted) {

        String internalHostNameEnvVariable = System.getenv(CHE_DOCKER_MACHINE_HOST_INTERNAL);
        if (internalHostNameEnvVariable != null) {
            return internalHostNameEnvVariable;
        } else if (internalHostnameProperty != null) {
            return internalHostnameProperty;
        } else {
            return internalHostnameAssisted;
        }
    }
}
