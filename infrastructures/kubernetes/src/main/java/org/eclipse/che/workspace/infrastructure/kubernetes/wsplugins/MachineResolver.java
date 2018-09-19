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

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;

import io.fabric8.kubernetes.api.model.Container;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/** @author Oleksandr Garagatyi */
public class MachineResolver {

  private final Container container;
  private final CheContainer cheContainer;
  private final String defaultSidecarMemoryLimitBytes;
  private final List<ChePluginEndpoint> containerEndpoints;

  public MachineResolver(
      Container container,
      CheContainer cheContainer,
      String defaultSidecarMemoryLimitBytes,
      List<ChePluginEndpoint> containerEndpoints) {
    this.container = container;
    this.cheContainer = cheContainer;
    this.defaultSidecarMemoryLimitBytes = defaultSidecarMemoryLimitBytes;
    this.containerEndpoints = containerEndpoints;
  }

  public InternalMachineConfig resolve() {
    InternalMachineConfig machineConfig =
        new InternalMachineConfig(
            null,
            toServers(containerEndpoints),
            null,
            null,
            toWorkspaceVolumes(cheContainer.getVolumes()));

    normalizeMemory(container, machineConfig);
    return machineConfig;
  }

  private void normalizeMemory(Container container, InternalMachineConfig machineConfig) {
    long ramLimit = Containers.getRamLimit(container);
    if (ramLimit == 0) {
      machineConfig.getAttributes().put(MEMORY_LIMIT_ATTRIBUTE, defaultSidecarMemoryLimitBytes);
    }
  }

  private Map<String, ? extends org.eclipse.che.api.core.model.workspace.config.Volume>
      toWorkspaceVolumes(List<Volume> volumes) {

    Map<String, VolumeImpl> result = new HashMap<>();

    for (Volume volume : volumes) {
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
