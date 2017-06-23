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
package org.eclipse.che.ide.workspace.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.SubscriptionManagerClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * Unsubscribes from receiving notifications about changing statuses
 * of the workspace, machines and servers when workspace is stopped.
 */
@Singleton
class WorkspaceEventsUnsubscriber {

    @Inject
    WorkspaceEventsUnsubscriber(EventBus eventBus, AppContext appContext, SubscriptionManagerClient subscriptionManagerClient) {
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, e -> {
            final Map<String, String> scope = singletonMap("workspaceId", appContext.getWorkspaceId());

            subscriptionManagerClient.unSubscribe("ws-master", "workspace/statusChanged", scope);
            subscriptionManagerClient.unSubscribe("ws-master", "machine/statusChanged", scope);
            subscriptionManagerClient.unSubscribe("ws-master", "server/statusChanged", scope);
        });
    }
}
