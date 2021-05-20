/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.distributed;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.multiuser.api.distributed.cache.JGroupsWorkspaceStatusCache;
import org.eclipse.che.multiuser.api.distributed.cache.StatusChangeListener;
import org.eclipse.che.workspace.infrastructure.kubernetes.event.KubernetesRuntimeStoppedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.event.KubernetesRuntimeStoppingEvent;

/**
 * Propagate workspace status changes.
 *
 * <p>It's needed to synchronize interrupt/stop operations between Che Servers instances. There can
 * be two Che Servers in case of Rolling Update.
 *
 * @author Sergii Leshchenko
 * @see org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer
 */
@Singleton
public class WorkspaceStopPropagator implements StatusChangeListener {

  private final EventService eventService;

  @Inject
  public WorkspaceStopPropagator(
      EventService eventService, JGroupsWorkspaceStatusCache statusCache) {
    this.eventService = eventService;
    statusCache.subscribe(this);
  }

  @Override
  public void statusChanged(String workspaceId, WorkspaceStatus status) {
    switch (status) {
      case STOPPING:
        // is supposed to notify start synchronizer that workspace is stopping
        // so, if runtime is starting then start will be interrupted
        eventService.publish(new KubernetesRuntimeStoppingEvent(workspaceId));
        break;
      case STOPPED:
        // is supposed to notify start synchronizer that workspace is stopped
        // so, if runtime start was interrupting then interruption will be considered as finished
        eventService.publish(new KubernetesRuntimeStoppedEvent(workspaceId));
        break;
      default:
        // there is no needed to publish other statuses
    }
  }
}
