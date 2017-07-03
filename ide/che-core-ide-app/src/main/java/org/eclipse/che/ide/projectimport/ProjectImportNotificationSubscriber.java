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
package org.eclipse.che.ide.projectimport;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;

import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE;

/**
 * Subscriber that register and deregister a listener for import project progress.
 *
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectImportNotificationSubscriber {

    public static final String WS_AGENT_ENDPOINT = "ws-agent";

    @Inject
    public ProjectImportNotificationSubscriber(EventBus eventBus, RequestTransmitter transmitter) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                transmitter.newRequest().endpointId(WS_AGENT_ENDPOINT).methodName(EVENT_IMPORT_OUTPUT_SUBSCRIBE).noParams()
                           .sendAndSkipResult();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                transmitter.newRequest().endpointId(WS_AGENT_ENDPOINT).methodName(EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE).noParams()
                           .sendAndSkipResult();
            }
        });
    }

}
