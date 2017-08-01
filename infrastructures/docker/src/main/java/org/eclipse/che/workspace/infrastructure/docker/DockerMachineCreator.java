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

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.workspace.infrastructure.docker.monit.DockerMachineStopDetector;
import org.eclipse.che.workspace.infrastructure.docker.strategy.ServersMapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

/** Helps to create {@link DockerMachine} instances. */
@Singleton
public class DockerMachineCreator {

    private final DockerConnector           docker;
    private final String                    registry;
    private final boolean                   snapshotUseRegistry;
    private final String                    registryNamespace;
    private final DockerMachineStopDetector dockerMachineStopDetector;

    @Inject
    public DockerMachineCreator(DockerConnector docker,
                                @Named("che.docker.registry") String registry,
                                @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
                                @Named("che.docker.namespace") @Nullable String registryNamespace,
                                DockerMachineStopDetector dockerMachineStopDetector) {
        this.docker = docker;
        this.registry = registry;
        this.snapshotUseRegistry = snapshotUseRegistry;
        this.registryNamespace = registryNamespace;
        this.dockerMachineStopDetector = dockerMachineStopDetector;
    }

    /** Creates new docker machine instance from the short container description. */
    public DockerMachine create(ContainerListEntry container) throws InfrastructureException {
        try {
            return create(docker.inspectContainer(container.getId()));
        } catch (IOException x) {
            throw new InfrastructureException(x.getMessage(), x);
        }
    }

    /** Creates new docker machine instance from the full container description. */
    public DockerMachine create(ContainerInfo container) {
        NetworkSettings networkSettings = container.getNetworkSettings();
        String hostname = networkSettings.getGateway();
        Map<String, ServerConfig> configs = Labels.newDeserializer(container.getConfig().getLabels()).servers();

        return new DockerMachine(container.getId(),
                                 container.getImage(),
                                 docker,
                                 new ServersMapper(hostname).map(networkSettings.getPorts(), configs),
                                 registry,
                                 snapshotUseRegistry,
                                 registryNamespace,
                                 dockerMachineStopDetector);
    }
}
