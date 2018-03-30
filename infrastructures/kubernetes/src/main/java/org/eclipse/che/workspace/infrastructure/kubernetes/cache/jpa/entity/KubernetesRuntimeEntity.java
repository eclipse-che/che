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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesRuntime")
@Table(name = "che_k8s_runtime")
public class KubernetesRuntimeEntity {
  @EmbeddedId private Id runtimeId;

  @Column(name = "namespace")
  private String namespace;

  @Column(name = "status")
  private WorkspaceStatus status;

  public KubernetesRuntimeEntity() {}

  public KubernetesRuntimeEntity(Id runtimeId, String namespace, WorkspaceStatus status) {
    this.runtimeId = runtimeId;
    this.namespace = namespace;
    this.status = status;
  }

  public KubernetesRuntimeEntity(KubernetesRuntimeEntity entity) {
    this(entity.getRuntimeId(), entity.getNamespace(), entity.getStatus());
  }

  public String getNamespace() {
    return namespace;
  }

  public Id getRuntimeId() {
    return runtimeId;
  }

  public WorkspaceStatus getStatus() {
    return status;
  }

  public void setStatus(WorkspaceStatus status) {
    this.status = status;
  }

  public KubernetesRuntimeEntity withStatus(WorkspaceStatus status) {
    this.status = status;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KubernetesRuntimeEntity)) {
      return false;
    }
    KubernetesRuntimeEntity that = (KubernetesRuntimeEntity) o;
    return Objects.equals(getRuntimeId(), that.getRuntimeId())
        && Objects.equals(getNamespace(), that.getNamespace())
        && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRuntimeId(), getNamespace(), getStatus());
  }

  @Override
  public String toString() {
    return "KubernetesRuntimeEntity{"
        + "runtimeId="
        + runtimeId
        + ", namespace='"
        + namespace
        + '\''
        + ", status="
        + status
        + '}';
  }

  /** @author Sergii Leshchenko */
  @Embeddable
  public static class Id implements RuntimeIdentity {

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "env_name")
    private String envName;

    @Column(name = "owner_id")
    private String ownerId;

    public Id() {}

    public Id(String workspaceId, String envName, String ownerId) {
      this.workspaceId = workspaceId;
      this.envName = envName;
      this.ownerId = ownerId;
    }

    public Id(RuntimeIdentity identity) {
      this(identity.getWorkspaceId(), identity.getEnvName(), identity.getOwnerId());
    }

    @Override
    public String getWorkspaceId() {
      return workspaceId;
    }

    public Id setWorkspaceId(String workspaceId) {
      this.workspaceId = workspaceId;
      return this;
    }

    @Override
    public String getEnvName() {
      return envName;
    }

    public Id setEnvName(String envName) {
      this.envName = envName;
      return this;
    }

    @Override
    public String getOwnerId() {
      return ownerId;
    }

    public Id setOwnerId(String ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Id)) {
        return false;
      }
      final Id that = (Id) obj;
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
      return "Id{"
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
