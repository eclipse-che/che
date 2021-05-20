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
package org.eclipse.che.api.workspace.server.devfile.convert.component.editor;

import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
public class EditorComponentToWorkspaceApplierTest {

  @Mock private FileContentProvider fileContentProvider;

  private EditorComponentToWorkspaceApplier editorComponentApplier;
  private PluginFQNParser fqnParser = new PluginFQNParser();

  @BeforeMethod
  public void setUp() {
    editorComponentApplier =
        new EditorComponentToWorkspaceApplier(new ComponentFQNParser(fqnParser));
  }

  @Test
  public void shouldProvisionWorkspaceEditorAttributeDuringCheEditorComponentApplying()
      throws Exception {
    String editorId = "eclipse/super-editor/0.0.1";
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    ComponentImpl editorComponent = new ComponentImpl();
    editorComponent.setType(EDITOR_COMPONENT_TYPE);
    editorComponent.setAlias("editor");
    editorComponent.setId(editorId);
    editorComponent.setMemoryLimit("12345M");

    // when
    editorComponentApplier.apply(workspaceConfig, editorComponent, null);

    // then
    assertEquals(workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE), editorId);
  }

  @Test
  public void
      shouldProvisionWorkspaceEditorAttributeWithCustomRegistryDuringCheEditorComponentApplying()
          throws Exception {
    String editorId = "eclipse/super-editor/0.0.1";
    String registryUrl = "https://myregistry.com/infolder/";
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    ComponentImpl editorComponent = new ComponentImpl();
    editorComponent.setType(EDITOR_COMPONENT_TYPE);
    editorComponent.setAlias("editor1");
    editorComponent.setId(editorId);
    editorComponent.setRegistryUrl(registryUrl);
    editorComponent.setMemoryLimit("12345M");

    // when
    editorComponentApplier.apply(workspaceConfig, editorComponent, null);

    // then
    assertEquals(
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE),
        registryUrl + "#" + editorId);
  }

  @Test
  public void shouldProvisionWorkspaceEditorAttributeWithReferenceDuringCheEditorComponentApplying()
      throws Exception {
    String reference = "https://myregistry.com/infolder/meta.yaml";
    String meta =
        "apiVersion: v2\n"
            + "publisher: eclipse\n"
            + "name: super-editor\n"
            + "version: 0.0.1\n"
            + "type: Che Editor";
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    ComponentImpl editorComponent = new ComponentImpl();
    editorComponent.setType(EDITOR_COMPONENT_TYPE);
    editorComponent.setAlias("editor1");
    editorComponent.setReference(reference);
    editorComponent.setMemoryLimit("12345M");
    when(fileContentProvider.fetchContent(anyString())).thenReturn(meta);

    // when
    editorComponentApplier.apply(workspaceConfig, editorComponent, fileContentProvider);

    // then
    assertEquals(
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE), reference);
    assertEquals(editorComponent.getId(), "eclipse/super-editor/0.0.1");
  }

  @Test
  public void shouldProvisionPluginCommandAttributesDuringCheEditorComponentApplying()
      throws Exception {
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("editor");
    superPluginComponent.setId("eclipse/super-editor/0.0.1");
    superPluginComponent.setType(EDITOR_COMPONENT_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, "editor");
    workspaceConfig.getCommands().add(command);

    // when
    editorComponentApplier.apply(workspaceConfig, superPluginComponent, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "eclipse/super-editor/0.0.1");
  }

  @Test
  public void shouldProvisionPluginCommandAttributeWhenIdIsURLToCustomPluginRegistry()
      throws Exception {
    // given
    ComponentImpl superPluginComponent = new ComponentImpl();
    superPluginComponent.setAlias("editor");
    superPluginComponent.setId("https://custom-plugin.registry/plugins#eclipse/super-editor/0.0.1");
    superPluginComponent.setType(EDITOR_COMPONENT_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, "editor");
    workspaceConfig.getCommands().add(command);

    // when
    editorComponentApplier.apply(workspaceConfig, superPluginComponent, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "eclipse/super-editor/0.0.1");
  }
}
