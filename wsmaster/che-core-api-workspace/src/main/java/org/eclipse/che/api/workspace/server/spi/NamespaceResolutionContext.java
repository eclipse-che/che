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
package org.eclipse.che.api.workspace.server.spi;

import java.util.Objects;
import org.eclipse.che.commons.subject.Subject;

/**
 * Holds information needed for resolving placeholders in the namespace name. The {@code
 * persistAfterCreate} attribute indicates whether namespace name should be persisted after
 * resolution (if the infrastructure supports it).
 *
 * @author Lukas Krejci
 * @author Sergii Leshchenko
 */
public class NamespaceResolutionContext {
  private final String workspaceId;
  private final String userId;
  private final String userName;

  public NamespaceResolutionContext(Subject subject) {
    this(null, subject.getUserId(), subject.getUserName());
  }

  public NamespaceResolutionContext(String workspaceId, String userId, String userName) {
    this.workspaceId = workspaceId;
    this.userId = userId;
    this.userName = userName;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NamespaceResolutionContext)) {
      return false;
    }
    final NamespaceResolutionContext that = (NamespaceResolutionContext) obj;
    return Objects.equals(workspaceId, that.workspaceId)
        && Objects.equals(userId, that.userId)
        && Objects.equals(userName, that.userName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workspaceId, userId, userName);
  }

  @Override
  public String toString() {
    return "NamespaceResolutionContext{"
        + "workspaceId='"
        + workspaceId
        + '\''
        + ", userId='"
        + userId
        + '\''
        + ", userName='"
        + userName
        + '\''
        + '}';
  }
}
