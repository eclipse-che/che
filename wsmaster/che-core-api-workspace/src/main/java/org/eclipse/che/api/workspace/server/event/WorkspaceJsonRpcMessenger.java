/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.event;

import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STATUS_CHANGED_METHOD;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

/** Send workspace events using JSON RPC to the clients */
@Singleton
public class WorkspaceJsonRpcMessenger {
  private final RemoteSubscriptionManager remoteSubscriptionManager;

  @Inject
  public WorkspaceJsonRpcMessenger(RemoteSubscriptionManager remoteSubscriptionManager) {
    this.remoteSubscriptionManager = remoteSubscriptionManager;
  }

  @PostConstruct
  private void postConstruct() {
    remoteSubscriptionManager.register(
        WORKSPACE_STATUS_CHANGED_METHOD, WorkspaceStatusEvent.class, this::predicate);
  }

  private boolean predicate(WorkspaceStatusEvent event, Map<String, String> scope) {
    return event.getWorkspaceId().equals(scope.get("workspaceId"));
  }
}
