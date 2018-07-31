/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace;

import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines a contract for workspace instance.
 *
 * <p>Workspace instance defines all the attributes related to the certain workspace plus
 * configuration used to create instance plus its runtime.
 *
 * @author Yevhenii Voevodin
 */
public interface Workspace {

  /** Returns the identifier of this workspace instance. It is mandatory and unique. */
  String getId();

  /**
   * Returns the namespace of the current workspace instance. Workspace name is unique for
   * workspaces in the same namespace.
   */
  String getNamespace();

  /**
   * Returns the status of the current workspace instance.
   *
   * <p>All the workspaces which are stopped have runtime are considered {@link
   * WorkspaceStatus#STOPPED}.
   */
  WorkspaceStatus getStatus();

  /**
   * Returns workspace instance attributes (e.g. last modification date). Workspace attributes must
   * not contain null keys or values.
   */
  Map<String, String> getAttributes();

  /**
   * Returns true if this workspace is temporary, and false otherwise. Temporary workspace exists
   * only in runtime so {@link #getRuntime()} will never return null for temporary workspace as well
   * as {@link #getStatus()} will never return {@link WorkspaceStatus#STOPPED}.
   */
  boolean isTemporary();

  /**
   * Returns a configuration of this workspace instance. Workspace is always created from the
   * configuration so the configuration is mandatory for every workspace instance.
   */
  WorkspaceConfig getConfig();

  /**
   * Returns the runtime of this workspace instance. If status of this workspace instance is either
   * {@link WorkspaceStatus#RUNNING} or {@link WorkspaceStatus#STARTING}, or {@link
   * WorkspaceStatus#STOPPING} then returned value is not null, otherwise it is.
   */
  @Nullable
  Runtime getRuntime();
}
