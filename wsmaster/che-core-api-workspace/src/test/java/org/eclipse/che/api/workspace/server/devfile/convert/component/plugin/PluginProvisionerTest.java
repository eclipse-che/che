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
package org.eclipse.che.api.workspace.server.devfile.convert.component.plugin;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class PluginProvisionerTest {

  private PluginProvisioner pluginComponentProvisioner;
  private PluginFQNParser fqnParser = new PluginFQNParser();

  @BeforeMethod
  public void setUp() {
    pluginComponentProvisioner = new PluginProvisioner(fqnParser);
  }

  @Test
  public void shouldProvisionChePluginComponent() throws WorkspaceExportException {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(
            WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE,
            "eclipse/super-plugin/0.0.1,https://localhost:8080#publisher1/custom-plugin/v1");
    workspaceConfig
        .getAttributes()
        .put(
            PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE,
            "eclipse/super-plugin/0.0.1=super-plugin,publisher1/custom-plugin/v1=custom");
    workspaceConfig
        .getAttributes()
        .put(format(SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE, "publisher1/custom-plugin"), "1024M");
    DevfileImpl devfile = new DevfileImpl();

    // when
    pluginComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 2);
    ComponentImpl superPluginComponent = devfile.getComponents().get(0);
    assertEquals(superPluginComponent.getAlias(), "super-plugin");
    assertEquals(superPluginComponent.getId(), "eclipse/super-plugin/0.0.1");
    assertEquals(superPluginComponent.getType(), PLUGIN_COMPONENT_TYPE);

    ComponentImpl customPluginComponent = devfile.getComponents().get(1);
    assertEquals(customPluginComponent.getAlias(), "custom");
    assertEquals(customPluginComponent.getId(), "publisher1/custom-plugin/v1");
    assertEquals(customPluginComponent.getType(), PLUGIN_COMPONENT_TYPE);
    assertEquals(customPluginComponent.getMemoryLimit(), "1024M");
  }

  @Test
  public void shouldSetAliasOnComponentIfAliasIsMissingInWorkspaceConfig()
      throws WorkspaceExportException {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, "eclipse/super-plugin/0.0.1");
    DevfileImpl devfile = new DevfileImpl();

    // when
    pluginComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl superPluginComponent = devfile.getComponents().get(0);
    assertNull(superPluginComponent.getAlias());
    assertEquals(superPluginComponent.getId(), "eclipse/super-plugin/0.0.1");
    assertEquals(superPluginComponent.getType(), PLUGIN_COMPONENT_TYPE);
  }
}
