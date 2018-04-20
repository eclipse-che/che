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
package org.eclipse.che.multiuser.api.distributed;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.workspace.server.WorkspaceServiceTermination;
import org.eclipse.che.multiuser.api.distributed.cache.JGroupsWorkspaceStatusCache;
import org.eclipse.che.multiuser.api.distributed.lock.JGroupsWorkspaceLockService;
import org.eclipse.che.multiuser.api.distributed.subscription.DistributedRemoteSubscriptionStorage;

/** @author Anton Korneta */
@Singleton
public class JGroupsServiceTerminator implements ServiceTermination {

  private final JGroupsWorkspaceLockService workspaceLockService;
  private final JGroupsWorkspaceStatusCache workspaceStatusCache;
  private final DistributedRemoteSubscriptionStorage distributedRemoteSubscriptionStorage;

  @Inject
  public JGroupsServiceTerminator(
      JGroupsWorkspaceLockService workspaceLockService,
      JGroupsWorkspaceStatusCache workspaceStatusCache,
      DistributedRemoteSubscriptionStorage distributedRemoteSubscriptionStorage) {
    this.workspaceLockService = workspaceLockService;
    this.workspaceStatusCache = workspaceStatusCache;
    this.distributedRemoteSubscriptionStorage = distributedRemoteSubscriptionStorage;
  }

  @Override
  public void terminate() throws InterruptedException {
    suspend();
  }

  @Override
  public void suspend() throws InterruptedException {
    try {
      workspaceLockService.shutdown();
    } catch (RuntimeException e) {
    }

    try {
      workspaceStatusCache.shutdown();
    } catch (RuntimeException e) {
    }
    try {
      distributedRemoteSubscriptionStorage.shutdown();
    } catch (RuntimeException e) {
    }
  }

  @Override
  public String getServiceName() {
    return "ReplicationService";
  }

  @Override
  public Set<String> getDependencies() {
    return ImmutableSet.of(WorkspaceServiceTermination.SERVICE_NAME);
  }
}
