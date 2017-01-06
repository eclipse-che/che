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
package org.eclipse.che.everrest;

import static org.eclipse.che.everrest.ServerContainerInitializeListener.ENVIRONMENT_CONTEXT;

import org.everrest.websockets.WSConnectionImpl;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.util.Map;

/**
 * @author Sergii Kabashniuk
 */
public class CheWSConnection extends WSConnectionImpl {
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        final Map<String, Object> userProperties = config.getUserProperties();
        setAttribute(ENVIRONMENT_CONTEXT, userProperties.get(ENVIRONMENT_CONTEXT));
        super.onOpen(session, config);

    }
}
