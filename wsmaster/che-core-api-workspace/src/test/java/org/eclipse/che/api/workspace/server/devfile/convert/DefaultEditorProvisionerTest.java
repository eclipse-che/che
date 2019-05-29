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
package org.eclipse.che.api.workspace.server.devfile.convert;

import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_FREE_DEVFILE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.testng.annotations.Test;

/**
 * Tests {@link DefaultEditorProvisioner}.
 *
 * @author Sergii Leshchenko
 */
public class DefaultEditorProvisionerTest {

  private static final String EDITOR_NAME = "theia";
  private static final String EDITOR_VERSION = "1.0.0";
  private static final String EDITOR_PUBLISHER = "eclipse";
  private static final String EDITOR_REF =
      EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/" + EDITOR_VERSION;

  private static final String TERMINAL_PLUGIN_NAME = "theia-terminal";
  private static final String TERMINAL_PLUGIN_REF =
      EDITOR_PUBLISHER + "/" + TERMINAL_PLUGIN_NAME + "/0.0.4";

  private static final String COMMAND_PLUGIN_NAME = "theia-command";
  private static final String COMMAND_PLUGIN_REF =
      EDITOR_PUBLISHER + "/" + COMMAND_PLUGIN_NAME + "/v1.0.0";

  private DefaultEditorProvisioner provisioner;

  private PluginFQNParser fqnParser = new PluginFQNParser();

  @Test
  public void shouldNotProvisionDefaultEditorIfItIsNotConfigured() throws DevfileException {
    // given
    provisioner = new DefaultEditorProvisioner(null, new String[] {}, fqnParser);
    DevfileImpl devfile = new DevfileImpl();

    // when
    provisioner.apply(devfile);

    // then
    assertTrue(devfile.getComponents().isEmpty());
  }

  @Test
  public void shouldProvisionDefaultEditorWithPluginsWhenDevfileDoNotHaveAny()
      throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF, COMMAND_PLUGIN_REF}, fqnParser);
    DevfileImpl devfile = new DevfileImpl();

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, COMMAND_PLUGIN_REF)));
    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void shouldProvisionDefaultPluginsIfTheyAreNotSpecifiedAndDefaultEditorIsConfigured()
      throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF}, fqnParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl defaultEditorWithDifferentVersion =
        new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/latest");
    devfile.getComponents().add(defaultEditorWithDifferentVersion);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertTrue(components.contains(defaultEditorWithDifferentVersion));

    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void
      shouldProvisionDefaultPluginsIfTheyAreNotSpecifiedAndDefaultEditorFromCustomRegistryIsConfigured()
          throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF}, fqnParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl defaultEditorWithDifferentVersion =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE,
            "https://my-custom-registry/" + EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/latest");
    devfile.getComponents().add(defaultEditorWithDifferentVersion);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertTrue(components.contains(defaultEditorWithDifferentVersion));

    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void shouldNotProvisionDefaultPluginsIfCustomEditorIsConfiguredWhichStartWithDefaultId()
      throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF}, fqnParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl editorWithNameSimilarToDefault =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + EDITOR_NAME + "-dev/dev-version");
    devfile.getComponents().add(editorWithNameSimilarToDefault);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(editorWithNameSimilarToDefault));
    assertNull(findById(components, EDITOR_NAME));
  }

  @Test
  public void shouldNotProvisionDefaultPluginsIfDevfileContainsEditorFreeAttribute()
      throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF}, fqnParser);

    DevfileImpl devfile = new DevfileImpl();
    devfile.getAttributes().put(EDITOR_FREE_DEVFILE_ATTRIBUTE, "true");

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertTrue(components.isEmpty());
  }

  @Test
  public void
      shouldProvisionDefaultPluginIfDevfileAlreadyContainPluginWithNameWhichStartWithDefaultOne()
          throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF}, fqnParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl pluginWithNameSimilarToDefault =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + TERMINAL_PLUGIN_NAME + "-dummy/latest");
    devfile.getComponents().add(pluginWithNameSimilarToDefault);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
    assertTrue(components.contains(pluginWithNameSimilarToDefault));
  }

  @Test
  public void shouldNotProvisionDefaultEditorOrDefaultPluginsIfDevfileAlreadyHasNonDefaultEditor()
      throws DevfileException {
    // given
    provisioner = new DefaultEditorProvisioner(EDITOR_REF, new String[] {}, fqnParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl nonDefaultEditor =
        new ComponentImpl(EDITOR_COMPONENT_TYPE, "anypublisher/anyname/v" + EDITOR_VERSION);
    devfile.getComponents().add(nonDefaultEditor);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(nonDefaultEditor));
  }

  @Test
  public void shouldNonProvisionDefaultEditorIfDevfileAlreadyContainsSuchButWithDifferentVersion()
      throws DevfileException {
    // given
    provisioner = new DefaultEditorProvisioner(EDITOR_REF, new String[] {}, fqnParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTheiaEditor =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/my-custom");
    devfile.getComponents().add(myTheiaEditor);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(myTheiaEditor));
  }

  @Test
  public void shouldNotProvisionDefaultPluginIfDevfileAlreadyContainsSuchButWithDifferentVersion()
      throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(EDITOR_REF, new String[] {TERMINAL_PLUGIN_REF}, fqnParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTerminal =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + TERMINAL_PLUGIN_NAME + "/my-custom");
    devfile.getComponents().add(myTerminal);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(myTerminal));
  }

  @Test
  public void shouldGenerateDefaultPluginNameIfIdIsNotUnique() throws DevfileException {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, new String[] {EDITOR_PUBLISHER + "/" + "my-plugin/v2.0"}, fqnParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myPlugin =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + "my-custom-plugin/v0.0.3");
    devfile.getComponents().add(myPlugin);

    // when
    provisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(myPlugin));
    ComponentImpl defaultPlugin = findByRef(components, EDITOR_PUBLISHER + "/" + "my-plugin/v2.0");
    assertNotNull(defaultPlugin);
    assertNull(defaultPlugin.getAlias());
  }

  private ComponentImpl findById(List<ComponentImpl> components, String id) {
    return components
        .stream()
        .filter(c -> c.getId() != null && c.getId().startsWith(id + ':'))
        .findAny()
        .orElse(null);
  }

  private ComponentImpl findByRef(List<ComponentImpl> components, String ref) {
    return components.stream().filter(c -> ref.equals(c.getId())).findAny().orElse(null);
  }
}
