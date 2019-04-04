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
package org.eclipse.che.api.devfile.server.convert.component.editor;

import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class EditorComponentProvisionerTest {

  private EditorComponentProvisioner editorComponentProvisioner;

  @BeforeMethod
  public void setUp() {
    editorComponentProvisioner = new EditorComponentProvisioner();
  }

  @Test
  public void shouldProvisionCheEditorComponent() throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "org.eclipse.che.super-editor:0.0.1");
    workspaceConfig.getAttributes().put(EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE, "editor");
    DevfileImpl devfile = new DevfileImpl();

    // when
    editorComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl editorComponent = devfile.getComponents().get(0);
    assertEquals(editorComponent.getName(), "editor");
    assertEquals(editorComponent.getType(), EDITOR_COMPONENT_TYPE);
    assertEquals(editorComponent.getId(), "org.eclipse.che.super-editor:0.0.1");
  }

  @Test
  public void shouldUseEditorIdAsComponentNameIfAliasIsMissingDuringEditorComponentProvisioning()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig
        .getAttributes()
        .put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "org.eclipse.che.super-editor:0.0.1");
    DevfileImpl devfile = new DevfileImpl();

    // when
    editorComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl editorComponent = devfile.getComponents().get(0);
    assertEquals(editorComponent.getName(), "org.eclipse.che.super-editor:0.0.1");
  }
}
