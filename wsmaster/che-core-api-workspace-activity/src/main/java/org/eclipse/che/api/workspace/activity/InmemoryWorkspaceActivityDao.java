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
package org.eclipse.che.api.workspace.activity;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Singleton;

/**
 * Inmemory workspaces expiration times storage.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
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
  public List<String> findExpired(long timestamp) {
    return activeWorkspaces
        .entrySet()
        .stream()
        .filter(e -> e.getValue() < timestamp)
        .map(Entry::getKey)
        .collect(Collectors.toList());
  }
}
