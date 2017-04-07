/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *   Red Hat Inc.  - refactor getServers() method
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.old;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.OldMachineConfig;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.OldServerConf;
import org.eclipse.che.api.machine.server.model.impl.OldServerConfImpl;
//import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerState;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.ServerEvaluationStrategy;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.ServerEvaluationStrategyProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Docker implementation of {@link MachineRuntimeInfo}
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Roman Iuvshyn
 */
public class DockerInstanceRuntimeInfo implements MachineRuntimeInfo {
    /**
     * Env variable that points to root folder of projects in dev machine
     */
    public static final String PROJECTS_ROOT_VARIABLE = "CHE_PROJECTS_ROOT";

    /**
     * Env variable for jvm settings
     */
    public static final String JAVA_OPTS_VARIABLE = "JAVA_OPTS";

    /**
     * Env variable for dev machine that contains url of Che API
     */
    public static final String API_ENDPOINT_URL_VARIABLE = "CHE_API";

    /**
     * Environment variable that will be setup in developer machine will contain ID of a workspace for which this machine has been created
     */
    public static final String CHE_WORKSPACE_ID = "CHE_WORKSPACE_ID";

    /**
     * Default HOSTNAME that will be added in all docker containers that are started. This host will container the Docker host's ip
     * reachable inside the container.
     */
    public static final String CHE_HOST = "che-host";

    /**
     * Environment variable that will be setup in developer machine and contains user token.
     */
    public static final String USER_TOKEN = "USER_TOKEN";

    private final ContainerInfo                    info;
    private final Map<String, OldServerConfImpl>   serversConf;
    private final String                           internalHost;
    private final ServerEvaluationStrategyProvider provider;

    @Inject
    public DockerInstanceRuntimeInfo(@Assisted ContainerInfo containerInfo,
                                     @Assisted OldMachineConfig machineConfig,
                                     @Assisted String internalHost,
                                     ServerEvaluationStrategyProvider provider,
                                     @Named("machine.docker.dev_machine.machine_servers") Set<OldServerConf> devMachineSystemServers,
                                     @Named("machine.docker.machine_servers") Set<OldServerConf> allMachinesSystemServers) {
        this.info = containerInfo;

        Stream<OldServerConf> confStream = Stream.concat(machineConfig.getServers().stream(), allMachinesSystemServers.stream());
        if (machineConfig.isDev()) {
            confStream = Stream.concat(confStream, devMachineSystemServers.stream());
        }
        // convert list to map for quick search and normalize port - add /tcp if missing
        this.serversConf = confStream.collect(toMap(srvConf -> srvConf.getPort().contains("/") ?
                                                               srvConf.getPort() :
                                                               srvConf.getPort() + "/tcp",
                                                    OldServerConfImpl::new));

        this.internalHost = internalHost;
        this.provider = provider;
    }

    @Override
    public Map<String, ServerImpl> getServers() {
        ServerEvaluationStrategy strategy = provider.get();
        return strategy.getServers(info, internalHost, serversConf);
    }
}
