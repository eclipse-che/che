/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainerNameGenerator;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/** @author Alexander Garagatyi */
public class DockerEnvironmentNormalizer {
  private final ContainerNameGenerator containerNameGenerator;

  @Inject
  public DockerEnvironmentNormalizer(ContainerNameGenerator containerNameGenerator) {
    this.containerNameGenerator = containerNameGenerator;
  }

  public void normalize(DockerEnvironment dockerEnvironment, RuntimeIdentity identity)
      throws InfrastructureException {

    String networkId = identity.getWorkspaceId() + "_" + identity.getEnvName();
    dockerEnvironment.setNetwork(networkId);

    Map<String, DockerContainerConfig> containers = dockerEnvironment.getContainers();
    for (Map.Entry<String, DockerContainerConfig> containerEntry : containers.entrySet()) {
      DockerContainerConfig containerConfig = containerEntry.getValue();

      containerConfig.setContainerName(
          containerNameGenerator.generateContainerName(
              identity.getWorkspaceId(),
              containerConfig.getId(),
              identity.getOwnerId(),
              containerEntry.getKey()));
    }
    normalizeNames(dockerEnvironment);
  }

  private void normalizeNames(DockerEnvironment dockerEnvironment) throws InfrastructureException {
    Map<String, DockerContainerConfig> containers = dockerEnvironment.getContainers();
    for (Map.Entry<String, DockerContainerConfig> containerEntry : containers.entrySet()) {
      DockerContainerConfig container = containerEntry.getValue();
      normalizeVolumesFrom(container, containers);
      normalizeLinks(container, containers);
    }
  }

  // replace machines names in volumes_from with containers IDs
  private void normalizeVolumesFrom(
      DockerContainerConfig container, Map<String, DockerContainerConfig> containers) {
    if (container.getVolumesFrom() != null) {
      container.setVolumesFrom(
          container
              .getVolumesFrom()
              .stream()
              .map(containerName -> containers.get(containerName).getContainerName())
              .collect(toList()));
    }
  }

  /**
   * Replaces linked to this container's name with container name which represents the container in
   * links section. The problem is that a user writes names of other services in links section in
   * compose file. But actually links are constraints and their values should be names of containers
   * (not services) to be linked. <br>
   * For example: serviceDB:serviceDbAlias -> container_1234:serviceDbAlias <br>
   * If alias is omitted then service name will be used.
   *
   * @param containerToNormalizeLinks container which links will be normalized
   * @param containers all containers in environment
   */
  private void normalizeLinks(
      DockerContainerConfig containerToNormalizeLinks,
      Map<String, DockerContainerConfig> containers)
      throws InfrastructureException {
    List<String> normalizedLinks = new ArrayList<>();
    for (String link : containerToNormalizeLinks.getLinks()) {
      // a link has format: 'name:alias' or 'name'
      String containerNameAndAliasToLink[] = link.split(":", 2);
      String containerName = containerNameAndAliasToLink[0];
      String containerAlias =
          (containerNameAndAliasToLink.length > 1) ? containerNameAndAliasToLink[1] : null;
      DockerContainerConfig containerLinkTo = containers.get(containerName);
      if (containerLinkTo != null) {
        String containerNameLinkTo = containerLinkTo.getContainerName();
        normalizedLinks.add(
            (containerAlias == null)
                ? containerNameLinkTo
                : containerNameLinkTo + ':' + containerAlias);
      } else {
        // should never happens. Errors like this should be filtered by CheEnvironmentValidator
        throw new InfrastructureException(
            "Attempt to link non existing container "
                + containerName
                + " to "
                + containerToNormalizeLinks
                + " container.");
      }
    }
    containerToNormalizeLinks.setLinks(normalizedLinks);
  }
}
