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
package org.eclipse.che.api.devfile.server.convert.tool.plugin;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.TOOL_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;

import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Applies changes on workspace config according to the specified plugin tool.
 *
 * @author Sergii Leshchenko
 */
public class PluginToolToWorkspaceApplier implements ToolToWorkspaceApplier {

  /**
   * Applies changes on workspace config according to the specified plugin tool.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param pluginTool plugin tool that should be applied
   * @param contentProvider optional content provider that may be used for external tool resource
   *     fetching
   * @throws IllegalArgumentException if specified workspace config or plugin tool is null
   * @throws IllegalArgumentException if specified tool has type different from chePlugin
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      Tool pluginTool,
      @Nullable FileContentProvider contentProvider) {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(pluginTool != null, "Tool must not be null");
    checkArgument(
        PLUGIN_TOOL_TYPE.equals(pluginTool.getType()),
        format("Plugin must have `%s` type", PLUGIN_TOOL_TYPE));

    String workspacePluginsAttribute =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);
    workspaceConfig
        .getAttributes()
        .put(
            WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE,
            append(workspacePluginsAttribute, pluginTool.getId()));

    String pluginsAliases =
        workspaceConfig.getAttributes().get(PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE);
    workspaceConfig
        .getAttributes()
        .put(
            PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE,
            append(pluginsAliases, pluginTool.getId() + "=" + pluginTool.getName()));

    for (CommandImpl command : workspaceConfig.getCommands()) {
      String commandTool = command.getAttributes().get(TOOL_NAME_COMMAND_ATTRIBUTE);

      if (commandTool == null) {
        // command does not have tool information
        continue;
      }

      if (!commandTool.equals(pluginTool.getName())) {
        continue;
      }

      command.getAttributes().put(PLUGIN_ATTRIBUTE, pluginTool.getId());
    }
  }

  private String append(String source, String toAppend) {
    if (isNullOrEmpty(source)) {
      return toAppend;
    } else {
      return source + "," + toAppend;
    }
  }
}
