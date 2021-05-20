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
package org.eclipse.che.api.core.model.workspace;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
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
   * Returns a configuration of this workspace instance. The only one format (workspace config or
   * devfile) may be used for workspace at the same time.
   */
  @Nullable
  WorkspaceConfig getConfig();

  /**
   * Returns a configuration of this workspace instance in Devfile format. The only one format
   * (workspace config or devfile) may be used for workspace at the same time.
   */
  @Beta
  @Nullable
  Devfile getDevfile();

  /**
   * Returns the runtime of this workspace instance. If status of this workspace instance is either
   * {@link WorkspaceStatus#RUNNING} or {@link WorkspaceStatus#STARTING}, or {@link
   * WorkspaceStatus#STOPPING} then returned value is not null, otherwise it is.
   */
  @Nullable
  Runtime getRuntime();
}
