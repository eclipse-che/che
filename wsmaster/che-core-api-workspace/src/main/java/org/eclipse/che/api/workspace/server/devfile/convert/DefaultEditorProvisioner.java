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

import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_FREE_DEVFILE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;

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
    this.defaultEditorRef = Strings.isNullOrEmpty(defaultEditorRef) ? null : defaultEditorRef;
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
   */
  public void apply(DevfileImpl devfile) throws DevfileException {
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
      isDefaultEditorUsed = defaultEditor.equals(getPluginPublisherAndName(editor.getId()));
    }

    if (isDefaultEditorUsed) {
      provisionDefaultPlugins(components);
    }
  }

  private void provisionDefaultPlugins(List<ComponentImpl> components) throws DevfileException {
    Map<String, String> missingPluginsIdToRef = new HashMap<>(defaultPluginsToRefs);

    for (ComponentImpl t : components) {
      if (PLUGIN_COMPONENT_TYPE.equals(t.getType())) {
        missingPluginsIdToRef.remove(getPluginPublisherAndName(t.getId()));
      }
    }

    missingPluginsIdToRef
        .values()
        .forEach(pluginRef -> components.add(new ComponentImpl(PLUGIN_COMPONENT_TYPE, pluginRef)));
  }

  private String getPluginPublisherAndName(String reference) throws DevfileException {
    try {
      ExtendedPluginFQN meta = fqnParser.parsePluginFQN(reference);
      return meta.getPublisherAndName();
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }
}
