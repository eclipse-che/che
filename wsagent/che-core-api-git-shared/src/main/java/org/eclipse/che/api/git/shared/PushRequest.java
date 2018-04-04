/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Request to update remote refs using local refs. In other words send changes from local repository
 * to remote one.
 *
 * @author andrew00x
 */
@DTO
public interface PushRequest {
  /** @return list of refspec to push */
  List<String> getRefSpec();

  void setRefSpec(List<String> refSpec);

  PushRequest withRefSpec(List<String> refspec);

  /**
   * @return remote repository. URI or name is acceptable. If not specified then 'origin' will be
   *     used
   */
  String getRemote();

  void setRemote(String remote);

  PushRequest withRemote(String remote);

  /** @return force or not push operation */
  boolean isForce();

  void setForce(boolean isForce);

  PushRequest withForce(boolean force);

  /**
   * @return time (in seconds) to wait without data transfer occurring before aborting pushing data
   *     to remote repository
   */
  int getTimeout();

  void setTimeout(int timeout);

  PushRequest withTimeout(int timeout);

  /** Returns user name for authentication */
  String getUsername();

  /** Set user name for authentication. */
  void setUsername(String username);

  /** @return {@link PushRequest} with specified user name for authentication */
  PushRequest withUsername(String username);

  /** @return password for authentication */
  String getPassword();

  /** Set password for authentication. */
  void setPassword(String password);

  /** @return {@link PushRequest} with specified password for authentication */
  PushRequest withPassword(String password);
}
