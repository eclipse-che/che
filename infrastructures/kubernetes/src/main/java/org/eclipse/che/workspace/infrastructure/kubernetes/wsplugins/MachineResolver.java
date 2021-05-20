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
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Quantity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/** @author Oleksandr Garagatyi */
public class MachineResolver {

  private final Container container;
  private final CheContainer cheContainer;
  private final String defaultSidecarMemoryLimitBytes;
  private final String defaultSidecarMemoryRequestBytes;
  private final String defaultSidecarCpuLimitCores;
  private final String defaultSidecarCpuRequestCores;
  private final List<ChePluginEndpoint> containerEndpoints;
  private final Pair<String, String> projectsRootPathEnvVar;
  private final Component component;

  public MachineResolver(
      Pair<String, String> projectsRootPathEnvVar,
      Container container,
      CheContainer cheContainer,
      String defaultSidecarMemoryLimitBytes,
      String defaultSidecarMemoryRequestBytes,
      String defaultSidecarCpuLimitCores,
      String defaultSidecarCpuRequestCores,
      List<ChePluginEndpoint> containerEndpoints,
      Component component) {
    this.container = container;
    this.cheContainer = cheContainer;
    this.defaultSidecarMemoryLimitBytes = defaultSidecarMemoryLimitBytes;
    this.defaultSidecarMemoryRequestBytes = defaultSidecarMemoryRequestBytes;
    this.defaultSidecarCpuLimitCores = defaultSidecarCpuLimitCores;
    this.defaultSidecarCpuRequestCores = defaultSidecarCpuRequestCores;
    this.containerEndpoints = containerEndpoints;
    this.projectsRootPathEnvVar = projectsRootPathEnvVar;
    this.component = component;
  }

  public InternalMachineConfig resolve() throws InfrastructureException {
    InternalMachineConfig machineConfig =
        new InternalMachineConfig(
            toServers(containerEndpoints),
            emptyMap(),
            resolveMachineAttributes(),
            toWorkspaceVolumes(cheContainer));
    applyDevfileVolumes(machineConfig);
    applyDevfileEndpoints(machineConfig);
    normalizeMemory(container, machineConfig);
    normalizeCpu(container, machineConfig);
    return machineConfig;
  }

  private void applyDevfileVolumes(InternalMachineConfig machineConfig) {
    for (org.eclipse.che.api.core.model.workspace.devfile.Volume volume : component.getVolumes()) {
      machineConfig
          .getVolumes()
          .put(volume.getName(), new VolumeImpl().withPath(volume.getContainerPath()));
    }
  }

  private void applyDevfileEndpoints(InternalMachineConfig machineConfig)
      throws InfrastructureException {
    for (org.eclipse.che.api.core.model.workspace.devfile.Endpoint endpoint :
        component.getEndpoints()) {
      if (!machineConfig.getServers().containsKey(endpoint.getName())) {
        machineConfig
            .getServers()
            .put(endpoint.getName(), ServerConfigImpl.createFromEndpoint(endpoint));
      } else {
        throw new InfrastructureException(
            format(
                "Devfile overrides the endpoint '%s' of the plugin/editor component '%s'. "
                    + "This is not allowed because it would most probably cause the workspace "
                    + "to malfunction. Please change the name of the endpoint in the devfile and try "
                    + "to start the workspace again.",
                component.getId(), endpoint.getName()));
      }
    }
  }

  private Map<String, String> resolveMachineAttributes() {
    Map<String, String> attributes = new HashMap<>();

    String alias = component.getAlias();
    if (alias != null) {
      attributes.put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, alias);
    }

