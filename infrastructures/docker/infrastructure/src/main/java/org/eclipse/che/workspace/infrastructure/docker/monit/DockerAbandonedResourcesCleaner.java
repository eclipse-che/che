/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.monit;

import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.eclipse.che.infrastructure.docker.client.json.network.Network;
import org.eclipse.che.infrastructure.docker.client.params.RemoveContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.network.GetNetworksParams;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainerNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job for periodically clean up abandoned docker containers and networks created by CHE. Also, logs
 * active containers list.
 *
 * @author Alexander Andrienko
 * @author Mykola Morhun
 */
@Singleton
public class DockerAbandonedResourcesCleaner implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(DockerAbandonedResourcesCleaner.class);

  private static final Filters NETWORK_FILTERS = new Filters().withFilter("type", "custom");
  private static final GetNetworksParams GET_NETWORKS_PARAMS =
      GetNetworksParams.create().withFilters(NETWORK_FILTERS);
  private static final String WORKSPACE_ID_REGEX_GROUP = "workspaceId";
  private static final String CHE_NETWORK_REGEX =
      "^(?<" + WORKSPACE_ID_REGEX_GROUP + ">workspace[a-z\\d]{16})_[a-z\\d]{16}$";
  private static final Pattern CHE_NETWORK_PATTERN = Pattern.compile(CHE_NETWORK_REGEX);

  private final WorkspaceManager workspaceManager;
  private final DockerConnector dockerConnector;
  private final ContainerNameGenerator nameGenerator;
  private final WorkspaceRuntimes runtimes;

  @Inject
  public DockerAbandonedResourcesCleaner(
      WorkspaceManager workspaceManager,
      DockerConnector dockerConnector,
      ContainerNameGenerator nameGenerator,
      WorkspaceRuntimes workspaceRuntimes) {
    this.workspaceManager = workspaceManager;
    this.dockerConnector = dockerConnector;
    this.nameGenerator = nameGenerator;
    this.runtimes = workspaceRuntimes;
  }

  @ScheduleRate(
    periodParameterName = "che.docker.cleanup_period_min",
    initialDelay = 0L,
    unit = TimeUnit.MINUTES
  )
  @Override
  public void run() {
    cleanContainers();
    cleanNetworks();
  }

  /** Cleans up CHE docker containers which don't tracked by API any more. */
  @VisibleForTesting
  void cleanContainers() {
    List<String> activeContainers = new ArrayList<>();
    try {
      for (ContainerListEntry container : dockerConnector.listContainers()) {
        String containerName = container.getNames()[0];
        Optional<ContainerNameGenerator.ContainerNameInfo> optional =
            nameGenerator.parse(containerName);
        if (optional.isPresent()) {
          try {
            // container is orphaned if not found exception is thrown
            WorkspaceImpl workspace =
                workspaceManager.getWorkspace(optional.get().getWorkspaceId());
            // if there is no such machine container will be cleaned up below
            if (workspace.getRuntime().getMachines().containsKey(optional.get().getMachineId())) {
              activeContainers.add(containerName);
              continue;
            }
          } catch (NotFoundException e) {
            // container will be cleaned up below
          } catch (Exception e) {
            LOG.error(
                format(
                    "Failed to check activity for container with name '%s'. Cause: %s",
                    containerName, e.getLocalizedMessage()),
                e);
          }
          cleanUpContainer(container);
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
        LOG.warn(
            "Unused container with 'id': '{}' and 'name': '{}' was killed ",
            containerId,
            containerName);
      }
    } catch (IOException e) {
      LOG.error(
          format(
              "Failed to kill unused container with 'id': '%s' and 'name': '%s'",
              containerId, containerName),
          e);
    }
  }

  private void removeContainer(String containerId, String containerName) {
    try {
      dockerConnector.removeContainer(
          RemoveContainerParams.create(containerId).withForce(true).withRemoveVolumes(true));
      LOG.warn(
          "Unused container with 'id': '{}' and 'name': '{}' was removed",
          containerId,
          containerName);
    } catch (IOException e) {
      LOG.error(
          format(
              "Failed to delete unused container with 'id': '%s' and 'name': '%s'",
              containerId, containerName),
          e);
    }
  }

  /**
   * Deletes all abandoned CHE networks. Abandoned networks appear when workspaces were stopped
   * unexpectedly, for example, force stop che, restart docker, turn off PC, etc. A network is
   * considered abandoned when it doesn't contain a container. To do this job more efficiently, it
   * should be invoked after cleaning of abandoned containers.
   */
  @VisibleForTesting
  void cleanNetworks() {
    try {
      List<Network> customNetworks = dockerConnector.getNetworks(GET_NETWORKS_PARAMS);
      // This workaround is added because of docker bug which returns null instead of empty list
      // See https://github.com/docker/docker/issues/29946
      if (customNetworks == null) {
        return;
      }

      for (Network network : customNetworks) {
        Matcher cheNetworkMatcher = CHE_NETWORK_PATTERN.matcher(network.getName());
        if (cheNetworkMatcher.matches()
            && network.getContainers().isEmpty()
            && !runtimes.hasRuntime(cheNetworkMatcher.group(WORKSPACE_ID_REGEX_GROUP))) {

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
