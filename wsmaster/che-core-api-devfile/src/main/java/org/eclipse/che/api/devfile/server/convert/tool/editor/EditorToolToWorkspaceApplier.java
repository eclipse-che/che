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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.TOOL_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;

import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/**
 * Applies changes on workspace config according to the specified editor tool.
 *
 * @author Sergii Leshchenko
 */
public class EditorToolToWorkspaceApplier implements ToolToWorkspaceApplier {

  /**
   * Applies changes on workspace config according to the specified editor tool.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param editorTool plugin tool that should be applied
   * @param contentProvider optional content provider that may be used for external tool resource
   *     fetching
   * @throws IllegalArgumentException if specified workspace config or plugin tool is null
   * @throws IllegalArgumentException if specified tool has type different from cheEditor
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig, Tool editorTool, FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(editorTool != null, "Tool must not be null");
    checkArgument(
        EDITOR_TOOL_TYPE.equals(editorTool.getType()),
        format("Plugin must have `%s` type", EDITOR_TOOL_TYPE));

    String editorToolName = editorTool.getName();
    String editorId = editorTool.getId();

    workspaceConfig.getAttributes().put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, editorId);

    workspaceConfig.getAttributes().put(EDITOR_TOOL_ALIAS_WORKSPACE_ATTRIBUTE, editorToolName);

    String editorIdVersion = resolveIdAndVersion(editorTool.getId());
    workspaceConfig
        .getCommands()
        .stream()
        .filter(c -> c.getAttributes().get(TOOL_NAME_COMMAND_ATTRIBUTE).equals(editorToolName))
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
