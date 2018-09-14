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
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/** @author Alexander Garagatyi */
public class MachineResolver {

  private final Container container;
  private final CheContainer cheContainer;
  private final String defaultSidecarMemorySizeAttribute;
  private final List<ChePluginEndpoint> containerEndpoints;

  public MachineResolver(
      Container container,
      CheContainer cheContainer,
      String defaultSidecarMemorySizeAttribute,
      List<ChePluginEndpoint> containerEndpoints) {
    this.container = container;
    this.cheContainer = cheContainer;
    this.defaultSidecarMemorySizeAttribute = defaultSidecarMemorySizeAttribute;
    this.containerEndpoints = containerEndpoints;
  }

  public InternalMachineConfig getMachine() {
    InternalMachineConfig machineConfig =
        addMachineConfig(containerEndpoints, cheContainer.getVolumes());

    normalizeMemory(container, machineConfig);
    return machineConfig;
  }

  private InternalMachineConfig addMachineConfig(
      List<ChePluginEndpoint> endpoints, List<Volume> volumes) {

    return new InternalMachineConfig(
        null, toWorkspaceServers(endpoints), null, null, toWorkspaceVolumes(volumes));
  }

  private void normalizeMemory(Container container, InternalMachineConfig machineConfig) {
    long ramLimit = Containers.getRamLimit(container);
    Map<String, String> attributes = machineConfig.getAttributes();
    if (ramLimit == 0) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, defaultSidecarMemorySizeAttribute);
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

  private Map<String, ? extends ServerConfig> toWorkspaceServers(
      List<ChePluginEndpoint> endpoints) {
    return endpoints
        .stream()
        .collect(
            toMap(ChePluginEndpoint::getName, endpoint -> normalizeServer(toServer(endpoint))));
  }

  private ServerConfigImpl toServer(ChePluginEndpoint endpoint) {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("internal", Boolean.toString(!endpoint.isPublic()));
    endpoint
        .getAttributes()
        .forEach(
            (k, v) -> {
              if (!"protocol".equals(k) && !"path".equals(k)) {
                attributes.put(k, v);
              }
            });
    return new ServerConfigImpl(
        Integer.toString(endpoint.getTargetPort()),
        endpoint.getAttributes().get("protocol"),
        endpoint.getAttributes().get("path"),
        attributes);
  }

  private ServerConfigImpl normalizeServer(ServerConfigImpl serverConfig) {
    String port = serverConfig.getPort();
    if (port != null && !port.contains("/")) {
      serverConfig.setPort(port + "/tcp");
    }
    return serverConfig;
  }
}
