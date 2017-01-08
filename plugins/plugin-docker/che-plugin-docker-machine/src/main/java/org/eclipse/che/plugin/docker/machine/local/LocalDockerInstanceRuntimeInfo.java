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
package org.eclipse.che.plugin.docker.machine.local;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * By default, gets predefined docker containers host (internal and external addresses) for machine servers instead of evaluating it
 * from docker configuration. External address is needed when clients (UD, IDE, etc...) can't ping machine servers
 * directly (e.g. when running on Docker for Mac or behind a NAT). If the external address is not provided it defaults
 * to the internal one.
 *
 * <p>If property {@code che.docker.ip.use_internal_address} is true, is true, the IP Address from
 * {@link ContainerInfo}, if available, is used for internal address instead.<br>
 * <p>Value of host can be retrieved from property {@code che.docker.ip}. <br>
 * <p>Value of external hostname can be retrieved from property {@code che.docker.ip.external}. <br>
 * Environment variables override properties.
 *
 * @author Alexander Garagatyi
 * @see org.eclipse.che.api.core.model.machine.ServerConf
 */
public class LocalDockerInstanceRuntimeInfo extends DockerInstanceRuntimeInfo {

    @Inject
    public LocalDockerInstanceRuntimeInfo(@Assisted ContainerInfo containerInfo,
                                          @Assisted("externalhost") @Nullable String containerExternalHostname,
                                          @Assisted("internalhost") String containerInternalHostname,
                                          @Assisted MachineConfig machineConfig,
                                          @Nullable @Named("che.docker.ip") String dockerNodeInternalHostname,
                                          @Nullable @Named("che.docker.ip.external") String dockerNodeExternalHostname,
                                          @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
                                          @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
                                          @Named("che.docker.ip.use_internal_address") boolean useInternalAddress) {

        super(containerInfo,
              externalHostnameWithPrecedence(dockerNodeExternalHostname,
                                             containerExternalHostname,
                                             dockerNodeInternalHostname,
                                             containerInternalHostname),
              internalHostnameWithPrecedence(dockerNodeInternalHostname,
                                             containerInternalHostname,
                                             containerInfo.getNetworkSettings(),
                                             useInternalAddress),
              machineConfig,
              devMachineServers,
              allMachinesServers,
              useInternalAddress);
    }

    private static String externalHostnameWithPrecedence(String externalHostnameProperty,
                                                         String externalHostnameAssisted,
                                                         String internalHostnameProperty,
                                                         String internalHostnameAssisted) {

        if (externalHostnameProperty != null) {
            return externalHostnameProperty;
        } else if (externalHostnameAssisted != null) {
            return externalHostnameAssisted;
        } else {
            return internalHostnameWithPrecedence(internalHostnameProperty,
                                                  internalHostnameAssisted,
                                                  null,
                                                  false);
        }
    }

    private static String internalHostnameWithPrecedence(String internalHostnameProperty,
                                                         String internalHostnameAssisted,
                                                         NetworkSettings networkSettings,
                                                         boolean useInternalAddress) {

        if (useInternalAddress) {
            String containerHostName = null;
            if (networkSettings != null) {
                containerHostName = networkSettings.getIpAddress();
            }
            if (containerHostName != null && !containerHostName.isEmpty()) {
                return containerHostName;
            }
        }

        if (internalHostnameProperty != null) {
            return internalHostnameProperty;
        } else {
            return internalHostnameAssisted;
        }
    }
}
