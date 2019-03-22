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
package org.eclipse.che.api.devfile.server.convert.tool.editor;

import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.TOOL_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class EditorToolToWorkspaceApplierTest {

  private EditorToolToWorkspaceApplier editorToolApplier;

  @BeforeMethod
  public void setUp() {
    editorToolApplier = new EditorToolToWorkspaceApplier();
  }

  @Test
  public void shouldProvisionWorkspaceEditorAttributeDuringCheEditorToolApplying()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    Tool editorTool = new Tool();
    editorTool.setType(EDITOR_TOOL_TYPE);
    editorTool.setName("editor");
    editorTool.setId("org.eclipse.che.super-editor:0.0.1");

    // when
    editorToolApplier.apply(workspaceConfig, editorTool, null);

    // then
    assertEquals(
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE),
        "org.eclipse.che.super-editor:0.0.1");
    assertEquals(
        workspaceConfig.getAttributes().get(EDITOR_TOOL_ALIAS_WORKSPACE_ATTRIBUTE), "editor");
  }

  @Test
  public void shouldProvisionPluginCommandAttributesDuringCheEditorToolApplying() throws Exception {
    // given
    Tool superPluginTool = new Tool();
    superPluginTool.setName("editor");
    superPluginTool.setId("org.eclipse.che.super-editor:0.0.1");
    superPluginTool.setType(EDITOR_TOOL_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, "editor");
    workspaceConfig.getCommands().add(command);

    // when
    editorToolApplier.apply(workspaceConfig, superPluginTool, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "org.eclipse.che.super-editor:0.0.1");
  }

  @Test
  public void shouldProvisionPluginCommandAttributeWhenIdIsURLToCustomPluginRegistry()
      throws Exception {
    // given
    Tool superPluginTool = new Tool();
    superPluginTool.setName("editor");
    superPluginTool.setId(
        "https://custom-plugin.registry/plugins/org.eclipse.che.super-editor:0.0.1");
    superPluginTool.setType(EDITOR_TOOL_TYPE);

    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, "editor");
    workspaceConfig.getCommands().add(command);

    // when
    editorToolApplier.apply(workspaceConfig, superPluginTool, null);

    // then
    assertEquals(
        workspaceConfig.getCommands().get(0).getAttributes().get(PLUGIN_ATTRIBUTE),
        "org.eclipse.che.super-editor:0.0.1");
  }
}
