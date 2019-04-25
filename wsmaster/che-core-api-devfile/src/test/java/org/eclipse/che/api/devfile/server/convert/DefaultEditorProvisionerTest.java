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
package org.eclipse.che.api.devfile.server.convert;

import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_FREE_DEVFILE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.testng.annotations.Test;

/**
 * Tests {@link DefaultEditorProvisioner}.
 *
 * @author Sergii Leshchenko
 */
public class DefaultEditorProvisionerTest {

  private static final String DEFAULT_EDITOR_NAME = "theia";
  private static final String DEFAULT_EDITOR_VERSION = "1.0.0";
  private static final String DEFAULT_EDITOR_PUBLISHER = "eclipse";
  private static final String DEFAULT_EDITOR_REF =
      DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_EDITOR_NAME + "/" + DEFAULT_EDITOR_VERSION;

  private static final String DEFAULT_TERMINAL_PLUGIN_NAME = "theia-terminal";
  private static final String DEFAULT_TERMINAL_PLUGIN_REF =
      DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_TERMINAL_PLUGIN_NAME + "/0.0.4";

  private static final String DEFAULT_COMMAND_PLUGIN_NAME = "theia-command";
  private static final String DEFAULT_COMMAND_PLUGIN_REF =
      DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_COMMAND_PLUGIN_NAME + "/v1.0.0";

  private DefaultEditorProvisioner defaultEditorProvisioner;

  @Test
  public void shouldNotProvisionDefaultEditorIfItIsNotConfigured() {
    // given
    defaultEditorProvisioner = new DefaultEditorProvisioner(null, new String[] {});
    DevfileImpl devfile = new DevfileImpl();

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    assertTrue(devfile.getComponents().isEmpty());
  }

  @Test
  public void shouldProvisionDefaultEditorWithPluginsWhenDevfileDoNotHaveAny() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF,
            new String[] {DEFAULT_TERMINAL_PLUGIN_REF, DEFAULT_COMMAND_PLUGIN_REF});
    DevfileImpl devfile = new DevfileImpl();

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, DEFAULT_EDITOR_REF)));
    assertTrue(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, DEFAULT_COMMAND_PLUGIN_REF)));
    assertTrue(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, DEFAULT_TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void shouldProvisionDefaultPluginsIfTheyAreNotSpecifiedAndDefaultEditorIsConfigured() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_TERMINAL_PLUGIN_REF});

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl defaultEditorWithDifferentVersion =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE,
            DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_EDITOR_NAME + "/latest");
    devfile.getComponents().add(defaultEditorWithDifferentVersion);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertTrue(components.contains(defaultEditorWithDifferentVersion));

    assertTrue(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, DEFAULT_TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void
      shouldProvisionDefaultPluginsIfTheyAreNotSpecifiedAndDefaultEditorFromCustomRegistryIsConfigured() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_TERMINAL_PLUGIN_REF});

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl defaultEditorWithDifferentVersion =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE,
            "https://my-custom-registry/"
                + DEFAULT_EDITOR_PUBLISHER
                + "/"
                + DEFAULT_EDITOR_NAME
                + "/latest");
    devfile.getComponents().add(defaultEditorWithDifferentVersion);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertTrue(components.contains(defaultEditorWithDifferentVersion));

    assertTrue(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, DEFAULT_TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void shouldNotProvisionDefaultPluginsIfCustomEditorIsConfiguredWhichStartWithDefaultId() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_TERMINAL_PLUGIN_REF});

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl editorWithNameSimilarToDefault =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE,
            DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_EDITOR_NAME + "-dev/dev-version");
    devfile.getComponents().add(editorWithNameSimilarToDefault);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(editorWithNameSimilarToDefault));
    assertNull(findById(components, DEFAULT_EDITOR_NAME));
  }

  @Test
  public void shouldNotProvisionDefaultPluginsIfDevfileContainsEditorFreeAttribute() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_TERMINAL_PLUGIN_REF});

    DevfileImpl devfile = new DevfileImpl();
    devfile.getAttributes().put(EDITOR_FREE_DEVFILE_ATTRIBUTE, "true");

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertTrue(components.isEmpty());
  }

  @Test
  public void
      shouldProvisionDefaultPluginIfDevfileAlreadyContainPluginWithNameWhichStartWithDefaultOne() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_TERMINAL_PLUGIN_REF});

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl pluginWithNameSimilarToDefault =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE,
            DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_TERMINAL_PLUGIN_NAME + "-dummy/latest");
    devfile.getComponents().add(pluginWithNameSimilarToDefault);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, DEFAULT_EDITOR_REF)));
    assertTrue(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, DEFAULT_TERMINAL_PLUGIN_REF)));
    assertTrue(components.contains(pluginWithNameSimilarToDefault));
  }

  @Test
  public void shouldNotProvisionDefaultEditorOrDefaultPluginsIfDevfileAlreadyHasNonDefaultEditor() {
    // given
    defaultEditorProvisioner = new DefaultEditorProvisioner(DEFAULT_EDITOR_REF, new String[] {});
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl nonDefaultEditor =
        new ComponentImpl(EDITOR_COMPONENT_TYPE, "anypublisher/anyname/v" + DEFAULT_EDITOR_VERSION);
    devfile.getComponents().add(nonDefaultEditor);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(nonDefaultEditor));
  }

  @Test
  public void shouldNonProvisionDefaultEditorIfDevfileAlreadyContainsSuchButWithDifferentVersion() {
    // given
    defaultEditorProvisioner = new DefaultEditorProvisioner(DEFAULT_EDITOR_REF, new String[] {});
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTheiaEditor =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE,
            DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_EDITOR_NAME + "/my-custom");
    devfile.getComponents().add(myTheiaEditor);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(myTheiaEditor));
  }

  @Test
  public void shouldNotProvisionDefaultPluginIfDevfileAlreadyContainsSuchButWithDifferentVersion() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_TERMINAL_PLUGIN_REF});
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTerminal =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE,
            DEFAULT_EDITOR_PUBLISHER + "/" + DEFAULT_TERMINAL_PLUGIN_NAME + "/my-custom");
    devfile.getComponents().add(myTerminal);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, DEFAULT_EDITOR_REF)));
    assertTrue(components.contains(myTerminal));
  }

  @Test
  public void shouldGenerateDefaultPluginNameIfIdIsNotUnique() {
    // given
    defaultEditorProvisioner =
        new DefaultEditorProvisioner(
            DEFAULT_EDITOR_REF, new String[] {DEFAULT_EDITOR_PUBLISHER + "/" + "my-plugin/v2.0"});
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myPlugin =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, DEFAULT_EDITOR_PUBLISHER + "/" + "my-custom-plugin/v0.0.3");
    devfile.getComponents().add(myPlugin);

    // when
    defaultEditorProvisioner.apply(devfile);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, DEFAULT_EDITOR_REF)));
    assertTrue(components.contains(myPlugin));
    ComponentImpl defaultPlugin =
        findByRef(components, DEFAULT_EDITOR_PUBLISHER + "/" + "my-plugin/v2.0");
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
