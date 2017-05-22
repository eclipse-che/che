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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.bootstrap.WorkspaceStarter;
import org.eclipse.che.ide.util.loging.Log;

@Singleton
public class WorkspaceStatusEventHandler {

    @Inject
    WorkspaceStatusEventHandler(RequestHandlerConfigurator configurator, WorkspaceStarter workspaceStarter) {
        configurator.newConfiguration()
                    .methodName("event:workspace-status:changed")
                    .paramsAsDto(WorkspaceStatusEvent.class)
                    .noResult()
                    .withConsumer((endpointId, event) -> {
                        Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

                        workspaceStarter.checkWorkspaceStatus(event);
                    });
    }
}
