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

public class Constants {

  public static final String SCHEMA_LOCATION = "schema/devfile.json";

  public static final String CURRENT_SPEC_VERSION = "0.0.1";

  public static final String EDITOR_TOOL_TYPE = "cheEditor";

  public static final String PLUGIN_TOOL_TYPE = "chePlugin";

  public static final String KUBERNETES_TOOL_TYPE = "kubernetes";

  public static final String OPENSHIFT_TOOL_TYPE = "openshift";

  public static final String DOCKERIMAGE_TOOL_TYPE = "dockerimage";

  /**
   * Workspace attribute which contains comma-separated list of mappings of tool id to its name
   * Example value:
   *
   * <pre>
   * eclipse/maven-jdk8:1.0.0=mvn-stack,eclipse/theia:0.0.3=theia-ide,eclipse/theia-jdtls:0.0.3=jdt.ls
   * </pre>
   */
  public static final String ALIASES_WORKSPACE_ATTRIBUTE_NAME = "toolsAliases";
}
