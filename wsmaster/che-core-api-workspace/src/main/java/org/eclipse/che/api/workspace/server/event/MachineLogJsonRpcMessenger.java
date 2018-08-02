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
package org.eclipse.che.api.workspace.server.event;

import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_LOG_METHOD;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;

/**
 * Register subscriber on {@link MachineLogEvent machine log event} for resending this type of event
 * via JSON-RPC to clients.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineLogJsonRpcMessenger {

  private final RemoteSubscriptionManager subscriptionManager;

  @Inject
  public MachineLogJsonRpcMessenger(RemoteSubscriptionManager subscriptionManager) {
    this.subscriptionManager = subscriptionManager;
  }

  @PostConstruct
  private void postConstruct() {
    subscriptionManager.register(MACHINE_LOG_METHOD, MachineLogEvent.class, this::predicate);
  }

  private boolean predicate(MachineLogEvent event, Map<String, String> scope) {
    return event.getRuntimeId().getWorkspaceId().equals(scope.get("workspaceId"));
  }
}
