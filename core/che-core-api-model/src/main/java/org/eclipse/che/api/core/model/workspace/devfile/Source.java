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
package org.eclipse.che.api.core.model.workspace.devfile;

public interface Source {
  /** Returns type of source. It is mandatory. */
  String getType();

  /** Returns project's source location address. It is mandatory. */
  String getLocation();

  /**
   * The name of the of the branch to check out after obtaining the source from the location. The
   * branch has to already exist in the source otherwise the default branch is used. In case of git,
   * this is also the name of the remote branch to push to.
   */
  String getBranch();

  /** The tag or commit id to reset the checked out branch to. */
  String getStartPoint();

  /**
   * The name of the tag to reset the checked out branch to. Note that this is equivalent to
   * 'startPoint' and provided for convenience.
   */
  String getTag();

  /**
   * The id of the commit to reset the checked out branch to. Note that this is equivalent to
   * 'startPoint' and provided for convenience.
   */
  String getCommitId();

  /**
   * The directory which is kept by sparse checkout. If this parameter is not null then the only
   * given directory will be present after clone.
   */
  String getSparseCheckoutDir();
}
