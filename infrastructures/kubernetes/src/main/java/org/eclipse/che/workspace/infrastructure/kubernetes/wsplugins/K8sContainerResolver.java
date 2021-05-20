/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
import io.fabric8.kubernetes.api.model.ExecAction;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsplugins.model.Exec;
import org.eclipse.che.api.workspace.server.wsplugins.model.Handler;
import org.eclipse.che.api.workspace.server.wsplugins.model.Lifecycle;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/**
 * Resolves Kubernetes container {@link Container} configuration from Che workspace sidecar and Che
 * plugin endpoints.
 *
 * @author Oleksandr Garagatyi
 */
public class K8sContainerResolver {

  private final String imagePullPolicy;
  private final CheContainer cheContainer;
  private final List<ChePluginEndpoint> containerEndpoints;

  public K8sContainerResolver(
      String imagePullPolicy, CheContainer container, List<ChePluginEndpoint> containerEndpoints) {
    this.imagePullPolicy = imagePullPolicy;
    this.cheContainer = container;
    this.containerEndpoints = containerEndpoints;
  }

  public List<ChePluginEndpoint> getEndpoints() {
    return containerEndpoints;
  }

  public Container resolve() throws InfrastructureException {
    Container container =
        new ContainerBuilder()
            .withImage(cheContainer.getImage())
            .withImagePullPolicy(imagePullPolicy)
            .withName(buildContainerName(cheContainer.getName()))
            .withEnv(toK8sEnv(cheContainer.getEnv()))
            .withPorts(getContainerPorts())
            .withCommand(cheContainer.getCommand())
            .withArgs(cheContainer.getArgs())
            .withLifecycle(toK8sLifecycle(cheContainer.getLifecycle()))
            .build();

    provisionMemoryLimit(container, cheContainer);
    provisionMemoryRequest(container, cheContainer);
    provisionCpuLimit(container, cheContainer);
    provisionCpuRequest(container, cheContainer);

    return container;
  }

  private io.fabric8.kubernetes.api.model.Lifecycle toK8sLifecycle(Lifecycle lifecycle) {
    if (lifecycle == null) {
      return null;
    }
    io.fabric8.kubernetes.api.model.Handler postStart = toK8sHandler(lifecycle.getPostStart());
    io.fabric8.kubernetes.api.model.Handler preStop = toK8sHandler(lifecycle.getPreStop());
    io.fabric8.kubernetes.api.model.Lifecycle k8sLifecycle =
        new io.fabric8.kubernetes.api.model.Lifecycle(postStart, preStop);
    return k8sLifecycle;
  }

  private io.fabric8.kubernetes.api.model.Handler toK8sHandler(Handler handler) {
    if (handler == null || handler.getExec() == null) {
      return null;
    }
    ExecAction exec = toExecAction(handler.getExec());
    if (exec == null) {
      return null;
    }
    // TODO: add 'httpGetAction' and 'tcpSocketAction' support
    io.fabric8.kubernetes.api.model.Handler k8SHandler =
        new io.fabric8.kubernetes.api.model.Handler(exec, null, null);
    return k8SHandler;
  }

  private ExecAction toExecAction(Exec exec) {
    if (exec == null || exec.getCommand().isEmpty()) {
      return null;
    }
    return new ExecAction(exec.getCommand());
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
  }

  private void provisionMemoryRequest(Container container, CheContainer cheContainer)
      throws InfrastructureException {
    String memoryRequest = cheContainer.getMemoryRequest();
    if (isNullOrEmpty(memoryRequest)) {
      return;
    }
    try {
      KubernetesSize.toBytes(memoryRequest);
    } catch (IllegalArgumentException e) {
      throw new InfrastructureException(
          format(
              "Sidecar memory request field contains illegal value '%s'. Error: '%s'",
              memoryRequest, e.getMessage()));
    }
    Containers.addRamRequest(container, memoryRequest);
  }

  private void provisionCpuLimit(Container container, CheContainer cheContainer)
      throws InfrastructureException {
    String cpuLimit = cheContainer.getCpuLimit();
    if (isNullOrEmpty(cpuLimit)) {
      return;
    }
    try {
      KubernetesSize.toCores(cpuLimit);
    } catch (IllegalArgumentException e) {
      throw new InfrastructureException(
          format(
              "Sidecar CPU limit field contains illegal value '%s'. Error: '%s'",
              cpuLimit, e.getMessage()));
    }
    Containers.addCpuLimit(container, cpuLimit);
  }

  private void provisionCpuRequest(Container container, CheContainer cheContainer)
      throws InfrastructureException {
    String cpuRequest = cheContainer.getCpuRequest();
    if (isNullOrEmpty(cpuRequest)) {
      return;
    }
    try {
      KubernetesSize.toCores(cpuRequest);
    } catch (IllegalArgumentException e) {
      throw new InfrastructureException(
          format(
              "Sidecar CPU request field contains illegal value '%s'. Error: '%s'",
              cpuRequest, e.getMessage()));
    }
    Containers.addCpuRequest(container, cpuRequest);
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

  private String buildContainerName(String cheContainerName) {
    String uniqueName = NameGenerator.generate(cheContainerName, 3).toLowerCase();
    return uniqueName.substring(0, min(uniqueName.length(), Names.MAX_CONTAINER_NAME_LENGTH));
  }
}
