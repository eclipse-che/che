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
package org.eclipse.che.ide.jsonrpc;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry to store request and notification handlers associated with a
 * specific method names
 */
@Singleton
public class RequestHandlerRegistry {

    private final Map<String, RequestHandler>      requestHandlers      = new ConcurrentHashMap<>();
    private final Map<String, NotificationHandler> notificationHandlers = new ConcurrentHashMap<>();

    public void register(String method, RequestHandler handler) {
        requestHandlers.put(method, handler);
    }

    public void register(String method, NotificationHandler handler) {
        notificationHandlers.put(method, handler);
    }

    public void unregisterRequestHandler(String method) {
        requestHandlers.remove(method);
    }

    public void unregisterNotificationHandler(String method) {
        notificationHandlers.remove(method);
    }

    public RequestHandler getRequestHandler(String method) {
        return requestHandlers.get(method);
    }

    public NotificationHandler getNotificationHandler(String method) {
        return notificationHandlers.get(method);
    }
}
