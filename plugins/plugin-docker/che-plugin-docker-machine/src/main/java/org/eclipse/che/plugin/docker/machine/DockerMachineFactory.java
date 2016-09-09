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
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;

/**
 * Provides Docker machine implementation instances.
 *
 * @author Alexander Garagatyi
 */
public interface DockerMachineFactory {
    /**
     * Creates docker implementation of {@link InstanceProcess} for process that is running or can be started in container
     *
     * @param container
     *         container identifier
     * @param command
     *         command that should be executed in the machine
     * @param outputChannel
     *         websocket chanel for execution logs
     * @param pidFilePath
     *         full path to pid file of the process
     * @param pid
     *         id of the {@code InstanceProcess}. It's external PID that may differ from PID inside container
     * @throws MachineException
     *         if error occurs on creation of {@code InstanceProcess}
     */
    InstanceProcess createProcess(@Assisted Command command,
                                  @Assisted("container") String container,
                                  @Assisted("outputChannel") String outputChannel,
                                  @Assisted("pid_file_path") String pidFilePath,
                                  @Assisted int pid) throws MachineException;

    /**
     * Creates docker implementation of {@link Instance}
     *
     * @param machine description of machine
     * @param container container that represents {@code Instance}
     * @param image image from what container was created
     * @param node description of server where container is running
     * @param outputConsumer consumer of output from container main process
     * @throws MachineException if error occurs on creation of {@code Instance}
     */
    Instance createInstance(@Assisted Machine machine,
                            @Assisted("container") String container,
                            @Assisted("image") String image,
                            @Assisted DockerNode node,
                            @Assisted LineConsumer outputConsumer) throws MachineException;

    /**
     * Creates {@link DockerNode} that describes server where container is running
     *
     * @param workspaceId id of workspace that owns docker container
     * @param containerId identifier of container
     * @throws MachineException if error occurs on creation of {@code DockerNode}
     */
    DockerNode createNode(@Assisted("workspace") String workspaceId,
                          @Assisted("container") String containerId) throws MachineException;

    /**
     * Creates {@link DockerInstanceRuntimeInfo} instance using assisted injection
     *
     * @param containerInfo description of docker container
     * @param containerExternalHostname docker host external hostname (used by the browser)
     * @param containerInternalHostname docker host internal hostname (used by the wsmaster)
     * @param machineConfig config of machine
     */
    DockerInstanceRuntimeInfo createMetadata(@Assisted ContainerInfo containerInfo,
                                             @Assisted("externalhost") @Nullable String containerExternalHostname,
                                             @Assisted("internalhost") String containerInternalHostname,
                                             @Assisted MachineConfig machineConfig);
}
