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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

/**
 * Data object for storing workspace expiration times.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Entity(name = "WorkspaceExpiration")
@NamedQueries({
  @NamedQuery(
      name = "WorkspaceExpiration.getExpired",
      query = "SELECT e FROM WorkspaceExpiration e WHERE e.expiration < :expiration")
})
@Table(name = "che_workspace_expiration")
public class WorkspaceExpiration {

  @Id
  @Column(name = "workspace_id")
  private String workspaceId;

  @PrimaryKeyJoinColumn private WorkspaceImpl workspace;

  @Column(name = "expiration")
  private long expiration;

  public WorkspaceExpiration() {}

  public WorkspaceExpiration(String workspaceId, long expiration) {
    this.workspaceId = workspaceId;
    this.expiration = expiration;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public long getExpiration() {
    return expiration;
  }

  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WorkspaceExpiration)) {
      return false;
    }
    WorkspaceExpiration that = (WorkspaceExpiration) obj;
    final WorkspaceExpiration other = (WorkspaceExpiration) obj;
    return Objects.equals(workspaceId, other.getWorkspaceId())
        && Objects.equals(expiration, other.getExpiration());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(workspaceId);
    hash = 31 * hash + Objects.hashCode(expiration);
    return hash;
  }

  @Override
  public String toString() {
    return "WorkspaceExpiration{"
        + "workspaceId='"
        + workspaceId
        + '\''
        + ", expiration="
        + expiration
        + '}';
  }
}
