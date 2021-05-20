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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/** @author gazarenkov */
public final class RuntimeIdentityImpl implements RuntimeIdentity {

  private final String workspaceId;
  private final String envName;
  private final String ownerId;
  private final String infrastructureNamespace;

  public RuntimeIdentityImpl(RuntimeIdentity id) {
    this(id.getWorkspaceId(), id.getEnvName(), id.getOwnerId(), id.getInfrastructureNamespace());
  }

  public RuntimeIdentityImpl(
      String workspaceId, String envName, String ownerId, String infrastructureNamespace) {
    this.workspaceId = workspaceId;
    this.envName = envName;
    this.ownerId = ownerId;
    this.infrastructureNamespace = infrastructureNamespace;
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
  public String getInfrastructureNamespace() {
    return infrastructureNamespace;
  }

  @Override
  public int hashCode() {
    return Objects.hash(workspaceId, envName, ownerId, infrastructureNamespace);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RuntimeIdentityImpl)) {
      return false;
    }
    RuntimeIdentityImpl other = (RuntimeIdentityImpl) obj;
    return workspaceId.equals(other.workspaceId)
        && Objects.equals(envName, other.envName)
        && Objects.equals(ownerId, other.ownerId)
        && Objects.equals(infrastructureNamespace, other.infrastructureNamespace);
  }

  @Override
  public String toString() {
    return "RuntimeIdentityImpl{"
        + "workspaceId='"
        + workspaceId
        + '\''
        + ", envName='"
        + envName
        + '\''
        + ", ownerId='"
        + ownerId
        + '\''
        + ", infrastructureNamespace='"
        + infrastructureNamespace
        + '\''
        + '}';
  }
}
