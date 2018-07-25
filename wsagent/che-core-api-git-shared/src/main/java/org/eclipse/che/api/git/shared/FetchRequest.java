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
package org.eclipse.che.api.git.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Request to fetch data from remote repository.
 *
 * @author andrew00x
 */
@DTO
public interface FetchRequest {
  /** @return list of refspec to fetch */
  List<String> getRefSpec();

  void setRefSpec(List<String> refSpec);

  FetchRequest withRefSpec(List<String> refSpec);

  /** @return remote name. If <code>null</code> then 'origin' will be used */
  String getRemote();

  void setRemote(String remote);

  FetchRequest withRemote(String remote);

  /**
   * @return <code>true</code> if local refs must be deleted if they deleted in remote repository
   *     and <code>false</code> otherwise
   */
  boolean isRemoveDeletedRefs();

  void setRemoveDeletedRefs(boolean isRemoveDeletedRefs);

  FetchRequest withRemoveDeletedRefs(boolean isRemoveDeletedRefs);

  /**
   * @return time (in seconds) to wait without data transfer occurring before aborting fetching data
   *     from remote repository
   */
  int getTimeout();

  void setTimeout(int timeout);

  FetchRequest withTimeout(int timeout);

  /** Returns user name for authentication */
  String getUsername();

  /** Set user name for authentication. */
  void setUsername(String username);

  /** @return {@link FetchRequest} with specified user name for authentication */
  FetchRequest withUsername(String username);

  /** @return password for authentication */
  String getPassword();

  /** Set password for authentication. */
  void setPassword(String password);

  /** @return {@link FetchRequest} with specified password for authentication */
  FetchRequest withPassword(String password);
}
