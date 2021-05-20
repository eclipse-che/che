/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.model.impl;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.eclipse.che.multiuser.permission.workspace.server.model.Worker;

/**
 * Data object for {@link Worker}
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Worker")
@NamedQueries({
  @NamedQuery(
      name = "Worker.getByWorkspaceId",
      query =
          "SELECT worker " + "FROM Worker worker " + "WHERE worker.workspaceId = :workspaceId "),
  @NamedQuery(
      name = "Worker.getCountByWorkspaceId",
      query =
          "SELECT COUNT(worker) "
              + "FROM Worker worker "
              + "WHERE worker.workspaceId = :workspaceId "),
  @NamedQuery(
      name = "Worker.getByUserId",
      query = "SELECT worker " + "FROM Worker worker " + "WHERE worker.userId = :userId "),
  @NamedQuery(
      name = "Worker.getByUserAndWorkspaceId",
      query =
          "SELECT worker "
              + "FROM Worker worker "
              + "WHERE worker.userId = :userId "
              + "AND worker.workspaceId = :workspaceId ",
      hints = {@QueryHint(name = "eclipselink.query-results-cache", value = "true")})
})
@Table(name = "che_worker")
public class WorkerImpl extends AbstractPermissions implements Worker {

  @Column(name = "workspace_id")
  private String workspaceId;

  @ManyToOne
  @JoinColumn(name = "workspace_id", insertable = false, updatable = false)
  private WorkspaceImpl workspace;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "actions")
  @CollectionTable(name = "che_worker_actions", joinColumns = @JoinColumn(name = "worker_id"))
  protected List<String> actions;

  public WorkerImpl() {}

  public WorkerImpl(String workspaceId, String userId, List<String> actions) {
    super(userId);
    this.workspaceId = workspaceId;
    if (actions != null) {
      this.actions = new ArrayList<>(actions);
    }
  }

  public WorkerImpl(Worker worker) {
    this(worker.getWorkspaceId(), worker.getUserId(), worker.getActions());
  }

  @Override
  public String getInstanceId() {
    return workspaceId;
  }

  @Override
  public String getDomainId() {
    return WorkspaceDomain.DOMAIN_ID;
  }

  @Override
  public List<String> getActions() {
    return actions;
  }

  @Override
  public String getWorkspaceId() {
    return workspaceId;
  }

  @Override
  public String toString() {
    return "WorkerImpl{"
        + "userId='"
        + getUserId()
        + '\''
        + ", workspaceId='"
        + workspaceId
        + '\''
        + ", actions="
        + actions
        + '}';
  }
}
