/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Sets.newConcurrentHashSet;

/**
 * Send workspace events using JSON RPC to the clients
 */
@Singleton
public class ServerStatusJsonRpcMessenger {
    private final RemoteSubscriptionManager remoteSubscriptionManager;

    @Inject
    public ServerStatusJsonRpcMessenger(RemoteSubscriptionManager remoteSubscriptionManager) {
        this.remoteSubscriptionManager = remoteSubscriptionManager;
    }

    @PostConstruct
    private void postConstruct() {
        remoteSubscriptionManager.register("server/statusChanged", ServerStatusEvent.class, this::predicate);
    }

    private boolean predicate(ServerStatusEvent event, Map<String, String> scope) {
        return event.getIdentity().getWorkspaceId().equals(scope.get("workspaceId"));
    }
}
