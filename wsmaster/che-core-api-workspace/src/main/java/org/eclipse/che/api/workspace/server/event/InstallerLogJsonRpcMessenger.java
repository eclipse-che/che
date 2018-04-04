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
package org.eclipse.che.api.workspace.server.event;

import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;

/**
 * Register subscriber on {@link InstallerLogEvent installer log event} for resending this type of
 * event via JSON-RPC to clients.
 *
 * @author Anton Korneta
 */
@Singleton
public class InstallerLogJsonRpcMessenger {

  private final RemoteSubscriptionManager subscriptionManager;

  @Inject
  public InstallerLogJsonRpcMessenger(RemoteSubscriptionManager subscriptionManager) {
    this.subscriptionManager = subscriptionManager;
  }

  @PostConstruct
  private void postConstruct() {
    subscriptionManager.register(INSTALLER_LOG_METHOD, InstallerLogEvent.class, this::predicate);
  }

  private boolean predicate(InstallerLogEvent event, Map<String, String> scope) {
    return event.getRuntimeId().getWorkspaceId().equals(scope.get("workspaceId"));
  }
}
