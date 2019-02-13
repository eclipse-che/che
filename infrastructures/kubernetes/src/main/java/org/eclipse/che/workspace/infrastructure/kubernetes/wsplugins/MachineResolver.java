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
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE;

import io.fabric8.kubernetes.api.model.Container;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
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

  private final String pluginId;
  private final Container container;
  private final CheContainer cheContainer;
  private final String defaultSidecarMemoryLimitBytes;
  private final List<ChePluginEndpoint> containerEndpoints;
  private Map<String, String> wsAttributes;
  private final Pair<String, String> projectsRootPathEnvVar;

  public MachineResolver(
      String pluginId,
      Pair<String, String> projectsRootPathEnvVar,
      Container container,
      CheContainer cheContainer,
      String defaultSidecarMemoryLimitBytes,
      List<ChePluginEndpoint> containerEndpoints,
      Map<String, String> wsAttributes) {
    this.pluginId = pluginId;
    this.container = container;
    this.cheContainer = cheContainer;
    this.defaultSidecarMemoryLimitBytes = defaultSidecarMemoryLimitBytes;
    this.containerEndpoints = containerEndpoints;
    this.wsAttributes = wsAttributes != null ? wsAttributes : Collections.emptyMap();
    this.projectsRootPathEnvVar = projectsRootPathEnvVar;
  }

  public InternalMachineConfig resolve() throws InfrastructureException {
    InternalMachineConfig machineConfig =
        new InternalMachineConfig(
            null, toServers(containerEndpoints), null, null, toWorkspaceVolumes(cheContainer));

    normalizeMemory(container, machineConfig);
    return machineConfig;
  }

  private void normalizeMemory(Container container, InternalMachineConfig machineConfig) {
    long ramLimit = Containers.getRamLimit(container);
    if (ramLimit == 0) {
      machineConfig.getAttributes().put(MEMORY_LIMIT_ATTRIBUTE, defaultSidecarMemoryLimitBytes);
    }
    String overriddenSidecarMemLimit =
        wsAttributes.get(format(SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE, pluginId));
    if (!isNullOrEmpty(overriddenSidecarMemLimit)) {
      machineConfig
          .getAttributes()
          .put(
              MEMORY_LIMIT_ATTRIBUTE,
              Long.toString(KubernetesSize.toBytes(overriddenSidecarMemLimit)));
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
                pluginId,
                PROJECTS_VOLUME_NAME,
                container.getName(),
                volume.getMountPath(),
                projectsRootPathEnvVar.second,
                projectsRootPathEnvVar.first));
      }
      result.put(volume.getName(), new VolumeImpl().withPath(volume.getMountPath()));
    }
    return result;
  }

  private Map<String, ? extends ServerConfig> toServers(List<ChePluginEndpoint> endpoints) {
    return endpoints.stream().collect(toMap(ChePluginEndpoint::getName, this::toServer));
  }

  private ServerConfigImpl toServer(ChePluginEndpoint endpoint) {
    ServerConfigImpl serverConfig =
        new ServerConfigImpl().withPort(Integer.toString(endpoint.getTargetPort()) + "/tcp");
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
