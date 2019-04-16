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
package org.eclipse.che.api.devfile.server;

import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;

import org.eclipse.che.api.core.model.workspace.devfile.Component;

/** Utility methods for working with components */
public class Components {

  private Components() {}

  /**
   * Get a name that can be used to identify the component in the devfile. Either the component
   * alias is used or, if not defined, the identifying attribute corresponding to the component
   * type.
   *
   * @return
   */
  public static String getIdentifiableComponentName(Component component) {
    if (component.getAlias() != null) {
      return component.getAlias();
    }

    switch (component.getType()) {
      case EDITOR_COMPONENT_TYPE:
      case PLUGIN_COMPONENT_TYPE:
        return component.getId();
      case DOCKERIMAGE_COMPONENT_TYPE:
        return component.getImage();
      case KUBERNETES_COMPONENT_TYPE:
      case OPENSHIFT_COMPONENT_TYPE:
        return component.getReference();
      default:
        return null;
    }
  }
}
