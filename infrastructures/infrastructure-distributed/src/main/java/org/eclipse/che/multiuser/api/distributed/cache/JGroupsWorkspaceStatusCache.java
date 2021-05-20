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
package org.eclipse.che.multiuser.api.distributed.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceStatusCache;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JGroups based implementation of {@link WorkspaceStatusCache}.
 *
 * @author Anton Korneta
 */
@Singleton
public class JGroupsWorkspaceStatusCache implements WorkspaceStatusCache {

  private static final String CHANNEL_NAME = "WorkspaceStateCache";
  private static final Logger LOG = LoggerFactory.getLogger(JGroupsWorkspaceStatusCache.class);

  private final ReplicatedHashMap<String, WorkspaceStatus> delegate;

  @Inject
  public JGroupsWorkspaceStatusCache(@Named("jgroups.config.file") String confFile) {
    try {
      JChannel channel = new JChannel(confFile).connect(CHANNEL_NAME);
      delegate = new ReplicatedHashMap<>(channel);
      delegate.setBlockingUpdates(true);
      delegate.start(5000);
    } catch (Exception ex) {
      throw new RuntimeException("Jgroups cache creation failed. Cause :" + ex.getMessage());
    }
  }

  @Override
  public WorkspaceStatus get(String workspaceId) {
    return delegate.get(workspaceId);
  }

  @Override
  public WorkspaceStatus replace(String workspaceId, WorkspaceStatus newStatus) {
    return delegate.replace(workspaceId, newStatus);
  }

  @Override
  public boolean replace(
      String workspaceId, WorkspaceStatus prevStatus, WorkspaceStatus newStatus) {
    return delegate.replace(workspaceId, prevStatus, newStatus);
  }

  @Override
  public WorkspaceStatus remove(String workspaceId) {
    return delegate.remove(workspaceId);
  }

  @Override
  public WorkspaceStatus putIfAbsent(String workspaceId, WorkspaceStatus status) {
    return delegate.putIfAbsent(workspaceId, status);
  }

  @Override
  public Map<String, WorkspaceStatus> asMap() {
    return new HashMap<>(delegate);
  }

  /**
   * Subscribes status changes listener.
   *
   * @param listener listener instance that will receive status changed events
   */
  public void subscribe(StatusChangeListener listener) {
    delegate.addNotifier(
        new ReplicatedMapNotificationAdapter() {
          @Override
          public void entrySet(Object workspaceId, Object workspaceStatus) {
            listener.statusChanged((String) workspaceId, (WorkspaceStatus) workspaceStatus);
          }

          @Override
          public void entryRemoved(Object workspaceId) {
            listener.statusChanged((String) workspaceId, WorkspaceStatus.STOPPED);
          }
        });
  }

  /** Stops workspace status cache. */
  public void shutdown() {
    try {
      delegate.close();
    } catch (IOException | RuntimeException ex) {
      LOG.error("Failed to stop workspace status cache. Cause: " + ex.getMessage());
    }
  }
}
