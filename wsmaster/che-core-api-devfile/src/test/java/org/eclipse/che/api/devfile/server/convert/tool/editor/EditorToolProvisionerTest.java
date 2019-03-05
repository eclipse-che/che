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

import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class EditorToolProvisionerTest {

  private EditorToolProvisioner editorToolProvisioner;

  @BeforeMethod
  public void setUp() {
    editorToolProvisioner = new EditorToolProvisioner();
  }

  @Test
  public void shouldProvisionCheEditorTool() throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "org.eclipse.che.super-editor:0.0.1");
    workspaceConfig.getAttributes().put(EDITOR_TOOL_ALIAS_WORKSPACE_ATTRIBUTE, "editor");
    Devfile devfile = new Devfile();

    // when
    editorToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool editorTool = devfile.getTools().get(0);
    assertEquals(editorTool.getName(), "editor");
    assertEquals(editorTool.getType(), EDITOR_TOOL_TYPE);
    assertEquals(editorTool.getId(), "org.eclipse.che.super-editor:0.0.1");
  }

  @Test
  public void shouldUseEditorIdAsToolNameIfAliasIsMissingDuringEditorToolProvisioning()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "org.eclipse.che.super-editor:0.0.1");
    Devfile devfile = new Devfile();

    // when
    editorToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool editorTool = devfile.getTools().get(0);
    assertEquals(editorTool.getName(), "org.eclipse.che.super-editor:0.0.1");
  }
}
