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
package org.eclipse.che.api.core.model.workspace.runtime;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Holds meta information about where workspace is run, and to whom it belongs to (workspace and
 * user info).
 *
 * @author gazarenkov
 */
public interface RuntimeIdentity {

  /** The id of workspace to which the runtime belongs to. Must not be null. */
  String getWorkspaceId();

  /**
   * The workspace environment name that was used to start runtime. May be null if workspace does
   * not contain stored environment configuration (it may be generated on fly).
   */
  @Nullable
  String getEnvName();

  /** The id of user who initialized workspace start. Must not be null. */
  String getOwnerId();

  /** Returns infrastructure namespace where runtime is run. Must not be null. */
  String getInfrastructureNamespace();
}
