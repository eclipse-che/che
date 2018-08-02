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
package org.eclipse.che.api.workspace.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

/**
 * Default implementation of {@link WorkspaceStatusCache} based on {@link ConcurrentHashMap}.
 *
 * @author Anton Korneta
 */
public class DefaultWorkspaceStatusCache implements WorkspaceStatusCache {

  private final ConcurrentHashMap<String, WorkspaceStatus> delegate = new ConcurrentHashMap<>();

  @Override
  public WorkspaceStatus get(String workspaceId) {
    return delegate.get(workspaceId);
  }

  @Override
  public WorkspaceStatus replace(String workspaceId, WorkspaceStatus newStatus) {
    return delegate.replace(workspaceId, newStatus);
  }

  @Override
  public boolean replace(
      String workspaceId, WorkspaceStatus prevStatus, WorkspaceStatus newStatus) {
    return delegate.replace(workspaceId, prevStatus, newStatus);
  }

  @Override
  public WorkspaceStatus remove(String workspaceId) {
    return delegate.remove(workspaceId);
  }

  @Override
  public WorkspaceStatus putIfAbsent(String workspaceId, WorkspaceStatus status) {
    return delegate.putIfAbsent(workspaceId, status);
  }

  @Override
  public Map<String, WorkspaceStatus> asMap() {
    return new HashMap<>(delegate);
  }
}
