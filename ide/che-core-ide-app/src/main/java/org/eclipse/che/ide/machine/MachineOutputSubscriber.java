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
package org.eclipse.che.ide.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.MachineStartingEvent;

// TODO (spi ide): rework according to the new messages format
/** Subscribes to the output of each started machine. */
@Singleton
class MachineOutputSubscriber {

    @Inject
    MachineOutputSubscriber(EventBus eventBus, RequestTransmitter transmitter, AppContext appContext) {
        final String endpointId = "ws-master";
        final String subscribeByName = "event:environment-output:subscribe-by-machine-name";

        eventBus.addHandler(MachineStartingEvent.TYPE, event -> {
            final String workspaceIdPlusMachineName = appContext.getWorkspaceId() + "::" + event.getMachine().getName();

            transmitter.newRequest()
                       .endpointId(endpointId)
                       .methodName(subscribeByName)
                       .paramsAsString(workspaceIdPlusMachineName)
                       .sendAndSkipResult();
        });
    }
}
