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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Singleton;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
@Singleton
public class InmemoryWorkspaceActivityDao implements WorkspaceActivityDao {

  private final Map<String, Long> activeWorkspaces = new ConcurrentHashMap<>();

  @Override
  public void setExpiration(WorkspaceExpiration expiration) {
    activeWorkspaces.put(expiration.getWorkspaceId(), expiration.getExpiration());
  }

  @Override
  public void removeExpiration(String workspaceId) {
    activeWorkspaces.remove(workspaceId);
  }

  @Override
  public List<WorkspaceExpiration> findExpired(long timestamp) {
    return activeWorkspaces
        .entrySet()
        .stream()
        .filter(e -> e.getValue() < timestamp)
        .map(e -> new WorkspaceExpiration(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }
}
