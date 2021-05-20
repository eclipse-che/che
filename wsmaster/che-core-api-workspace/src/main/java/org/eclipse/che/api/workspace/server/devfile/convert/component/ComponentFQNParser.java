/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile.convert.component;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPOSITE_EDITOR_PLUGIN_ATTRIBUTE_FORMAT;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;

/**
 * Helper class to build {@code ExtendedPluginFQN} from editor/plugin type components and other FQN
 * utility functions.
 */
public class ComponentFQNParser {

  private final PluginFQNParser fqnParser;

  @Inject
  public ComponentFQNParser(PluginFQNParser fqnParser) {
    this.fqnParser = fqnParser;
  }

  public ExtendedPluginFQN evaluateFQN(Component component, FileContentProvider contentProvider)
      throws DevfileException {
    if (!component.getType().equals(EDITOR_COMPONENT_TYPE)
        && !component.getType().equals(PLUGIN_COMPONENT_TYPE)) {
      throw new DevfileException(
          "Invalid component type provided. Only editor or plugin is supported.");
    }
    try {
      if (!isNullOrEmpty(component.getReference())) {
        return fqnParser.evaluateFqn(component.getReference(), contentProvider);
      } else {
        return fqnParser.parsePluginFQN(
            getCompositeId(component.getRegistryUrl(), component.getId()));
      }
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }

  public String getCompositeId(String registryUrl, String id) {
    return registryUrl != null
        ? format(COMPOSITE_EDITOR_PLUGIN_ATTRIBUTE_FORMAT, registryUrl, id)
        : id;
  }

  public String getPluginPublisherAndName(String pluginId) throws DevfileException {
    try {
      ExtendedPluginFQN meta = fqnParser.parsePluginFQN(pluginId);
      return meta.getPublisherAndName();
    } catch (InfrastructureException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }
}
