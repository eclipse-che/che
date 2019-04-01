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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.commons.lang.NameGenerator;

/**
 * Provision default editor if there is no any another editor and default plugins for it.
 *
 * @author Sergii Leshchenko
 */
public class DefaultEditorProvisioner {

  private final String defaultEditorRef;
  private final String defaultEditorId;
  private final Map<String, String> defaultPluginsIdToRef;

  @Inject
  public DefaultEditorProvisioner(
      @Named("che.workspace.devfile.default_editor") String defaultEditorRef,
      @Named("che.workspace.devfile.default_editor.plugins") String[] defaultPluginsRefs) {
    this.defaultEditorRef = Strings.isNullOrEmpty(defaultEditorRef) ? null : defaultEditorRef;
    this.defaultEditorId = this.defaultEditorRef == null ? null : getId(this.defaultEditorRef);
    this.defaultPluginsIdToRef =
        Arrays.stream(defaultPluginsRefs).collect(toMap(this::getId, identity()));
  }

  /**
   * Provision default editor if there is no any another editor and default plugins for it.
   *
   * @param devfile devfile where editor and plugins should be provisioned
   */
  public void apply(Devfile devfile) {
    if (defaultEditorRef == null) {
      // there is no default editor configured
      return;
    }

    if ("true".equals(devfile.getAttributes().get(EDITOR_FREE_DEVFILE_ATTRIBUTE))) {
      return;
    }

    List<Component> components = devfile.getComponents();
    Set<String> componentsNames =
        components.stream().map(Component::getName).collect(Collectors.toCollection(HashSet::new));

    Optional<Component> editorOpt =
        components.stream().filter(t -> EDITOR_COMPONENT_TYPE.equals(t.getType())).findFirst();

    boolean isDefaultEditorUsed;
    if (!editorOpt.isPresent()) {
      components.add(
          new Component()
              .withName(findAvailableName(componentsNames, defaultEditorRef))
              .withType(EDITOR_COMPONENT_TYPE)
              .withId(defaultEditorRef));
      isDefaultEditorUsed = true;
    } else {
      Component editor = editorOpt.get();
      isDefaultEditorUsed = editor.getId().startsWith(defaultEditorId + ':');
    }

    if (isDefaultEditorUsed) {
      provisionDefaultPlugins(components, componentsNames);
    }
  }

  private void provisionDefaultPlugins(List<Component> components, Set<String> componentsNames) {
    Map<String, String> missingPluginsIdToRef = new HashMap<>(defaultPluginsIdToRef);

    components
        .stream()
        .filter(t -> PLUGIN_COMPONENT_TYPE.equals(t.getType()))
        .forEach(t -> missingPluginsIdToRef.remove(getId(t.getId())));

    missingPluginsIdToRef
        .values()
        .forEach(
            pluginRef ->
                components.add(
                    new Component()
                        .withType(PLUGIN_COMPONENT_TYPE)
                        .withId(pluginRef)
                        .withName(findAvailableName(componentsNames, pluginRef))));
  }

  /**
   * Returns available name for component with the specified id.
   *
   * <p>Id without version is used as base name and generated part is added if it is already busy.
   */
  private String findAvailableName(Set<String> busyNames, String componentRef) {
    String id = getId(componentRef);
    String name = id;
    while (!busyNames.add(name)) {
      name = NameGenerator.generate(id, 5);
    }
    return name;
  }

  private String getId(String reference) {
    return reference.split(":", 2)[0];
  }
}
