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

import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.shared.Constants;

/**
 * Provision cheEditor component in {@link Devfile} according to the value of {@link
 * Constants#WORKSPACE_TOOLING_EDITOR_ATTRIBUTE} in the specified {@link WorkspaceConfigImpl}.
 *
 * @author Sergii Leshchenko
 */
public class EditorComponentProvisioner implements ComponentProvisioner {

  /**
   * Converts workspace editor attribute to cheEditor component and injects it into the specified
   * {@link Devfile devfile}.
   *
   * @param devfile devfile to which create component should be injected
   * @param workspaceConfig workspace config that may contain environments to convert
   */
  @Override
  public void provision(Devfile devfile, WorkspaceConfigImpl workspaceConfig) {
    String editorAttribute =
        workspaceConfig.getAttributes().get(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE);
    if (editorAttribute == null) {
      return;
    }

    Component editorComponent =
        new Component()
            .withType(EDITOR_COMPONENT_TYPE)
            .withId(editorAttribute)
            .withName(
                workspaceConfig
                    .getAttributes()
                    .getOrDefault(EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE, editorAttribute));
    devfile.getComponents().add(editorComponent);
  }
}
