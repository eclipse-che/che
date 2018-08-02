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
package org.eclipse.che.api.system.server;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.api.system.shared.event.SystemEvent;

/**
 * Broadcasts system status events to the websocket channel.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class SystemEventsWebsocketBroadcaster {

  public static final String SYSTEM_STATE_METHOD_NAME = "system/state";

  private final RemoteSubscriptionManager remoteSubscriptionManager;

  @Inject
  public SystemEventsWebsocketBroadcaster(RemoteSubscriptionManager remoteSubscriptionManager) {
    this.remoteSubscriptionManager = remoteSubscriptionManager;
  }

  @PostConstruct
  @VisibleForTesting
  void subscribe() {
    remoteSubscriptionManager.register(
        SYSTEM_STATE_METHOD_NAME, SystemEvent.class, (systemEvent, stringStringMap) -> true);
  }
}
