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
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesRuntime")
@Table(name = "che_k8s_runtime")
@NamedQueries({
  @NamedQuery(name = "KubernetesRuntime.getAll", query = "SELECT r FROM KubernetesRuntime r"),
  @NamedQuery(
      name = "KubernetesRuntime.getByWorkspaceId",
      query = "SELECT r FROM KubernetesRuntime r WHERE r.runtimeId.workspaceId = :workspaceId")
})
public class KubernetesRuntimeState {
  @EmbeddedId private RuntimeId runtimeId;

  @Column(name = "namespace")
  private String namespace;

  @Column(name = "status")
  private WorkspaceStatus status;

  public KubernetesRuntimeState() {}

  public KubernetesRuntimeState(
      RuntimeIdentity runtimeIdentity, String namespace, WorkspaceStatus status) {
    this.runtimeId =
        new RuntimeId(
            runtimeIdentity.getWorkspaceId(),
            runtimeIdentity.getEnvName(),
            runtimeIdentity.getOwnerId());
    this.namespace = namespace;
    this.status = status;
  }

  public KubernetesRuntimeState(KubernetesRuntimeState entity) {
    this(entity.getRuntimeId(), entity.getNamespace(), entity.getStatus());
  }

  public String getNamespace() {
    return namespace;
  }

  public RuntimeId getRuntimeId() {
    return runtimeId;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KubernetesRuntimeState)) {
      return false;
    }
    final KubernetesRuntimeState that = (KubernetesRuntimeState) obj;
    return Objects.equals(runtimeId, that.runtimeId)
        && Objects.equals(namespace, that.namespace)
        && Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(runtimeId);
    hash = 31 * hash + Objects.hashCode(namespace);
    hash = 31 * hash + Objects.hashCode(status);
    return hash;
  }

  @Override
  public String toString() {
    return "KubernetesRuntimeState{"
        + "runtimeId="
        + runtimeId
        + ", namespace='"
        + namespace
        + '\''
        + ", status="
        + status
        + '}';
  }

  @Embeddable
  public static class RuntimeId implements RuntimeIdentity {

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "env_name")
    private String envName;

    @Column(name = "owner_id")
    private String ownerId;

    public RuntimeId() {}

    public RuntimeId(String workspaceId, String envName, String ownerId) {
      this.workspaceId = workspaceId;
      this.envName = envName;
      this.ownerId = ownerId;
    }

    public RuntimeId(RuntimeIdentity identity) {
      this(identity.getWorkspaceId(), identity.getEnvName(), identity.getOwnerId());
    }

    @Override
    public String getWorkspaceId() {
      return workspaceId;
    }

    @Override
    public String getEnvName() {
      return envName;
    }

    @Override
    public String getOwnerId() {
      return ownerId;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof RuntimeId)) {
        return false;
      }
      final RuntimeId that = (RuntimeId) obj;
      return Objects.equals(workspaceId, that.workspaceId)
          && Objects.equals(envName, that.envName)
          && Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + Objects.hashCode(workspaceId);
      hash = 31 * hash + Objects.hashCode(envName);
      hash = 31 * hash + Objects.hashCode(ownerId);
      return hash;
    }

    @Override
    public String toString() {
      return "RuntimeId{"
          + "workspaceId='"
          + workspaceId
          + '\''
          + ", envName='"
          + envName
          + '\''
          + ", ownerId='"
          + ownerId
          + '\''
          + '}';
    }
  }
}
