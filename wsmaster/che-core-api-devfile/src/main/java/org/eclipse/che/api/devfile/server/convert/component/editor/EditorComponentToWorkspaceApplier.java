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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;

import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/**
 * Applies changes on workspace config according to the specified editor component.
 *
 * @author Sergii Leshchenko
 */
public class EditorComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  /**
   * Applies changes on workspace config according to the specified editor component.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param editorComponent plugin component that should be applied
   * @param contentProvider optional content provider that may be used for external component
   *     resource fetching
   * @throws IllegalArgumentException if specified workspace config or plugin component is null
   * @throws IllegalArgumentException if specified component has type different from cheEditor
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      Component editorComponent,
      FileContentProvider contentProvider) {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(editorComponent != null, "Component must not be null");
    checkArgument(
        EDITOR_COMPONENT_TYPE.equals(editorComponent.getType()),
        format("Plugin must have `%s` type", EDITOR_COMPONENT_TYPE));

    String editorComponentAlias = editorComponent.getAlias();
    String editorId = editorComponent.getId();

    workspaceConfig.getAttributes().put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, editorId);

    if (editorComponentAlias != null) {
      workspaceConfig
          .getAttributes()
          .put(EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE, editorComponentAlias);
    }

    String editorIdVersion = resolveIdAndVersion(editorComponent.getId());
    workspaceConfig
        .getCommands()
        .stream()
        .filter(
            c ->
                c.getAttributes()
                    .get(COMPONENT_ALIAS_COMMAND_ATTRIBUTE)
                    .equals(editorComponentAlias))
        .forEach(c -> c.getAttributes().put(PLUGIN_ATTRIBUTE, editorIdVersion));
  }

  private String resolveIdAndVersion(String ref) {
    int lastSlashPosition = ref.lastIndexOf("/");
    if (lastSlashPosition < 0) {
      return ref;
    } else {
      return ref.substring(lastSlashPosition + 1);
    }
  }
}
