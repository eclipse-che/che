/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.machine.execagent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ConnectedEventDto;
import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Handles 'connected' event, the event is fired when we firstly connect to exec agent.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ConnectedEventHandler extends RequestHandler<ConnectedEventDto, Void> {

    @Inject
    protected ConnectedEventHandler() {
        super(ConnectedEventDto.class, Void.class);
    }

    @Override
    public void handleNotification(String endpointId, ConnectedEventDto params) {
        Log.debug(getClass(), "Handling channel connected event. Params: " + params);
    }
}
