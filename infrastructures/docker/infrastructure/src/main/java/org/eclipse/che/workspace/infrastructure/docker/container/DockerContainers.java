/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.container;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.eclipse.che.infrastructure.docker.client.params.ListContainersParams;
import org.eclipse.che.workspace.infrastructure.docker.Labels;

/** Facade for operations with docker containers in infrastructure domain context. */
@Singleton
public class DockerContainers {

  private final DockerConnector docker;

  @Inject
  public DockerContainers(DockerConnector docker) {
    this.docker = docker;
  }

  /**
   * Lookups all containers owned by runtime with given identity.
   *
   * @param id identity of runtime which owns containers
   * @return list of running containers owned by runtime with given id
   * @throws InternalInfrastructureException when any error occurs during lookup
   */
  public List<ContainerListEntry> find(RuntimeIdentity id) throws InternalInfrastructureException {
    return listNonStoppedContainers(
        Labels.newSerializer()
            .runtimeId(id)
            .labels()
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + '=' + entry.getValue())
            .toArray(String[]::new));
  }

  /**
   * Looks up all runtime identifiers provided by running containers labels.
   *
   * @return unique runtime identifiers of running containers
   * @throws InternalInfrastructureException when any error occurs during lookup
   */
  public Set<RuntimeIdentity> findIdentities() throws InternalInfrastructureException {
    return listNonStoppedContainers(
            Labels.LABEL_WORKSPACE_ID, Labels.LABEL_WORKSPACE_ENV, Labels.LABEL_WORKSPACE_OWNER)
        .stream()
        .map(e -> Labels.newDeserializer(e.getLabels()).runtimeId())
        .collect(Collectors.toSet());
  }

  private List<ContainerListEntry> listNonStoppedContainers(String... labelFilterValues)
      throws InternalInfrastructureException {
    try {
      return docker.listContainers(
          ListContainersParams.create()
              .withAll(false)
              .withFilters(Filters.label(labelFilterValues)));
    } catch (IOException x) {
      throw new InternalInfrastructureException(x.getMessage(), x);
    }
  }
}
