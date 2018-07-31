/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/** @author gazarenkov */
public final class RuntimeIdentityImpl implements RuntimeIdentity {

  private final String workspaceId;
  private final String envName;
  private final String ownerId;

  public RuntimeIdentityImpl(String workspaceId, String envName, String ownerId) {
    this.workspaceId = workspaceId;
    this.envName = envName;
    this.ownerId = ownerId;
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
  public int hashCode() {
    return (workspaceId + envName).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RuntimeIdentityImpl)) return false;
    RuntimeIdentityImpl other = (RuntimeIdentityImpl) obj;
    return workspaceId.equals(other.workspaceId) && envName.equals(other.envName);
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
        + '}';
  }
}
