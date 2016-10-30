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
package org.eclipse.che.plugin.docker.machine.cleaner;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.eclipse.che.plugin.docker.client.params.RemoveContainerParams.create;
import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;

/**
 * Job for periodically clean up inactive docker containers
 *
 * @author Alexander Andrienko
 */
@Singleton
public class DockerContainerCleaner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DockerContainerCleaner.class);

    // TODO replace with WorkspaceManager
    private final CheEnvironmentEngine         environmentEngine;
    private final DockerConnector              dockerConnector;
    private final DockerContainerNameGenerator nameGenerator;

    @Inject
    public DockerContainerCleaner(CheEnvironmentEngine environmentEngine,
                                  DockerConnector dockerConnector,
                                  DockerContainerNameGenerator nameGenerator) {
        this.environmentEngine = environmentEngine;
        this.dockerConnector = dockerConnector;
        this.nameGenerator = nameGenerator;
    }

    @ScheduleRate(periodParameterName = "che.docker.unused_containers_cleanup_min",
                  initialDelayParameterName = "che.docker.unused_containers_cleanup_min",
                  unit = TimeUnit.MINUTES)
    @Override
    public void run() {
        try {
            for (ContainerListEntry container : dockerConnector.listContainers()) {
                Optional<ContainerNameInfo> optional = nameGenerator.parse(container.getNames()[0]);
                if (optional.isPresent()) {
                    try {
                        // container is orphaned if not found exception is thrown
                        environmentEngine.getMachine(optional.get().getWorkspaceId(),
                                                     optional.get().getMachineId());

                    } catch (NotFoundException e) {
                        cleanUp(container);
                    } catch (Exception e) {
                        LOG.error("Failed to clean up inactive container. " + e.getLocalizedMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to get list docker containers", e);
        } catch (Exception e) {
            LOG.error("Failed to clean up inactive containers", e);
        }
    }

    private void cleanUp(ContainerListEntry container) {
        String containerId = container.getId();
        String containerName = container.getNames()[0];

        killContainer(containerId, containerName, container.getStatus());
        removeContainer(containerId, containerName);
    }

    private void killContainer(String containerId, String containerName, String containerStatus) {
        try {
            if (containerStatus.startsWith("Up")) {
                dockerConnector.killContainer(containerId);
                LOG.warn("Unused container with 'id': '{}' and 'name': '{}' was killed ", containerId, containerName);
            }
        } catch (IOException e) {
            LOG.error(format("Failed to kill unused container with 'id': '%s' and 'name': '%s'", containerId, containerName), e);
        }
    }

    private void removeContainer(String containerId, String containerName) {
        try {
            dockerConnector.removeContainer(create(containerId).withForce(true).withRemoveVolumes(true));
            LOG.warn("Unused container with 'id': '{}' and 'name': '{}' was removed", containerId, containerName);
        } catch (IOException e) {
            LOG.error(format("Failed to delete unused container with 'id': '%s' and 'name': '%s'", containerId, containerName), e);
        }
    }
}
