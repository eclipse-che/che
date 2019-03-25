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
package org.eclipse.che.api.devfile.server.convert.component.plugin;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.devfile.server.Constants.PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.shared.Constants;

/**
 * Provision chePlugin components in {@link Devfile} according to the value of {@link
 * Constants#WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE} in the specified {@link WorkspaceConfigImpl}.
 *
 * @author Sergii Leshchenko
 */
public class PluginProvisioner implements ComponentProvisioner {

  /**
   * Provision chePlugin components in {@link Devfile} according to the value of {@link
   * Constants#WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE} in the specified {@link WorkspaceConfigImpl}.
   *
   * @param devfile devfile that should be provisioned with chePlugin components
   * @param workspaceConfig workspace config that may contain configured plugins
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   */
  @Override
  public void provision(Devfile devfile, WorkspaceConfigImpl workspaceConfig) {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(devfile != null, "Workspace config must not be null");

    String pluginsAttribute =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);
    if (isNullOrEmpty(pluginsAttribute)) {
      return;
    }

    Map<String, String> pluginIdToComponentName = extractPluginIdToComponentName(workspaceConfig);

    for (String pluginId : pluginsAttribute.split(",")) {
      Component pluginComponent =
          new Component()
              .withId(pluginId)
              .withType(PLUGIN_COMPONENT_TYPE)
              .withName(pluginIdToComponentName.getOrDefault(pluginId, pluginId));
      devfile.getComponents().add(pluginComponent);
    }
  }

  private Map<String, String> extractPluginIdToComponentName(WorkspaceConfigImpl wsConfig) {
    String aliasesAttribute =
        wsConfig.getAttributes().get(PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE);
    if (isNullOrEmpty(aliasesAttribute)) {
      return new HashMap<>();
    }
    return Arrays.stream(aliasesAttribute.split(","))
        .map(s -> s.split("=", 2))
        .collect(toMap(arr -> arr[0], arr -> arr[1]));
  }
}
