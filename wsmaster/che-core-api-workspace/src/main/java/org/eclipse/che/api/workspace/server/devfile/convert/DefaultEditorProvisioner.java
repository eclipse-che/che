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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_FREE_DEVFILE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;

/**
 * Provision default editor if there is no any another editor and default plugins for it.
 *
 * @author Sergii Leshchenko
 */
public class DefaultEditorProvisioner {

  private final String defaultEditorRef;
  private final String defaultEditor;
  private final Map<String, String> defaultPluginsToRefs;
  private final PluginFQNParser fqnParser;

  @Inject
  public DefaultEditorProvisioner(
      @Named("che.workspace.devfile.default_editor") String defaultEditorRef,
      @Named("che.workspace.devfile.default_editor.plugins") String[] defaultPluginsRefs,
      PluginFQNParser fqnParser)
      throws DevfileException {
    this.defaultEditorRef = isNullOrEmpty(defaultEditorRef) ? null : defaultEditorRef;
    this.fqnParser = fqnParser;
    this.defaultEditor =
        this.defaultEditorRef == null ? null : getPluginPublisherAndName(this.defaultEditorRef);
    Map<String, String> map = new HashMap<>();
    for (String defaultPluginsRef : defaultPluginsRefs) {
      map.put(getPluginPublisherAndName(defaultPluginsRef), defaultPluginsRef);
    }
    this.defaultPluginsToRefs = map;
  }

  /**
   * Provision default editor if there is no editor. Also provisions default plugins for default
   * editor regardless whether it is provisioned or set by user.
   *
   * @param devfile devfile where editor and plugins should be provisioned
   * @param contentProvider content provider for plugin references retrieval
   */
  public void apply(DevfileImpl devfile, FileContentProvider contentProvider)
      throws DevfileException {
    if (defaultEditorRef == null) {
      // there is no default editor configured
      return;
    }

    if ("true".equals(devfile.getAttributes().get(EDITOR_FREE_DEVFILE_ATTRIBUTE))) {
      return;
    }

    List<ComponentImpl> components = devfile.getComponents();

    Optional<ComponentImpl> editorOpt =
        components.stream().filter(t -> EDITOR_COMPONENT_TYPE.equals(t.getType())).findFirst();

    boolean isDefaultEditorUsed;
    if (!editorOpt.isPresent()) {
      components.add(new ComponentImpl(EDITOR_COMPONENT_TYPE, defaultEditorRef));
      isDefaultEditorUsed = true;
    } else {
      Component editor = editorOpt.get();
      String editorPublisherAndName = getPluginPublisherAndName(editor, contentProvider);
      isDefaultEditorUsed = defaultEditor.equals(editorPublisherAndName);
    }

    if (isDefaultEditorUsed) {
      provisionDefaultPlugins(components, contentProvider);
    }
  }

  private void provisionDefaultPlugins(
      List<ComponentImpl> components, FileContentProvider contentProvider) throws DevfileException {
    Map<String, String> missingPluginsIdToRef = new HashMap<>(defaultPluginsToRefs);

    for (ComponentImpl t : components) {
      if (PLUGIN_COMPONENT_TYPE.equals(t.getType())) {
        String pluginPublisherAndName = getPluginPublisherAndName(t, contentProvider);
        missingPluginsIdToRef.remove(pluginPublisherAndName);
      }
    }

    missingPluginsIdToRef
        .values()
        .forEach(pluginRef -> components.add(new ComponentImpl(PLUGIN_COMPONENT_TYPE, pluginRef)));
  }

  private String getPluginPublisherAndName(Component component, FileContentProvider contentProvider)
      throws DevfileException {
    try {
      if (component.getId() != null) {
        return getPluginPublisherAndName(component.getId());
      } else {
        return fqnParser
            .evaluateFqn(component.getReference(), contentProvider)
            .getPublisherAndName();
      }
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }

  private String getPluginPublisherAndName(String pluginId) throws DevfileException {
    try {
      return fqnParser.parsePluginFQN(pluginId).getPublisherAndName();
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }
}
