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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

@Entity
@Table(name = "che_workspace_activity")
@NamedQueries({
  @NamedQuery(
      name = "WorkspaceActivity.getExpiredIdle",
      query = "SELECT a.workspaceId FROM WorkspaceActivity a WHERE a.expiration < :expiration"),
  @NamedQuery(
      name = "WorkspaceActivity.getExpiredRunTimeout",
      query =
          "SELECT a.workspaceId FROM WorkspaceActivity a WHERE "
              + "(a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING AND "
              + "a.lastRunning < :timeDifference)"),
  @NamedQuery(
      name = "WorkspaceActivity.getStoppedSince",
      query =
          "SELECT a.workspaceId FROM WorkspaceActivity a "
              + "WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED "
              + "AND a.lastStopped <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getStoppedSinceCount",
      query =
          "SELECT COUNT(a) FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED"
              + " AND a.lastStopped <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getStoppingSince",
      query =
          "SELECT a.workspaceId FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING"
              + " AND a.lastStopping <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getStoppingSinceCount",
      query =
          "SELECT COUNT(a) FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING"
              + " AND a.lastStopping <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getRunningSince",
      query =
          "SELECT a.workspaceId FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING"
              + " AND a.lastRunning <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getRunningSinceCount",
      query =
          "SELECT COUNT(a) FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING"
              + " AND a.lastRunning <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getStartingSince",
      query =
          "SELECT a.workspaceId FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING"
              + " AND a.lastStarting <= :time"),
  @NamedQuery(
      name = "WorkspaceActivity.getStartingSinceCount",
      query =
          "SELECT COUNT(a) FROM WorkspaceActivity a"
              + " WHERE a.status = org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING"
              + " AND a.lastStarting <= :time"),
  @NamedQuery(name = "WorkspaceActivity.getAll", query = "SELECT a FROM WorkspaceActivity a"),
  @NamedQuery(
      name = "WorkspaceActivity.getAllCount",
      query = "SELECT COUNT(a) FROM WorkspaceActivity a"),
})
public class WorkspaceActivity {

  @Id
  @Column(name = "workspace_id")
  private String workspaceId;

  @Column(name = "created")
  private Long created;

  @Column(name = "last_starting")
  private Long lastStarting;

  @Column(name = "last_running")
  private Long lastRunning;

  @Column(name = "last_stopping")
  private Long lastStopping;

  @Column(name = "last_stopped")
  private Long lastStopped;

  @Column(name = "expiration")
  private Long expiration;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private WorkspaceStatus status;

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public Long getCreated() {
    return created;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  public Long getLastStarting() {
    return lastStarting;
  }

  public void setLastStarting(long lastStarting) {
    this.lastStarting = lastStarting;
  }

  public Long getLastRunning() {
    return lastRunning;
  }

  public void setLastRunning(long lastRunning) {
    this.lastRunning = lastRunning;
  }

  public Long getLastStopping() {
    return lastStopping;
  }

  public void setLastStopping(long lastStopping) {
    this.lastStopping = lastStopping;
  }

  public Long getLastStopped() {
    return lastStopped;
  }

  public void setLastStopped(long lastStopped) {
    this.lastStopped = lastStopped;
  }

  public Long getExpiration() {
    return expiration;
  }

  public void setExpiration(Long expiration) {
    this.expiration = expiration;
  }

  public WorkspaceStatus getStatus() {
    return status;
  }

  public void setStatus(WorkspaceStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkspaceActivity activity = (WorkspaceActivity) o;
    return Objects.equals(workspaceId, activity.workspaceId)
        && Objects.equals(created, activity.created)
        && Objects.equals(lastStarting, activity.lastStarting)
        && Objects.equals(lastRunning, activity.lastRunning)
        && Objects.equals(lastStopping, activity.lastStopping)
        && Objects.equals(lastStopped, activity.lastStopped)
        && Objects.equals(expiration, activity.expiration)
        && status == activity.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        workspaceId,
        created,
        lastStarting,
        lastRunning,
        lastStopping,
        lastStopped,
        expiration,
        status);
  }

  @Override
  public String toString() {
    return "WorkspaceActivity{"
        + "workspaceId='"
        + workspaceId
        + '\''
        + ", created="
        + created
        + ", lastStarting="
        + lastStarting
        + ", lastRunning="
        + lastRunning
        + ", lastStopping="
        + lastStopping
        + ", lastStopped="
        + lastStopped
        + ", expiration="
        + expiration
        + ", status="
        + status
        + '}';
  }
}
