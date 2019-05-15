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

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PLUGIN_PREFERENCE_ATTR_TEMPLATE;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class PluginComponentToWorkspaceApplierTest {

  private PluginComponentToWorkspaceApplier pluginComponentApplier;
  private PluginFQNParser fqnParser = new PluginFQNParser();

  @BeforeMethod
  public void setUp() {
    pluginComponentApplier = new PluginComponentToWorkspaceApplier(fqnParser);
  }

  @Test
  public void shouldProvisionPluginWorkspaceAttributeDuringChePluginComponentApplying()
      throws DevfileException {

    String superPluginId = "eclipse/super-plugin/0.0.1";
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("super-plugin");
    superPluginComponent.setId(superPluginId);
    superPluginComponent.setType(PLUGIN_COMPONENT_TYPE);
    superPluginComponent.setMemoryLimit("1234M");
    superPluginComponent.getPreferences().put("java-home", "/home/user/jdk11");

    ComponentImpl customPluginComponent = new ComponentImpl();
    customPluginComponent.setAlias("custom");
    customPluginComponent.setId("publisher1/custom-plugin/v1");
    customPluginComponent.setType(PLUGIN_COMPONENT_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();

    // when
    pluginComponentApplier.apply(workspaceConfig, superPluginComponent, null);
    pluginComponentApplier.apply(workspaceConfig, customPluginComponent, null);

    // then
    String workspaceTooling =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);
    assertTrue(workspaceTooling.matches("(.+/.+/.+),(.+/.+/.+)"));
    assertTrue(workspaceTooling.contains(superPluginId));
    assertTrue(workspaceTooling.contains("publisher1/custom-plugin/v1"));
    String toolingAliases =
        workspaceConfig.getAttributes().get(PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE);
    assertTrue(toolingAliases.matches("(.+/.+/.+=.+),(.+/.+/.+=.+)"));
    assertTrue(toolingAliases.contains(superPluginId + "=super-plugin"));
    assertTrue(toolingAliases.contains("publisher1/custom-plugin/v1=custom"));
    assertEquals(
        workspaceConfig
            .getAttributes()
            .get(format(SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE, "eclipse/super-plugin")),
        "1234M");

    assertEquals(
        workspaceConfig
            .getAttributes()
            .get(format(PLUGIN_PREFERENCE_ATTR_TEMPLATE, "eclipse/super-plugin", "java-home")),
        "/home/user/jdk11");
  }

  @Test
  public void shouldProvisionPluginCommandAttributesDuringChePluginComponentApplying()
      throws DevfileException {
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("super-plugin");
    superPluginComponent.setId("eclipse/super-plugin/0.0.1");
    superPluginComponent.setType(PLUGIN_COMPONENT_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, "super-plugin");
    workspaceConfig.getCommands().add(command);

    // when
    pluginComponentApplier.apply(workspaceConfig, superPluginComponent, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "eclipse/super-plugin/0.0.1");
  }

  @Test
  public void shouldProvisionPluginCommandAttributeWhenIdIsURLToCustomPluginRegistry()
      throws DevfileException {
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("super-plugin");
    superPluginComponent.setId("https://custom-plugin.registry/plugins/eclipse/super-plugin/0.0.1");
    superPluginComponent.setType(PLUGIN_COMPONENT_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, "super-plugin");
    workspaceConfig.getCommands().add(command);

    // when
    pluginComponentApplier.apply(workspaceConfig, superPluginComponent, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "eclipse/super-plugin/0.0.1");
  }
}
