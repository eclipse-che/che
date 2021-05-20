/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile.convert.component.plugin;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;

import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentFQNParser;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Applies changes on workspace config according to the specified plugin component.
 *
 * @author Sergii Leshchenko
 */
public class PluginComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  private final ComponentFQNParser componentFQNParser;

  @Inject
  public PluginComponentToWorkspaceApplier(ComponentFQNParser componentFQNParser) {
    this.componentFQNParser = componentFQNParser;
  }

  /**
   * Applies changes on workspace config according to the specified plugin component.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param pluginComponent plugin component that should be applied
   * @param contentProvider optional content provider that may be used for external component
   *     resource fetching
   * @throws IllegalArgumentException if specified workspace config or plugin component is null
   * @throws IllegalArgumentException if specified component has type different from chePlugin
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      ComponentImpl pluginComponent,
      @Nullable FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(pluginComponent != null, "Component must not be null");
    checkArgument(
        PLUGIN_COMPONENT_TYPE.equals(pluginComponent.getType()),
        format("Plugin must have `%s` type", PLUGIN_COMPONENT_TYPE));

    String workspacePluginsAttribute =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);

    final String pluginId = pluginComponent.getId();
    final String registryUrl = pluginComponent.getRegistryUrl();

    final ExtendedPluginFQN fqn = componentFQNParser.evaluateFQN(pluginComponent, contentProvider);
    if (!isNullOrEmpty(fqn.getReference())) {
      workspaceConfig
          .getAttributes()
          .put(
              WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE,
              append(workspacePluginsAttribute, fqn.getReference()));
    } else {
      workspaceConfig
          .getAttributes()
          .put(
              WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE,
              append(
                  workspacePluginsAttribute,
                  componentFQNParser.getCompositeId(registryUrl, pluginId)));
    }

    for (CommandImpl command : workspaceConfig.getCommands()) {
      String commandComponent = command.getAttributes().get(COMPONENT_ALIAS_COMMAND_ATTRIBUTE);

      if (commandComponent == null) {
        // command does not have component information
        continue;
      }

      if (!commandComponent.equals(pluginComponent.getAlias())) {
        continue;
      }

      command.getAttributes().put(PLUGIN_ATTRIBUTE, fqn.getId());
    }

    // make sure id is set to be able to match component with plugin broker result
    // when referenceContent is used
    pluginComponent.setId(fqn.getId());
  }

  private String append(String source, String toAppend) {
    if (isNullOrEmpty(source)) {
      return toAppend;
    } else {
      return source + "," + toAppend;
    }
  }
}
