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

import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentFQNParser;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class PluginComponentToWorkspaceApplierTest {

  @Mock private FileContentProvider fileContentProvider;

  private PluginComponentToWorkspaceApplier pluginComponentApplier;
  private PluginFQNParser fqnParser = new PluginFQNParser();

  @BeforeMethod
  public void setUp() {
    pluginComponentApplier =
        new PluginComponentToWorkspaceApplier(new ComponentFQNParser(fqnParser));
  }

  @Test
  public void shouldProvisionPluginWorkspaceAttributeDuringChePluginComponentApplying()
      throws Exception {

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
  }

  @Test
  public void
      shouldProvisionPluginWorkspaceAttributeWithCustomRegistryDuringChePluginComponentApplying()
          throws Exception {

    String superPluginId = "eclipse/super-plugin/0.0.1";
    String registryUrl = "https://myregistry.com/infolder/";
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("super-plugin");
    superPluginComponent.setId(superPluginId);
    superPluginComponent.setType(PLUGIN_COMPONENT_TYPE);
    superPluginComponent.setMemoryLimit("1234M");

    ComponentImpl customPluginComponent = new ComponentImpl();
    customPluginComponent.setAlias("custom");
    customPluginComponent.setId("publisher1/custom-plugin/v1");
    customPluginComponent.setRegistryUrl(registryUrl);
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
    assertTrue(workspaceTooling.contains(registryUrl + "#" + "publisher1/custom-plugin/v1"));
  }

  @Test
  public void shouldProvisionPluginWorkspaceAttributeWithReferenceDuringChePluginComponentApplying()
      throws Exception {

    String superPluginId = "eclipse/super-plugin/0.0.1";
    String reference = "https://myregistry.com/infolder/meta.yaml";
    String meta =
        "apiVersion: v2\n"
            + "publisher: eclipse\n"
            + "name: super-plugin\n"
            + "version: 0.0.1\n"
            + "type: Che Plugin";
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("super-plugin");
    superPluginComponent.setId(superPluginId);
    superPluginComponent.setType(PLUGIN_COMPONENT_TYPE);
    superPluginComponent.setMemoryLimit("1234M");

    when(fileContentProvider.fetchContent(anyString())).thenReturn(meta);

    ComponentImpl customPluginComponent = new ComponentImpl();
    customPluginComponent.setAlias("custom");
    customPluginComponent.setType(PLUGIN_COMPONENT_TYPE);
    customPluginComponent.setReference(reference);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();

    // when
    pluginComponentApplier.apply(workspaceConfig, superPluginComponent, fileContentProvider);
    pluginComponentApplier.apply(workspaceConfig, customPluginComponent, fileContentProvider);

    // then
    String workspaceTooling =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);
    assertTrue(workspaceTooling.matches("(.+/.+/.+),(https://.+/.+/.+)"));
    assertTrue(workspaceTooling.contains(superPluginId));
    assertTrue(workspaceTooling.contains(reference));
    assertEquals(customPluginComponent.getId(), "eclipse/super-plugin/0.0.1");
  }

  @Test
  public void shouldProvisionPluginCommandAttributesDuringChePluginComponentApplying()
      throws Exception {
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
      throws Exception {
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("super-plugin");
    superPluginComponent.setId(
        "https://custom-plugin.registry/plugins/#eclipse/super-plugin/0.0.1");
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
