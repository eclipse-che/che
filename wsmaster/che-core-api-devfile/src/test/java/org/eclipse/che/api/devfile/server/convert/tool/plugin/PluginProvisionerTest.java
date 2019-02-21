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

import static org.eclipse.che.api.devfile.server.Constants.PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class PluginProvisionerTest {

  private PluginProvisioner pluginToolProvisioner;

  @BeforeMethod
  public void setUp() {
    pluginToolProvisioner = new PluginProvisioner();
  }

  @Test
  public void shouldProvisionChePluginTool() throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(
            WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE,
            "org.eclipse.che.super-plugin:0.0.1,custom-plugin:v1");
    workspaceConfig
        .getAttributes()
        .put(
            PLUGINS_TOOLS_ALIASES_WORKSPACE_ATTRIBUTE,
            "org.eclipse.che.super-plugin:0.0.1=super-plugin,custom-plugin:v1=custom");
    Devfile devfile = new Devfile();

    // when
    pluginToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 2);
    Tool superPluginTool = devfile.getTools().get(0);
    assertEquals(superPluginTool.getName(), "super-plugin");
    assertEquals(superPluginTool.getId(), "org.eclipse.che.super-plugin:0.0.1");
    assertEquals(superPluginTool.getType(), PLUGIN_TOOL_TYPE);

    Tool customPluginTool = devfile.getTools().get(1);
    assertEquals(customPluginTool.getName(), "custom");
    assertEquals(customPluginTool.getId(), "custom-plugin:v1");
    assertEquals(customPluginTool.getType(), PLUGIN_TOOL_TYPE);
  }

  @Test
  public void shouldUsePluginIdAsToolNameIfAliasIsMissingDuringProvisioningChePluginTool()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, "org.eclipse.che.super-plugin:0.0.1");
    Devfile devfile = new Devfile();

    // when
    pluginToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool superPluginTool = devfile.getTools().get(0);
    assertEquals(superPluginTool.getName(), "org.eclipse.che.super-plugin:0.0.1");
    assertEquals(superPluginTool.getId(), "org.eclipse.che.super-plugin:0.0.1");
    assertEquals(superPluginTool.getType(), PLUGIN_TOOL_TYPE);
  }
}
