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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;

import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentFQNParser;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;

/**
 * Applies changes on workspace config according to the specified editor component.
 *
 * @author Sergii Leshchenko
 */
public class EditorComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  private final ComponentFQNParser componentFQNParser;

  @Inject
  public EditorComponentToWorkspaceApplier(ComponentFQNParser componentFQNParser) {
    this.componentFQNParser = componentFQNParser;
  }

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
      FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(editorComponent != null, "Component must not be null");
    checkArgument(
        EDITOR_COMPONENT_TYPE.equals(editorComponent.getType()),
        format("Plugin must have `%s` type", EDITOR_COMPONENT_TYPE));

    final String editorComponentAlias = editorComponent.getAlias();
    final String editorId = editorComponent.getId();
    final String registryUrl = editorComponent.getRegistryUrl();
    final String memoryLimit = editorComponent.getMemoryLimit();

    if (editorComponentAlias != null) {
      workspaceConfig
          .getAttributes()
          .put(EDITOR_COMPONENT_ALIAS_WORKSPACE_ATTRIBUTE, editorComponentAlias);
    }
    final ExtendedPluginFQN fqn = componentFQNParser.evaluateFQN(editorComponent, contentProvider);
    if (!isNullOrEmpty(fqn.getReference())) {
      workspaceConfig.getAttributes().put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, fqn.getReference());
    } else {
      workspaceConfig
          .getAttributes()
          .put(
              WORKSPACE_TOOLING_EDITOR_ATTRIBUTE,
              componentFQNParser.getCompositeId(registryUrl, editorId));
    }
    if (memoryLimit != null) {
      workspaceConfig
          .getAttributes()
          .put(format(SIDECAR_MEMORY_LIMIT_ATTR_TEMPLATE, fqn.getPublisherAndName()), memoryLimit);
    }
    workspaceConfig
        .getCommands()
        .stream()
        .filter(
            c ->
                editorComponentAlias != null
                    && editorComponentAlias.equals(
                        c.getAttributes().get(COMPONENT_ALIAS_COMMAND_ATTRIBUTE)))
        .forEach(c -> c.getAttributes().put(PLUGIN_ATTRIBUTE, fqn.getId()));
  }
}
