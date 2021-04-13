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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentFQNParser;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link DefaultEditorProvisioner}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultEditorProvisionerTest {

  @Mock private FileContentProvider fileContentProvider;

  private static final String EDITOR_NAME = "theia";
  private static final String EDITOR_VERSION = "1.0.0";
  private static final String EDITOR_PUBLISHER = "eclipse";
  private static final String EDITOR_REF =
      EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/" + EDITOR_VERSION;

  private static final String TERMINAL_PLUGIN_NAME = "theia-terminal";
  private static final String TERMINAL_PLUGIN_VERSION = "0.0.4";
  private static final String ASYNC_STORAGE_PLUGIN_REF = "eclipse/che-async-pv-plugin/nightly";
  private static final String TERMINAL_PLUGIN_REF =
      EDITOR_PUBLISHER + "/" + TERMINAL_PLUGIN_NAME + "/" + TERMINAL_PLUGIN_VERSION;

  private static final String COMMAND_PLUGIN_NAME = "theia-command";
  private static final String COMMAND_PLUGIN_REF =
      EDITOR_PUBLISHER + "/" + COMMAND_PLUGIN_NAME + "/v1.0.0";

  private DefaultEditorProvisioner provisioner;

  private PluginFQNParser pluginFQNParser = new PluginFQNParser();
  private ComponentFQNParser fqnParser = new ComponentFQNParser(pluginFQNParser);

  @Test
  public void shouldNotProvisionDefaultEditorIfItIsNotConfigured() throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            null, "", ASYNC_STORAGE_PLUGIN_REF, fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    assertTrue(devfile.getComponents().isEmpty());
  }

  @Test
  public void shouldProvisionDefaultEditorWithPluginsWhenDevfileDoNotHaveAny() throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF,
            TERMINAL_PLUGIN_REF + "," + COMMAND_PLUGIN_REF,
            "",
            fqnParser,
            pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, COMMAND_PLUGIN_REF)));
    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void shouldProvisionDefaultPluginsIfTheyAreNotSpecifiedAndDefaultEditorIsConfigured()
      throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl defaultEditorWithDifferentVersion =
        new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/latest");
    devfile.getComponents().add(defaultEditorWithDifferentVersion);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertTrue(components.contains(defaultEditorWithDifferentVersion));

    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void
      shouldProvisionDefaultPluginsIfTheyAreNotSpecifiedAndDefaultEditorFromCustomRegistryIsConfigured()
          throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl defaultEditorWithDifferentVersion =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE,
            "https://my-custom-registry#" + EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/latest");
    devfile.getComponents().add(defaultEditorWithDifferentVersion);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertTrue(components.contains(defaultEditorWithDifferentVersion));

    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
  }

  @Test
  public void shouldNotProvisionDefaultPluginsIfCustomEditorIsConfiguredWhichStartWithDefaultId()
      throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl editorWithNameSimilarToDefault =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + EDITOR_NAME + "-dev/dev-version");
    devfile.getComponents().add(editorWithNameSimilarToDefault);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(editorWithNameSimilarToDefault));
    assertNull(findById(components, EDITOR_NAME));
  }

  @Test
  public void shouldNotProvisionDefaultPluginsIfDevfileContainsEditorFreeAttribute()
      throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    devfile.getAttributes().put(EDITOR_FREE_DEVFILE_ATTRIBUTE, "true");

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertTrue(components.isEmpty());
  }

  @Test
  public void
      shouldProvisionDefaultPluginIfDevfileAlreadyContainPluginWithNameWhichStartWithDefaultOne()
          throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl pluginWithNameSimilarToDefault =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + TERMINAL_PLUGIN_NAME + "-dummy/latest");
    devfile.getComponents().add(pluginWithNameSimilarToDefault);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, TERMINAL_PLUGIN_REF)));
    assertTrue(components.contains(pluginWithNameSimilarToDefault));
  }

  @Test
  public void shouldNotProvisionDefaultEditorOrDefaultPluginsIfDevfileAlreadyHasNonDefaultEditor()
      throws Exception {
    // given
    provisioner = new DefaultEditorProvisioner(EDITOR_REF, "", "", fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl nonDefaultEditor =
        new ComponentImpl(EDITOR_COMPONENT_TYPE, "anypublisher/anyname/v" + EDITOR_VERSION);
    devfile.getComponents().add(nonDefaultEditor);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(nonDefaultEditor));
  }

  @Test
  public void shouldNotProvisionDefaultEditorIfDevfileAlreadyContainsSuchButWithDifferentVersion()
      throws Exception {
    // given
    provisioner = new DefaultEditorProvisioner(EDITOR_REF, "", "", fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTheiaEditor =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + EDITOR_NAME + "/my-custom");
    devfile.getComponents().add(myTheiaEditor);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(myTheiaEditor));
  }

  @Test
  public void shouldNotProvisionDefaultEditorIfDevfileAlreadyContainsSuchByReference()
      throws Exception {
    // given
    provisioner = new DefaultEditorProvisioner(EDITOR_REF, "", "", fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTheiaEditor =
        new ComponentImpl(
            EDITOR_COMPONENT_TYPE, null, "https://myregistry.com/abc/meta.yaml", null, null, null);
    String meta =
        "apiVersion: v2\n"
            + "publisher: "
            + EDITOR_PUBLISHER
            + "\n"
            + "name: "
            + EDITOR_NAME
            + "\n"
            + "version: "
            + EDITOR_VERSION
            + "\n"
            + "type: Che Editor";
    devfile.getComponents().add(myTheiaEditor);
    when(fileContentProvider.fetchContent(anyString())).thenReturn(meta);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 1);
    assertTrue(components.contains(myTheiaEditor));
  }

  @Test
  public void shouldNotProvisionDefaultPluginIfDevfileAlreadyContainsSuchButWithDifferentVersion()
      throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myTerminal =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + TERMINAL_PLUGIN_NAME + "/my-custom");
    devfile.getComponents().add(myTerminal);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(myTerminal));
  }

  @Test
  public void shouldNotProvisionDefaultPluginIfDevfileAlreadyContainsSuchByReference()
      throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, "", fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();
    String meta =
        "apiVersion: v2\n"
            + "publisher: "
            + EDITOR_PUBLISHER
            + "\n"
            + "name: "
            + TERMINAL_PLUGIN_NAME
            + "\n"
            + "version: "
            + TERMINAL_PLUGIN_VERSION
            + "\n"
            + "type: Che Plugin";
    ComponentImpl myTerminal =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, null, "https://myregistry.com/abc/meta.yaml", null, null, null);
    when(fileContentProvider.fetchContent(anyString())).thenReturn(meta);
    devfile.getComponents().add(myTerminal);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(myTerminal));
  }

  @Test
  public void shouldGenerateDefaultPluginNameIfIdIsNotUnique() throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, EDITOR_PUBLISHER + "/" + "my-plugin/v2.0", "", fqnParser, pluginFQNParser);
    DevfileImpl devfile = new DevfileImpl();
    ComponentImpl myPlugin =
        new ComponentImpl(
            PLUGIN_COMPONENT_TYPE, EDITOR_PUBLISHER + "/" + "my-custom-plugin/v0.0.3");
    devfile.getComponents().add(myPlugin);

    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(new ComponentImpl(EDITOR_COMPONENT_TYPE, EDITOR_REF)));
    assertTrue(components.contains(myPlugin));
    ComponentImpl defaultPlugin = findByRef(components, EDITOR_PUBLISHER + "/" + "my-plugin/v2.0");
    assertNotNull(defaultPlugin);
    assertNull(defaultPlugin.getAlias());
  }

  @Test
  public void shouldResolveDefaultReferencePlugins() throws Exception {
    // given
    String referencePluginRef = "https://remotepluginregistry.com/plugins/abc/meta.yaml";
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF,
            EDITOR_PUBLISHER + "/" + "my-plugin/v2.0" + "," + referencePluginRef,
            "",
            fqnParser,
            pluginFQNParser);
    String meta =
        "apiVersion: v2\n"
            + "publisher: "
            + EDITOR_PUBLISHER
            + "\n"
            + "name: "
            + COMMAND_PLUGIN_NAME
            + "\n"
            + "version: v1.0.0"
            + "\n"
            + "type: Che Plugin";
    DevfileImpl devfile = new DevfileImpl();
    when(fileContentProvider.fetchContent(anyString())).thenReturn(meta);
    ComponentImpl myPlugin = new ComponentImpl(PLUGIN_COMPONENT_TYPE, COMMAND_PLUGIN_REF);
    myPlugin.setReference(referencePluginRef);
    myPlugin.setId(COMMAND_PLUGIN_REF);
    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);
    assertTrue(components.contains(myPlugin));
  }

  @Test
  public void shouldProvisionAsyncStoragePluginsIfWorkspaceHasOnlyOneAttribute() throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, ASYNC_STORAGE_PLUGIN_REF, fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    devfile.setAttributes(ImmutableMap.of(Constants.ASYNC_PERSIST_ATTRIBUTE, "true"));
    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 2);

    assertFalse(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, ASYNC_STORAGE_PLUGIN_REF)));
  }

  @Test
  public void shouldProvisionAsyncStoragePluginsIfWorkspaceHasBothAttributes() throws Exception {
    // given
    provisioner =
        new DefaultEditorProvisioner(
            EDITOR_REF, TERMINAL_PLUGIN_REF, ASYNC_STORAGE_PLUGIN_REF, fqnParser, pluginFQNParser);

    DevfileImpl devfile = new DevfileImpl();
    devfile.setAttributes(
        ImmutableMap.of(
            Constants.ASYNC_PERSIST_ATTRIBUTE,
            "true",
            Constants.PERSIST_VOLUMES_ATTRIBUTE,
            "false"));
    // when
    provisioner.apply(devfile, fileContentProvider);

    // then
    List<ComponentImpl> components = devfile.getComponents();
    assertEquals(components.size(), 3);

    assertTrue(
        components.contains(new ComponentImpl(PLUGIN_COMPONENT_TYPE, ASYNC_STORAGE_PLUGIN_REF)));
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
