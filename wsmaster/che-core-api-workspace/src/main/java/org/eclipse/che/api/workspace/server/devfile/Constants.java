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
package org.eclipse.che.api.workspace.server.devfile;

import java.util.List;

public class Constants {

  private Constants() {}

  public static final String SCHEMAS_LOCATION = "schema/";

  public static final String SCHEMA_FILENAME = "devfile.json";

  public static final String CURRENT_API_VERSION = "1.0.0";

  public static final List<String> SUPPORTED_VERSIONS = List.of(CURRENT_API_VERSION, "2.0.0");

  public static final String EDITOR_COMPONENT_TYPE = "cheEditor";

  public static final String PLUGIN_COMPONENT_TYPE = "chePlugin";

  public static final String KUBERNETES_COMPONENT_TYPE = "kubernetes";

  public static final String OPENSHIFT_COMPONENT_TYPE = "openshift";

  public static final String DOCKERIMAGE_COMPONENT_TYPE = "dockerimage";

  /** Action type that should be used for commands execution. */
  public static final String EXEC_ACTION_TYPE = "exec";

  /** Workspace command attributes that indicates with which component it is associated. */
  public static final String COMPONENT_ALIAS_COMMAND_ATTRIBUTE = "componentAlias";

  /**
   * {@link org.eclipse.che.api.core.model.workspace.devfile.Endpoint} attribute name which can
   * identify endpoint as public or internal. Attribute value {@code false} makes a endpoint
   * internal, any other value or lack of the attribute makes the endpoint public.
   */
  public static final String PUBLIC_ENDPOINT_ATTRIBUTE = "public";

  /**
   * {@link org.eclipse.che.api.core.model.workspace.devfile.Endpoint} attribute name which can
   * identify endpoint as discoverable(i.e. it is accessible by its name from workspace's
   * containers). Attribute value {@code true} makes a endpoint discoverable, any other value or
   * lack of the attribute makes the endpoint non-discoverable.
   */
  public static final String DISCOVERABLE_ENDPOINT_ATTRIBUTE = "discoverable";

  /**
   * The attribute of Devfile that should be devfile when no editor is needed and default one should
   * not be provisioned. Attributes value {@code true} deactivates provisioning of default editor,
   * any other value of lack of the attributes activates provisioning of default editor
   */
  public static final String EDITOR_FREE_DEVFILE_ATTRIBUTE = "editorFree";

  /**
   * Format used for composite (containing registry URL and id) editor and plugin components
   * workspace attribute values.
   */
  public static final String COMPOSITE_EDITOR_PLUGIN_ATTRIBUTE_FORMAT = "%s#%s";
}
