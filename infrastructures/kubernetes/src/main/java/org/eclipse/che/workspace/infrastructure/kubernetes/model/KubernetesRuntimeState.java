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
package org.eclipse.che.workspace.infrastructure.kubernetes.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
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
  @Enumerated(EnumType.STRING)
  private WorkspaceStatus status;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id")
  private List<KubernetesRuntimeCommandImpl> commands;

  public KubernetesRuntimeState() {}

  public KubernetesRuntimeState(
      RuntimeIdentity runtimeIdentity, WorkspaceStatus status, List<? extends Command> commands) {
    this.envName = runtimeIdentity.getEnvName();
    this.workspaceId = runtimeIdentity.getWorkspaceId();
    this.ownerId = runtimeIdentity.getOwnerId();
    this.namespace = runtimeIdentity.getInfrastructureNamespace();
    this.status = status;
    if (commands != null) {
      this.commands =
          commands.stream().map(KubernetesRuntimeCommandImpl::new).collect(Collectors.toList());
    }
  }

  public KubernetesRuntimeState(KubernetesRuntimeState entity) {
    this(entity.getRuntimeId(), entity.getStatus(), entity.getCommands());
  }

  public String getNamespace() {
    return namespace;
  }

  public RuntimeIdentity getRuntimeId() {
    return new RuntimeIdentityImpl(workspaceId, envName, ownerId, namespace);
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

  public List<KubernetesRuntimeCommandImpl> getCommands() {
    return commands;
  }

  public void setCommands(List<KubernetesRuntimeCommandImpl> commands) {
    this.commands = commands;
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
        && Objects.equals(namespace, that.namespace)
        && getStatus() == that.getStatus();
  }

  @Override
  public int hashCode() {
    return Objects.hash(workspaceId, envName, ownerId, namespace, getStatus());
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
