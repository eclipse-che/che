/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import org.eclipse.che.api.core.model.workspace.devfile.Component;

/** Utility methods for working with components */
public class Components {

  private Components() {}

  /**
   * Get a name that can be used to identify the component in the devfile. Either the component
   * alias is used or, if not defined, the identifying attribute corresponding to the component
   * type.
   *
   * <p>The alias is unique in the devfile but the identifying attributes are only unique per
   * component type. Hence, it is required to use both type and the returned name to uniquely
   * identify a component in all cases.
   *
   * @throws IllegalStateException if the type of the component is not recognized which can be
   *     classified as an implementation bug
   */
  public static String getIdentifiableComponentName(Component component)
      throws IllegalStateException {
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
        String reference = component.getReference();
        return reference == null ? component.getType() : reference;
      default:
        throw new IllegalStateException(
            format("Unhandled component type: %s", component.getType()));
    }
  }
}
