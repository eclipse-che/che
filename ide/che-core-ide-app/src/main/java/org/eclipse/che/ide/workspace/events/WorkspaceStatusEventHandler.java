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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceStatusHandler;

/**
 * Receives notifications about changing workspace's status.
 * After a notification is received it is processed and
 * an appropriate event is fired on the {@link EventBus}.
 */
@Singleton
class WorkspaceStatusEventHandler {

    @Inject
    WorkspaceStatusEventHandler(RequestHandlerConfigurator configurator, Provider<WorkspaceStatusHandler> handlerProvider) {
        configurator.newConfiguration()
                    .methodName("workspace/statusChanged")
                    .paramsAsDto(WorkspaceStatusEvent.class)
                    .noResult()
                    .withBiConsumer((endpointId, event) -> {
                        Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

                        // Since WorkspaceStatusEventHandler instantiated by GIN eagerly,
                        // it may be really expensive to instantiate WorkspaceStatusHandler with all it's dependencies.
                        // So defer that work.
                        handlerProvider.get().handleWorkspaceStatusChanged(event);
                    });
    }
}
