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

  public static final int JSON_IS_NOT_A_VALID_REPRESENTATION_FOR_AN_OBJECT_OF_TYPE_WARNING_CODE =
      4108;
  public static final String JSON_IS_NOT_A_VALID_REPRESENTATION_FOR_AN_OBJECT_OF_TYPE_MESSAGE_FMT =
      "Unable to provision git configuration into runtime. "
          + "Json object in user preferences is not a valid representation for an object of type map: '%s'";

  public static final int EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_WARNING_CODE = 4109;
  public static final String EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_MESSAGE_FMT =
      "Unable to provision git configuration into runtime. "
          + "Internal server error occurred during operating with user management: '%s'";

  public static final int NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL = 4110;
  public static final String NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL_MESSAGE =
      "Not able to provision objects for PreviewUrl. Message: '%s'";

  public static final int NOT_ABLE_TO_PROVISION_SSH_KEYS = 4111;
  public static final String NOT_ABLE_TO_PROVISION_SSH_KEYS_MESSAGE =
      "Not able to provision SSH Keys. Message: '%s'";

  public static final int NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT = 4200;
  public static final String NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_MESSAGE =
      "Not able to find workspace attributes for %s. Reason %s";

  public static final int NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS = 4250;
  public static final String
      NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS_MESSAGE =
          "Not able to provision workspace %s deployment labels or annotations because of invalid configuration. Reason: '%s'";

  public static final int SSH_KEYS_WILL_NOT_BE_MOUNTED = 4300;
  public static final String SSH_KEYS_WILL_NOT_BE_MOUNTED_MESSAGE =
      "Ssh keys %s have invalid names and can't be mounted to workspace %s.";

  private Warnings() {}
}
