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
package org.eclipse.che.workspace.infrastructure.kubernetes.event;

import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer;

/**
 * Should published via {@link EventService} when workspace becomes {@link
 * WorkspaceStatus#STOPPING}.
 *
 * <p>Note that is not published by default. It is needed for {@link StartSynchronizer} to
 * synchronize state in cluster mode.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesRuntimeStoppingEvent {

  private final String workspaceId;

  public KubernetesRuntimeStoppingEvent(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KubernetesRuntimeStoppingEvent)) {
      return false;
    }
    final KubernetesRuntimeStoppingEvent that = (KubernetesRuntimeStoppingEvent) obj;
    return Objects.equals(workspaceId, that.workspaceId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(workspaceId);
  }

  @Override
  public String toString() {
    return "KubernetesRuntimeStoppingEvent{" + "workspaceId='" + workspaceId + '\'' + '}';
  }
}
