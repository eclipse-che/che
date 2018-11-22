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
package org.eclipse.che.workspace.infrastructure.kubernetes.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesRuntime")
@Table(name = "che_k8s_runtime")
@NamedQueries({
  @NamedQuery(name = "KubernetesRuntime.getAll", query = "SELECT r FROM KubernetesRuntime r")
})
public class KubernetesRuntimeState {
  @Id
  @Column(name = "workspace_id")
  private String workspaceId;

  @Column(name = "env_name")
  private String envName;

  @Column(name = "owner_id")
  private String ownerId;

  @Column(name = "namespace")
  private String namespace;

  @Column(name = "status")
  private WorkspaceStatus status;

  public KubernetesRuntimeState() {}

  public KubernetesRuntimeState(
      RuntimeIdentity runtimeIdentity, String namespace, WorkspaceStatus status) {
    this.envName = runtimeIdentity.getEnvName();
    this.workspaceId = runtimeIdentity.getWorkspaceId();
    this.ownerId = runtimeIdentity.getOwnerId();
    this.namespace = namespace;
    this.status = status;
  }

  public KubernetesRuntimeState(KubernetesRuntimeState entity) {
    this(entity.getRuntimeId(), entity.getNamespace(), entity.getStatus());
  }

  public String getNamespace() {
    return namespace;
  }

  public RuntimeIdentity getRuntimeId() {
    return new RuntimeIdentityImpl(workspaceId, envName, ownerId);
  }

  public WorkspaceStatus getStatus() {
    return status;
  }

  public void setStatus(WorkspaceStatus status) {
    this.status = status;
  }

  public KubernetesRuntimeState withStatus(WorkspaceStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KubernetesRuntimeState)) {
      return false;
    }
    KubernetesRuntimeState that = (KubernetesRuntimeState) o;
    return Objects.equals(workspaceId, that.workspaceId)
        && Objects.equals(envName, that.envName)
        && Objects.equals(ownerId, that.ownerId)
        && Objects.equals(getNamespace(), that.getNamespace())
        && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(workspaceId, envName, ownerId, getNamespace(), getStatus());
  }

  @Override
  public String toString() {
    return "KubernetesRuntimeState{"
        + "workspaceId='"
        + workspaceId
        + '\''
        + ", envName='"
        + envName
        + '\''
        + ", ownerId='"
        + ownerId
        + '\''
        + ", namespace='"
        + namespace
        + '\''
        + ", status="
        + status
        + '}';
  }
}
