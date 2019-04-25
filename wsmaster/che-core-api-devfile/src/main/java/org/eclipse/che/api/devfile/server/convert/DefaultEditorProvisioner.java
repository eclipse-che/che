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
package org.eclipse.che.api.devfile.server.convert;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_FREE_DEVFILE_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provision default editor if there is no any another editor and default plugins for it.
 *
 * @author Sergii Leshchenko
 */
public class DefaultEditorProvisioner {

  private final String defaultEditorRef;
  private final String defaultEditor;
  private final Map<String, String> defaultPluginsToRefs;
  private final Pattern PLUGIN_PATTERN =
      Pattern.compile("(.*/)?(?<publisher>[^/]+)/(?<name>[^/]+)/(?<version>[^/]+)");

  @Inject
  public DefaultEditorProvisioner(
      @Named("che.workspace.devfile.default_editor") String defaultEditorRef,
      @Named("che.workspace.devfile.default_editor.plugins") String[] defaultPluginsRefs) {
    this.defaultEditorRef = Strings.isNullOrEmpty(defaultEditorRef) ? null : defaultEditorRef;
    this.defaultEditor = this.defaultEditorRef == null ? null : getId(this.defaultEditorRef);
    this.defaultPluginsToRefs =
        Arrays.stream(defaultPluginsRefs)
            .collect(toMap(this::getPluginPublisherAndName, identity()));
  }

  /**
   * Provision default editor if there is no editor. Also provisions default plugins for default
   * editor regardless whether it is provisioned or set by user.
   *
   * @param devfile devfile where editor and plugins should be provisioned
   */
  public void apply(DevfileImpl devfile) {
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
      isDefaultEditorUsed = defaultEditor.equals(resolveIdAndVersion(editor.getId()).first);
    }

    if (isDefaultEditorUsed) {
      provisionDefaultPlugins(components);
    }
  }

  private void provisionDefaultPlugins(List<ComponentImpl> components) {
    Map<String, String> missingPluginsIdToRef = new HashMap<>(defaultPluginsToRefs);

    // TODO
    components
        .stream()
        .filter(t -> PLUGIN_COMPONENT_TYPE.equals(t.getType()))
        .forEach(t -> missingPluginsIdToRef.remove(getId(t.getId())));

    missingPluginsIdToRef
        .values()
        .forEach(pluginRef -> components.add(new ComponentImpl(PLUGIN_COMPONENT_TYPE, pluginRef)));
  }

  private String getId(String reference) {
    return resolveIdAndVersion(reference).first;
  }

  private String getPluginPublisherAndName(String reference) {
    return resolveIdAndVersion(reference).first;
  }

  // todo
  private Pair<String, String> resolveIdAndVersion(String ref) {
    int lastSlashPosition = ref.lastIndexOf("/");
    String idVersion;
    if (lastSlashPosition < 0) {
      idVersion = ref;
    } else {
      idVersion = ref.substring(lastSlashPosition + 1);
    }
    String[] splitted = idVersion.split(":", 2);
    return Pair.of(splitted[0], splitted[1]);
  }
}
