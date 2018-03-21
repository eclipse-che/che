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
package org.eclipse.che.api.workspace.activity;

import java.util.List;
import org.eclipse.che.api.core.ServerException;

/**
 * Data access object for workspaces expiration times.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public interface WorkspaceActivityDao {

  /**
   * Sets workspace expiration time. Any running workspace must prolongate expiration time
   * periodically, otherwise it will be stopped by passing that time.
   *
   * @param expiration expiration object to store
   * @throws ServerException when operation failed
   */
  void setExpiration(WorkspaceExpiration expiration) throws ServerException;

  /**
   * Removes workspace expiration time (basically used on ws stop).
   *
   * @param workspaceId workspace id to remove expiration
   * @throws ServerException when operation failed
   */
  void removeExpiration(String workspaceId) throws ServerException;

  /**
   * Finds workspaces which are passed given expiration time and must be stopped.
   *
   * @param timestamp expiration time
   * @return list of workspaces which expiration time is older than given timestamp
   * @throws ServerException when operation failed
   */
  List<String> findExpired(long timestamp) throws ServerException;
}
