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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request to pull (fetch and merge) changes from remote repository to local branch.
 *
 * @author andrew00x
 */
@DTO
public interface PullRequest {
  /** @return refspec to fetch */
  String getRefSpec();

  void setRefSpec(String refSpec);

  PullRequest withRefSpec(String refSpec);

  /** @return remote name. If <code>null</code> then 'origin' will be used */
  String getRemote();

  void setRemote(String remote);

  PullRequest withRemote(String remote);

  /** Returns the value of 'Pull with rebase' flag. */
  boolean getRebase();

  void setRebase(boolean rebase);

  PullRequest withRebase(boolean rebase);

  /**
   * @return time (in seconds) to wait without data transfer occurring before aborting fetching data
   *     from remote repository
   */
  int getTimeout();

  void setTimeout(int timeout);

  PullRequest withTimeout(int timeout);

  /** Returns user name for authentication */
  String getUsername();

  /** Set user name for authentication. */
  void setUsername(String username);

  /** @return {@link PullRequest} with specified user name for authentication */
  PullRequest withUsername(String username);

  /** @return password for authentication */
  String getPassword();

  /** Set password for authentication. */
  void setPassword(String password);

  /** @return {@link PullRequest} with specified password for authentication */
  PullRequest withPassword(String password);
}
