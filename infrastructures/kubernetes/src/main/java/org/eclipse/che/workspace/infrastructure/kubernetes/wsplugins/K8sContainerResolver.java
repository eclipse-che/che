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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/**
 * Resolves Kubernetes container {@link Container} configuration from Che workspace sidecar and Che
 * plugin endpoints.
 *
 * @author Oleksandr Garagatyi
 */
public class K8sContainerResolver {

  static final int MAX_CONTAINER_NAME_LENGTH = 63; // K8S container name limit

  private final String pluginName;
  private final CheContainer cheContainer;
  private final List<ChePluginEndpoint> containerEndpoints;

  public K8sContainerResolver(
      String pluginName, CheContainer container, List<ChePluginEndpoint> containerEndpoints) {
    this.cheContainer = container;
    this.pluginName = pluginName;
    this.containerEndpoints = containerEndpoints;
  }

  public List<ChePluginEndpoint> getEndpoints() {
    return containerEndpoints;
  }

  public Container resolve() throws InfrastructureException {
    Container container =
        new ContainerBuilder()
            .withImage(cheContainer.getImage())
            .withName(buildContainerName(pluginName, cheContainer.getName()))
            .withEnv(toK8sEnv(cheContainer.getEnv()))
            .withPorts(getContainerPorts())
            .build();

    provisionMemoryLimit(container, cheContainer);

    return container;
  }

  private void provisionMemoryLimit(Container container, CheContainer cheContainer)
      throws InfrastructureException {
    String memoryLimit = cheContainer.getMemoryLimit();
    if (isNullOrEmpty(memoryLimit)) {
      return;
    }
    try {
      KubernetesSize.toBytes(memoryLimit);
    } catch (IllegalArgumentException e) {
      throw new InfrastructureException(
          format(
              "Sidecar memory limit field contains illegal value '%s'. Error: '%s'",
              memoryLimit, e.getMessage()));
    }
    Containers.addRamLimit(container, memoryLimit);
    Containers.addRamRequest(container, memoryLimit);
  }

  private List<ContainerPort> getContainerPorts() {
    return containerEndpoints
        .stream()
        .map(
            endpoint ->
                new ContainerPortBuilder()
                    .withContainerPort(endpoint.getTargetPort())
                    .withProtocol("TCP")
                    .build())
        .collect(Collectors.toList());
  }

  private List<io.fabric8.kubernetes.api.model.EnvVar> toK8sEnv(List<EnvVar> env) {
    if (env == null) {
      return emptyList();
    }
    return env.stream()
        .map(e -> new io.fabric8.kubernetes.api.model.EnvVar(e.getName(), e.getValue(), null))
        .collect(Collectors.toList());
  }

  private String buildContainerName(String pluginName, String cheContainerName) {
    if (pluginName == null) {
      return cheContainerName.substring(
          0, min(cheContainerName.length(), MAX_CONTAINER_NAME_LENGTH));
    }
    String preliminaryName = (pluginName + "-" + cheContainerName).toLowerCase();
    if (preliminaryName.length() <= MAX_CONTAINER_NAME_LENGTH) {
      return preliminaryName;
    }
    final String limitedContainerName =
        cheContainerName.substring(0, min(cheContainerName.length(), 49));
    return (pluginName.substring(
                0,
                min(
                    pluginName.length(),
                    MAX_CONTAINER_NAME_LENGTH - limitedContainerName.length() - 1))
            + "-"
            + limitedContainerName)
        .toLowerCase();
  }
}
