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
package org.eclipse.che.plugin.docker.machine.cleaner;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.json.Filters;
import org.eclipse.che.plugin.docker.client.json.network.Network;
import org.eclipse.che.plugin.docker.client.params.network.GetNetworksParams;
import org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.plugin.docker.client.params.RemoveContainerParams.create;
import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;

/**
 * Job for periodically clean up abandoned docker containers and networks created by CHE.
 * Also, logs active containers list.
 *
 * @author Alexander Andrienko
 * @author Mykola Morhun
 */
@Singleton
public class DockerAbandonedResourcesCleaner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DockerAbandonedResourcesCleaner.class);

    private static final Filters           NETWORK_FILTERS          = new Filters().withFilter("type", "custom");
    private static final GetNetworksParams GET_NETWORKS_PARAMS      = GetNetworksParams.create().withFilters(NETWORK_FILTERS);
    private static final String            WORKSPACE_ID_REGEX_GROUP = "workspaceId";
    private static final String            CHE_NETWORK_REGEX        = "^(?<" + WORKSPACE_ID_REGEX_GROUP + ">workspace[a-z\\d]{16})_[a-z\\d]{16}$";
    private static final Pattern           CHE_NETWORK_PATTERN      = Pattern.compile(CHE_NETWORK_REGEX);

    // TODO replace with WorkspaceManager
    private final CheEnvironmentEngine         environmentEngine;
    private final DockerConnector              dockerConnector;
    private final DockerContainerNameGenerator nameGenerator;
    private final WorkspaceRuntimes            runtimes;
    private final Set<String>                  additionalNetworks;

    @Inject
    public DockerAbandonedResourcesCleaner(CheEnvironmentEngine environmentEngine,
                                           DockerConnector dockerConnector,
                                           DockerContainerNameGenerator nameGenerator,
                                           WorkspaceRuntimes workspaceRuntimes,
                                           @Named("machine.docker.networks") Set<Set<String>> additionalNetworks) {
        this.environmentEngine = environmentEngine;
        this.dockerConnector = dockerConnector;
        this.nameGenerator = nameGenerator;
        this.runtimes = workspaceRuntimes;
        this.additionalNetworks = additionalNetworks.stream()
                                                    .flatMap(Set::stream)
                                                    .collect(toSet());
    }

    @ScheduleRate(periodParameterName = "che.docker.cleanup_period_min",
                  initialDelay = 0L,
                  unit = TimeUnit.MINUTES)
    @Override
    public void run() {
        cleanContainers();
        cleanNetworks();
    }

    /**
     * Cleans up CHE docker containers which don't tracked by API any more.
     */
    @VisibleForTesting
    void cleanContainers() {
        List<String> activeContainers = new ArrayList<>();
        try {
            for (ContainerListEntry container : dockerConnector.listContainers()) {
                String containerName = container.getNames()[0];
                Optional<ContainerNameInfo> optional = nameGenerator.parse(containerName);
                if (optional.isPresent()) {
                    try {
                        // container is orphaned if not found exception is thrown
                        environmentEngine.getMachine(optional.get().getWorkspaceId(),
                                                     optional.get().getMachineId());
                        activeContainers.add(containerName);
                    } catch (NotFoundException e) {
                        cleanUpContainer(container);
                    } catch (Exception e) {
                        LOG.error(format("Failed to check activity for container with name '%s'. Cause: %s",
                                         containerName, e.getLocalizedMessage()), e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to get list docker containers", e);
        } catch (Exception e) {
            LOG.error("Failed to clean up inactive containers", e);
        }
        LOG.info("List containers registered in the api: " + activeContainers);
    }

    private void cleanUpContainer(ContainerListEntry container) {
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

    /**
     * Deletes all abandoned CHE networks.
     * Abandoned networks appear when workspaces were stopped unexpectedly,
     * for example, force stop che, restart docker, turn off PC, etc.
     * A network is considered abandoned when it doesn't contain a container.
     * To do this job more efficiently, it should be invoked after cleaning of abandoned containers.
     */
    @VisibleForTesting
    void cleanNetworks() {
        try {
            for (Network network : dockerConnector.getNetworks(GET_NETWORKS_PARAMS)) {
                Matcher cheNetworkMatcher = CHE_NETWORK_PATTERN.matcher(network.getName());
                if (cheNetworkMatcher.matches() && network.getContainers().isEmpty() && !additionalNetworks.contains(network.getName()) &&
                    !runtimes.hasRuntime(cheNetworkMatcher.group(WORKSPACE_ID_REGEX_GROUP))) {
                    try {
                        dockerConnector.removeNetwork(network.getId());
                    } catch (IOException e) {
                        LOG.warn("Failed to remove abandoned network: " + network.getName(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to get list of docker networks", e);
        }
    }

}
