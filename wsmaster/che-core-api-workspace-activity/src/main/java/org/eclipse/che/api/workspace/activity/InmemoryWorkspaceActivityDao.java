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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

/**
 * In-memory workspaces expiration times storage.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class InmemoryWorkspaceActivityDao implements WorkspaceActivityDao {

  private final Map<String, WorkspaceActivity> workspaceActivities = new ConcurrentHashMap<>();

  @Override
  public void setExpiration(WorkspaceExpiration expiration) {
    setExpirationTime(expiration.getWorkspaceId(), expiration.getExpiration());
  }

  @Override
  public void setExpirationTime(String workspaceId, long expirationTime) {
    findActivity(workspaceId).setExpiration(expirationTime);
  }

  @Override
  public void removeExpiration(String workspaceId) {
    findActivity(workspaceId).setExpiration(null);
  }

  @Override
  public List<String> findExpired(long timestamp) {
    return workspaceActivities
        .values()
        .stream()
        .filter(a -> a.getExpiration() != null && a.getExpiration() < timestamp)
        .map(WorkspaceActivity::getWorkspaceId)
        .collect(Collectors.toList());
  }

  @Override
  public void setCreatedTime(String workspaceId, long createdTimestamp) {
    WorkspaceActivity activity = findActivity(workspaceId);
    activity.setCreated(createdTimestamp);
    if (activity.getStatus() == null) {
      activity.setStatus(WorkspaceStatus.STOPPED);
    }
  }

  @Override
  public void setStatusChangeTime(String workspaceId, WorkspaceStatus status, long timestamp)
      throws ServerException {
    WorkspaceActivity a = findActivity(workspaceId);
    switch (status) {
      case STARTING:
        a.setLastStarting(timestamp);
        break;
      case RUNNING:
        a.setLastRunning(timestamp);
        break;
      case STOPPING:
        a.setLastStopping(timestamp);
        break;
      case STOPPED:
        a.setLastStopped(timestamp);
        break;
      default:
        throw new ServerException("Unhandled workspace status: " + status);
    }
  }

  @Override
  public Page<String> findInStatusSince(
      long timestamp, WorkspaceStatus status, int maxItems, long skipCount) {
    List<String> all =
        workspaceActivities
            .values()
            .stream()
            .filter(a -> a.getStatus() == status)
            .filter(
                a -> {
                  switch (status) {
                    case STOPPED:
                      return isGreater(a.getLastStopped(), timestamp);
                    case STOPPING:
                      return isGreater(a.getLastStopped(), timestamp);
                    case RUNNING:
                      return isGreater(a.getLastStopped(), timestamp);
                    case STARTING:
                      return isGreater(a.getLastStopped(), timestamp);
                    default:
                      return false;
                  }
                })
            .map(WorkspaceActivity::getWorkspaceId)
            .collect(Collectors.toList());

    int total = all.size();
    int from = skipCount > total ? total : (int) skipCount;
    int to = from + maxItems;
    if (to > total) {
      to = total;
    }

    List<String> page = all.subList(from, to);

    return new Page<>(page, skipCount, maxItems, all.size());
  }

  @Override
  public WorkspaceActivity findActivity(String workspaceId) {
    return workspaceActivities.computeIfAbsent(workspaceId, __ -> new WorkspaceActivity());
  }

  @Override
  public void removeActivity(String workspaceId) throws ServerException {
    workspaceActivities.remove(workspaceId);
  }

  private boolean isGreater(Long value, long threshold) {
    return value != null && value > threshold;
  }
}
