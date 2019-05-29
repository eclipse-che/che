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
package org.eclipse.che.api.workspace.server.devfile.convert.component.editor;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class EditorComponentProvisionerTest {

  private EditorComponentProvisioner editorComponentProvisioner;
  private PluginFQNParser fqnParser = new PluginFQNParser();

  @BeforeMethod
  public void setUp() {
    editorComponentProvisioner = new EditorComponentProvisioner(fqnParser);
  }

  @Test
  public void shouldProvisionCheEditorComponent() throws WorkspaceExportException {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    String editorId = "eclipse/super-editor/0.0.1";
    workspaceConfig.getAttributes().put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, editorId);
    workspaceConfig
        .getAttributes()
        .put(format(SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE, "eclipse/super-editor"), "245G");
    workspaceConfig.getAttributes().put(EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE, "editor");
    DevfileImpl devfile = new DevfileImpl();

    // when
    editorComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl editorComponent = devfile.getComponents().get(0);
    assertEquals(editorComponent.getAlias(), "editor");
    assertEquals(editorComponent.getType(), EDITOR_COMPONENT_TYPE);
    assertEquals(editorComponent.getId(), editorId);
    assertEquals(editorComponent.getMemoryLimit(), "245G");
  }

  @Test
  public void shouldUseEditorIdAsComponentNameIfAliasIsMissingDuringEditorComponentProvisioning()
      throws WorkspaceExportException {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "eclipse/super-editor/0.0.1");
    DevfileImpl devfile = new DevfileImpl();

    // when
    editorComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl editorComponent = devfile.getComponents().get(0);
    assertEquals(editorComponent.getAlias(), "eclipse/super-editor/0.0.1");
  }
}
