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

import static org.eclipse.che.api.devfile.server.Constants.PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class PluginProvisionerTest {

  private PluginProvisioner pluginComponentProvisioner;

  @BeforeMethod
  public void setUp() {
    pluginComponentProvisioner = new PluginProvisioner();
  }

  @Test
  public void shouldProvisionChePluginComponent() throws Exception {
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
            PLUGINS_COMPONENTS_ALIASES_WORKSPACE_ATTRIBUTE,
            "org.eclipse.che.super-plugin:0.0.1=super-plugin,custom-plugin:v1=custom");
    DevfileImpl devfile = new DevfileImpl();

    // when
    pluginComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 2);
    ComponentImpl superPluginComponent = devfile.getComponents().get(0);
    assertEquals(superPluginComponent.getAlias(), "super-plugin");
    assertEquals(superPluginComponent.getId(), "org.eclipse.che.super-plugin:0.0.1");
    assertEquals(superPluginComponent.getType(), PLUGIN_COMPONENT_TYPE);

    ComponentImpl customPluginComponent = devfile.getComponents().get(1);
    assertEquals(customPluginComponent.getAlias(), "custom");
    assertEquals(customPluginComponent.getId(), "custom-plugin:v1");
    assertEquals(customPluginComponent.getType(), PLUGIN_COMPONENT_TYPE);
  }

  @Test
  public void shouldSetAliasOnComponentIfAliasIsMissingInWorkspaceConfig() throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, "org.eclipse.che.super-plugin:0.0.1");
    DevfileImpl devfile = new DevfileImpl();

    // when
    pluginComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl superPluginComponent = devfile.getComponents().get(0);
    assertNull(superPluginComponent.getAlias());
    assertEquals(superPluginComponent.getId(), "org.eclipse.che.super-plugin:0.0.1");
    assertEquals(superPluginComponent.getType(), PLUGIN_COMPONENT_TYPE);
  }
}
