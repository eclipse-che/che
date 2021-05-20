/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
 * Constants for Kubernetes implementation of spi.
 *
 * @author Sergii Leshchenko
 */
public final class Constants {

  /**
   * The label that contains a value with original object name.
   *
   * <p>Names of Kubernetes objects should be modified to avoid collision with objects of other
   * workspaces.
   *
   * @see org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner
   */
  public static final String CHE_ORIGINAL_NAME_LABEL = "che.original_name";

  /** The label that contains a value with workspace id to which object belongs to. */
  public static final String CHE_WORKSPACE_ID_LABEL = "che.workspace_id";

  /** The label that contains a value with user id to which object belongs to. */
  public static final String CHE_USER_ID_LABEL = "che.user_id";

  /** The label that contains name of deployment responsible for Pod. */
  public static final String CHE_DEPLOYMENT_NAME_LABEL = "che.deployment_name";

  /** The label that contains a value with volume name. */
  public static final String CHE_VOLUME_NAME_LABEL = "che.workspace.volume_name";

  /** Kubernetes Pod status phase values */
  public static final String POD_STATUS_PHASE_RUNNING = "Running";

  public static final String POD_STATUS_PHASE_FAILED = "Failed";
  public static final String POD_STATUS_PHASE_SUCCEEDED = "Succeeded";

  private Constants() {}
}
