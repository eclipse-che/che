/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
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
  public void setExpirationTime(String workspaceId, long expirationTime) {
    findActivity(workspaceId).setExpiration(expirationTime);
  }

  @Override
  public void removeExpiration(String workspaceId) {
    findActivity(workspaceId).setExpiration(null);
  }

  /**
   * Finds any workspaces that have expired.
   *
   * <p>A workspace is expired when the expiration value is less than the current time or when the
   * difference between the current time and the last running time is greater than the run timeout
   *
   * @param timestamp expiration time
   * @param runTimeout time after which the workspace will be stopped regardless of activity
   * @return
   */
  @Override
  public List<String> findExpiredRunTimeout(long timestamp, long runTimeout) {
    return workspaceActivities
        .values()
        .stream()
        .filter(
            a ->
                (a.getExpiration() != null && a.getExpiration() < timestamp)
                    || (runTimeout > 0
                        && a.getStatus() == WorkspaceStatus.RUNNING
                        && timestamp - a.getLastRunning() > runTimeout))
        .map(WorkspaceActivity::getWorkspaceId)
        .collect(toList());
  }

  @Override
  public List<String> findExpiredIdle(long timestamp) {
    return workspaceActivities
        .values()
        .stream()
        .filter(a -> a.getExpiration() != null && a.getExpiration() < timestamp)
        .map(WorkspaceActivity::getWorkspaceId)
        .collect(toList());
  }

  @Override
  public void setCreatedTime(String workspaceId, long createdTimestamp) {
    WorkspaceActivity activity = findActivity(workspaceId);
    activity.setCreated(createdTimestamp);
    if (activity.getStatus() == null) {
      activity.setStatus(WorkspaceStatus.STOPPED);
      activity.setLastStopped(createdTimestamp);
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
            .filter(a -> a.getStatus() == status && isGreater(a.getLastStopped(), timestamp))
            .map(WorkspaceActivity::getWorkspaceId)
            .collect(toList());

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
  public long countWorkspacesInStatus(WorkspaceStatus status, long timestamp) {
    return findInStatusSince(timestamp, status, Integer.MAX_VALUE, 0).getItemsCount();
  }

  @Override
  public WorkspaceActivity findActivity(String workspaceId) {
    return workspaceActivities.computeIfAbsent(workspaceId, __ -> new WorkspaceActivity());
  }

  @Override
  public void removeActivity(String workspaceId) throws ServerException {
    workspaceActivities.remove(workspaceId);
  }

  @Override
  public void createActivity(WorkspaceActivity activity) throws ConflictException {
    if (workspaceActivities.containsKey(activity.getWorkspaceId())) {
      throw new ConflictException("Already exists.");
    } else {
      workspaceActivities.put(activity.getWorkspaceId(), activity);
    }
  }

  @Override
  public Page<WorkspaceActivity> getAll(int maxItems, long skipCount) {
    return new Page<>(
        workspaceActivities.values().stream().skip(skipCount).limit(maxItems).collect(toList()),
        skipCount,
        maxItems,
        workspaceActivities.size());
  }

  private boolean isGreater(Long value, long threshold) {
    return value != null && value > threshold;
  }
}
