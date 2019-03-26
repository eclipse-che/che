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

import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.TOOL_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class PluginToolToWorkspaceApplierTest {

  private PluginToolToWorkspaceApplier pluginToolApplier;

  @BeforeMethod
  public void setUp() {
    pluginToolApplier = new PluginToolToWorkspaceApplier();
  }

  @Test
  public void shouldProvisionPluginWorkspaceAttributeDuringCheEditorToolApplying()
      throws Exception {
    // given
    Tool superPluginTool = new Tool();
    superPluginTool.setName("super-plugin");
    superPluginTool.setId("org.eclipse.che.super-plugin:0.0.1");
    superPluginTool.setType(PLUGIN_TOOL_TYPE);

    Tool customPluginTool = new Tool();
    customPluginTool.setName("custom");
    customPluginTool.setId("custom-plugin:v1");
    customPluginTool.setType(PLUGIN_TOOL_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();

    // when
    pluginToolApplier.apply(workspaceConfig, superPluginTool, null);
    pluginToolApplier.apply(workspaceConfig, customPluginTool, null);

    // then
    String workspaceTooling =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);
    assertTrue(workspaceTooling.matches("(.+:.+),(.+:.+)"));
    assertTrue(workspaceTooling.contains("org.eclipse.che.super-plugin:0.0.1"));
    assertTrue(workspaceTooling.contains("custom-plugin:v1"));
    String toollingAliases =
        workspaceConfig.getAttributes().get(PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE);
    assertTrue(toollingAliases.matches("(.+:.+=.+),(.+:.+=.+)"));
    assertTrue(toollingAliases.contains("org.eclipse.che.super-plugin:0.0.1=super-plugin"));
    assertTrue(toollingAliases.contains("custom-plugin:v1=custom"));
  }

  @Test
  public void shouldProvisionPluginCommandAttributesDuringCheEditorToolApplying() throws Exception {
    // given
    Tool superPluginTool = new Tool();
    superPluginTool.setName("super-plugin");
    superPluginTool.setId("org.eclipse.che.super-plugin:0.0.1");
    superPluginTool.setType(PLUGIN_TOOL_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, "super-plugin");
    workspaceConfig.getCommands().add(command);

    // when
    pluginToolApplier.apply(workspaceConfig, superPluginTool, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "org.eclipse.che.super-plugin:0.0.1");
  }

  @Test
  public void shouldProvisionPluginCommandAttributeWhenIdIsURLToCustomPluginRegistry()
      throws Exception {
    // given
    Tool superPluginTool = new Tool();
    superPluginTool.setName("super-plugin");
    superPluginTool.setId(
        "https://custom-plugin.registry/plugins/org.eclipse.che.super-plugin:0.0.1");
    superPluginTool.setType(PLUGIN_TOOL_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, "super-plugin");
    workspaceConfig.getCommands().add(command);

    // when
    pluginToolApplier.apply(workspaceConfig, superPluginTool, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "org.eclipse.che.super-plugin:0.0.1");
  }
}
