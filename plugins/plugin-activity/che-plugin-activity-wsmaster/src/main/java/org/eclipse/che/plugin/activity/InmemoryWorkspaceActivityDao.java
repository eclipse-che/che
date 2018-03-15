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
package org.eclipse.che.plugin.activity;

import org.eclipse.che.api.core.ServerException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InmemoryWorkspaceActivityDao implements WorkspaceActivityDao {

  private final Map<String, Long> activeWorkspaces = new ConcurrentHashMap<>();

  @Override
  public void setExpiration(WorkspaceExpiration expiration) throws ServerException {
      activeWorkspaces.put(expiration.getWorkspaceId(), expiration.getExpiration());
  }

  @Override
  public void removeExpiration(String workspaceId) throws ServerException {
      activeWorkspaces.remove(workspaceId);
  }

  @Override
  public List<WorkspaceExpiration> findExpired(long timestamp) throws ServerException {
    return activeWorkspaces.entrySet().stream().filter(e -> e.getValue() < timestamp).map(e ->)
  }
}