    return attributes;
  }

  private void normalizeMemory(Container container, InternalMachineConfig machineConfig) {
    long ramLimit = Containers.getRamLimit(container);
    if (ramLimit == 0) {
      machineConfig.getAttributes().put(MEMORY_LIMIT_ATTRIBUTE, defaultSidecarMemoryLimitBytes);
    }
    String overriddenSidecarMemLimit = component.getMemoryLimit();
    if (!isNullOrEmpty(overriddenSidecarMemLimit)) {
      machineConfig
          .getAttributes()
          .put(
              MEMORY_LIMIT_ATTRIBUTE,
              Long.toString(
                  Quantity.getAmountInBytes(Quantity.parse(overriddenSidecarMemLimit))
                      .longValue()));
    }

    long ramRequest = Containers.getRamRequest(container);
    if (ramRequest == 0) {
      machineConfig.getAttributes().put(MEMORY_REQUEST_ATTRIBUTE, defaultSidecarMemoryRequestBytes);
    }
    String overriddenSidecarMemRequest = component.getMemoryRequest();
    if (!isNullOrEmpty(overriddenSidecarMemRequest)) {
      machineConfig
          .getAttributes()
          .put(
              MEMORY_REQUEST_ATTRIBUTE,
              Long.toString(
                  Quantity.getAmountInBytes(Quantity.parse(overriddenSidecarMemRequest))
                      .longValue()));
    }
  }

  private void normalizeCpu(Container container, InternalMachineConfig machineConfig) {
    float cpuLimit = Containers.getCpuLimit(container);
    if (cpuLimit == 0) {
      machineConfig.getAttributes().put(CPU_LIMIT_ATTRIBUTE, defaultSidecarCpuLimitCores);
    }
    String overriddenSidecarCpuLimit = component.getCpuLimit();
    if (!isNullOrEmpty(overriddenSidecarCpuLimit)) {
      machineConfig
          .getAttributes()
          .put(
              CPU_LIMIT_ATTRIBUTE,
              Float.toString(KubernetesSize.toCores(overriddenSidecarCpuLimit)));
    }

    float cpuRequest = Containers.getCpuRequest(container);
    if (cpuRequest == 0) {
      machineConfig.getAttributes().put(CPU_REQUEST_ATTRIBUTE, defaultSidecarCpuRequestCores);
    }
    String overriddenSidecarCpuRequest = component.getCpuRequest();
    if (!isNullOrEmpty(overriddenSidecarCpuRequest)) {
      machineConfig
          .getAttributes()
          .put(
              CPU_REQUEST_ATTRIBUTE,
              Float.toString(KubernetesSize.toCores(overriddenSidecarCpuRequest)));
    }
  }

  private Map<String, ? extends org.eclipse.che.api.core.model.workspace.config.Volume>
      toWorkspaceVolumes(CheContainer container) throws InfrastructureException {

    Map<String, VolumeImpl> result = new HashMap<>();

    if (container.isMountSources()) {
      result.put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectsRootPathEnvVar.second));
    }

    for (Volume volume : container.getVolumes()) {
      if (volume.getName().equals(PROJECTS_VOLUME_NAME)
          && !projectsRootPathEnvVar.second.equals(volume.getMountPath())) {
        throw new InfrastructureException(
            format(
                "Plugin '%s' tried to manually mount the '%s' volume into its container '%s' on"
                    + " path '%s'. This is illegal because sources need to be mounted to '%s'. Set"
                    + " the mountSources attribute to true instead and remove the manual volume"
                    + " mount in the plugin. After that the mount path of the sources will be"
                    + " available automatically in the '%s' environment variable.",
                component.getId(),
                PROJECTS_VOLUME_NAME,
                container.getName(),
                volume.getMountPath(),
                projectsRootPathEnvVar.second,
                projectsRootPathEnvVar.first));
      }
    }
    return result;
  }

  private Map<String, ? extends ServerConfig> toServers(List<ChePluginEndpoint> endpoints) {
    return endpoints.stream().collect(toMap(ChePluginEndpoint::getName, this::toServer));
  }

  private ServerConfigImpl toServer(ChePluginEndpoint endpoint) {
    ServerConfigImpl serverConfig =
        new ServerConfigImpl().withPort(endpoint.getTargetPort() + "/tcp");
    serverConfig.getAttributes().put("internal", Boolean.toString(!endpoint.isPublic()));
    for (Entry<String, String> attribute : endpoint.getAttributes().entrySet()) {
      switch (attribute.getKey()) {
        case "protocol":
          serverConfig.setProtocol(attribute.getValue());
          break;
        case "path":
          serverConfig.setPath(attribute.getValue());
          break;
        default:
          serverConfig.getAttributes().put(attribute.getKey(), attribute.getValue());
      }
    }
    return serverConfig;
  }
}
