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
package org.eclipse.che.workspace.infrastructure.kubernetes;

/**
 * Constants for Kubernetes infrastructure specific warnings.
 *
 * @author Sergii Leshchenko
 */
public final class Warnings {

  public static final int INGRESSES_IGNORED_WARNING_CODE = 4100;
  public static final String INGRESSES_IGNORED_WARNING_MESSAGE =
      "Ingresses specified in Kubernetes recipe are ignored. "
          + "To expose ports please define servers in machine configuration.";

  public static final int SECRET_IGNORED_WARNING_CODE = 4102;
  public static final String SECRET_IGNORED_WARNING_MESSAGE =
      "Secrets specified in Kubernetes recipe are ignored.";

  public static final int RESTART_POLICY_SET_TO_NEVER_WARNING_CODE = 4104;
  public static final String RESTART_POLICY_SET_TO_NEVER_WARNING_MESSAGE_FMT =
      "Restart policy '%s' for pod '%s' is rewritten with %s";

  public static final int UNKNOWN_SECURE_SERVER_EXPOSER_CONFIGURED_IN_WS_WARNING_CODE = 4105;

  public static final int COMMAND_IS_CONFIGURED_IN_PLUGIN_WITHOUT_CONTAINERS_WARNING_CODE = 4106;
  public static final String
      COMMAND_IS_CONFIGURED_IN_PLUGIN_WITHOUT_CONTAINERS_WARNING_MESSAGE_FMT =
          "There are configured commands for plugin '%s' that doesn't have any containers";

  public static final int COMMAND_IS_CONFIGURED_IN_PLUGIN_WITH_MULTIPLY_CONTAINERS_WARNING_CODE =
      4107;
  public static final String
      COMMAND_IS_CONFIGURED_IN_PLUGIN_WITH_MULTIPLY_CONTAINERS_WARNING_MESSAGE_FMT =
          "There are configured commands for plugin '%s' that has multiply containers. Commands will be configured to be run in first container";

  private Warnings() {}
}
