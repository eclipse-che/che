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
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentFQNParser;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Provision default editor if there is no any another editor and default plugins for it.
 *
 * @author Sergii Leshchenko
 */
public class DefaultEditorProvisioner {

  private final String defaultEditorRef;
  private final String defaultEditor;
  private final Map<String, String> defaultPluginsToRefs;
  private final String asyncStoragePluginRef;
  private final ComponentFQNParser componentFQNParser;
  private final PluginFQNParser pluginFQNParser;

  @Inject
  public DefaultEditorProvisioner(
      @Named("che.workspace.devfile.default_editor") String defaultEditorRef,
      @Named("che.workspace.devfile.default_editor.plugins") @Nullable String defaultPluginsRefs,
      @Named("che.workspace.devfile.async.storage.plugin") String asyncStoragePluginRef,
      ComponentFQNParser componentFQNParser,
      PluginFQNParser pluginFQNParser)
      throws DevfileException {
    this.defaultEditorRef = isNullOrEmpty(defaultEditorRef) ? null : defaultEditorRef;
    this.asyncStoragePluginRef = asyncStoragePluginRef;
    this.componentFQNParser = componentFQNParser;
    this.pluginFQNParser = pluginFQNParser;
    this.defaultEditor =
        this.defaultEditorRef == null
            ? null
            : componentFQNParser.getPluginPublisherAndName(this.defaultEditorRef);
    Map<String, String> map = new HashMap<>();
    if (!isNullOrEmpty(defaultPluginsRefs)) {
      for (String defaultPluginsRef : defaultPluginsRefs.split(",")) {
        map.put(componentFQNParser.getPluginPublisherAndName(defaultPluginsRef), defaultPluginsRef);
      }
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
    if ("false".equals(devfile.getAttributes().get(PERSIST_VOLUMES_ATTRIBUTE))
        && "true".equals(devfile.getAttributes().get(ASYNC_PERSIST_ATTRIBUTE))) {
      provisionAsyncStoragePlugin(components, contentProvider);
    }
  }

  /**
   * Provision the default editor plugins and add them to the the Devfile's component list
   *
   * @param components The set of components currently present in the Devfile
   * @param contentProvider content provider for plugin references retrieval
   * @throws DevfileException - A DevfileException containing any caught InfrastructureException
   */
  private void provisionDefaultPlugins(
      List<ComponentImpl> components, FileContentProvider contentProvider) throws DevfileException {
    Map<String, String> missingPluginsIdToRef = new HashMap<>(defaultPluginsToRefs);
    removeAlreadyAddedPlugins(components, contentProvider, missingPluginsIdToRef);
    try {
      addMissingPlugins(components, contentProvider, missingPluginsIdToRef);
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }

  /**
   * Provision the for async storage service, it will provide ability backup and restore project
   * source using special storage. Will torn on only if workspace start in Ephemeral mode and has
   * attribute 'asyncPersist = true'
   *
   * @param components The set of components currently present in the Devfile
   * @param contentProvider content provider for plugin references retrieval
   * @throws DevfileException - A DevfileException containing any caught InfrastructureException
   */
  private void provisionAsyncStoragePlugin(
      List<ComponentImpl> components, FileContentProvider contentProvider) throws DevfileException {
    try {
      Map<String, String> missingPluginsIdToRef =
          Collections.singletonMap(
              componentFQNParser.getPluginPublisherAndName(asyncStoragePluginRef),
              asyncStoragePluginRef);
      addMissingPlugins(components, contentProvider, missingPluginsIdToRef);
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }

  /**
   * Checks if any of the Devfile's components are also in the list of missing default plugins, and
   * removes them.
   *
   * @param devfileComponents - The list of Devfile components
   * @param contentProvider - The content provider to retrieve YAML
   * @param missingPluginsIdToRef - The list of default plugins that are not currently in the list
   *     of Devfile components
   */
  private void removeAlreadyAddedPlugins(
      List<ComponentImpl> devfileComponents,
      FileContentProvider contentProvider,
      Map<String, String> missingPluginsIdToRef)
      throws DevfileException {
    for (ComponentImpl component : devfileComponents) {
      if (PLUGIN_COMPONENT_TYPE.equals(component.getType())) {
        String pluginPublisherAndName = getPluginPublisherAndName(component, contentProvider);
        missingPluginsIdToRef.remove(pluginPublisherAndName);
      }
    }
  }

  /**
   * Tries to add default plugins to the Devfile components. Each plugin is initially parsed by
   * plugin ref. If the plugin does not have a reference, it is added to the component list, and its
   * plugin ID will be used to resolve it. If it has a reference, the Plugin is evaluated, so that
   * its meta.yaml can be retrieved. From the meta.yaml, the new Component's ID and reference are
   * properly set, and the Component is added to the list.
   *
   * @param devfileComponents - The list of Devfile components
   * @param contentProvider - The content provider to retrieve YAML
   * @param missingPluginsIdToRef - The list of default plugins that are not currently in the list
   *     of devfile components
   * @throws InfrastructureException if the parser is unable to evaluate the FQN of the plugin.
   */
  private void addMissingPlugins(
      List<ComponentImpl> devfileComponents,
      FileContentProvider contentProvider,
      Map<String, String> missingPluginsIdToRef)
      throws InfrastructureException {
    for (String pluginRef : missingPluginsIdToRef.values()) {
      ComponentImpl component;
      ExtendedPluginFQN fqn = pluginFQNParser.parsePluginFQN(pluginRef);
      if (!isNullOrEmpty(fqn.getId())) {
        component = new ComponentImpl(PLUGIN_COMPONENT_TYPE, pluginRef);
      } else {
        component = createReferencePluginComponent(pluginRef, contentProvider);
      }
      devfileComponents.add(component);
    }
  }

  /**
   * Evaluates a plugin FQN by retrieving it's meta.yaml, and sets it's name and reference to the
   * appropriate values.
   *
   * @param pluginRef - The formatted plugin reference (e.g.
   *     eclipse/che-machine-exec-plugin/nightly)
   * @param contentProvider - The content provider used to read YAML data
   * @return - A {@link ComponentImpl} with it's ID and reference URL set.
   * @throws InfrastructureException when the parser cannot evalute the plugin's FQN.
   */
  private ComponentImpl createReferencePluginComponent(
      String pluginRef, FileContentProvider contentProvider) throws InfrastructureException {
    ExtendedPluginFQN fqn = pluginFQNParser.evaluateFqn(pluginRef, contentProvider);
    ComponentImpl component = new ComponentImpl();
    component.setType(PLUGIN_COMPONENT_TYPE);
    if (!isNullOrEmpty(fqn.getId())) {
      component.setId(fqn.getId());
    }
    if (!isNullOrEmpty(fqn.getReference())) {
      component.setReference(fqn.getReference());
    }
    return component;
  }

  private String getPluginPublisherAndName(Component component, FileContentProvider contentProvider)
      throws DevfileException {
    final ExtendedPluginFQN fqn = componentFQNParser.evaluateFQN(component, contentProvider);
    return fqn.getPublisherAndName();
  }
}
