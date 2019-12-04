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
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_ENV_VARIABLES_ATTR_TEMPLATE;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE;

import io.fabric8.kubernetes.api.model.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
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

  private final String pluginPublisherAndName;
  private final String pluginId;
  private final Container container;
  private final CheContainer cheContainer;
  private final String defaultSidecarMemoryLimitBytes;
  private final List<ChePluginEndpoint> containerEndpoints;
  private Map<String, String> wsAttributes;
  private final Pair<String, String> projectsRootPathEnvVar;

  public MachineResolver(
      String pluginPublisher,
      String pluginName,
      String pluginId,
      Pair<String, String> projectsRootPathEnvVar,
      Container container,
      CheContainer cheContainer,
      String defaultSidecarMemoryLimitBytes,
      List<ChePluginEndpoint> containerEndpoints,
      Map<String, String> wsAttributes) {
    this.pluginPublisherAndName = pluginPublisher + "/" + pluginName;
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
            null,
            toServers(containerEndpoints),
            toEnvVariables(wsAttributes),
            toMachineAttributes(pluginId, wsAttributes),
            toWorkspaceVolumes(cheContainer));

    normalizeMemory(container, machineConfig);
    return machineConfig;
  }

  private Map<String,String> toEnvVariables(Map<String,String> wsAttributes) {
    String envVars = wsAttributes
        .get(format(SIDECAR_ENV_VARIABLES_ATTR_TEMPLATE, pluginPublisherAndName));
    if (isNullOrEmpty(envVars)) {
      return null;
    }
    return Stream.of(envVars.split(","))
        // only splitting by 1'st '=' since env value may also contain it
        .map(value -> value.split("=", 2))
        .collect(toMap(arr -> arr[0], arr -> arr[1]));
  }

  private Map<String, String> toMachineAttributes(
      String pluginId, Map<String, String> wsAttributes) {
    Map<String, String> attributes = new HashMap<>();

    Optional<String> pluginAlias = findPluginAlias(pluginId, wsAttributes);
    pluginAlias.ifPresent(s -> attributes.put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, s));

    return attributes;
  }

  private Optional<String> findPluginAlias(String pluginId, Map<String, String> wsAttributes) {

    List<String> aliases = new ArrayList<>();

    String pluginComponentAliases =
        wsAttributes.get(PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE);
    if (!isNullOrEmpty(pluginComponentAliases)) {
      aliases.addAll(asList(pluginComponentAliases.split(",")));
    }

    String editorComponentAlias = wsAttributes.get(EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE);
    if (!isNullOrEmpty(editorComponentAlias)) {
      aliases.add(editorComponentAlias);
    }

    if (aliases.isEmpty()) {
      return Optional.empty();
    }

    return aliases
        .stream()
        .map(value -> value.split("="))
        .filter(arr -> arr[0].equals(pluginId))
        .map(arr -> arr[1])
        .findAny();
  }

  private void normalizeMemory(Container container, InternalMachineConfig machineConfig) {
    long ramLimit = Containers.getRamLimit(container);
    if (ramLimit == 0) {
      machineConfig.getAttributes().put(MEMORY_LIMIT_ATTRIBUTE, defaultSidecarMemoryLimitBytes);
    }
    // Use plugin_publisher/plugin_name to find overriding of memory limit.
    String overriddenSidecarMemLimit =
        wsAttributes.get(format(SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE, pluginPublisherAndName));
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
                pluginPublisherAndName,
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
