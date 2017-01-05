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
package org.eclipse.che.api.core.jsonrpc;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Registry to store request and notification handlers associated with a
 * specific method names
 */
@Singleton
public class RequestHandlerRegistry {

    private final Map<String, RequestHandler>      requestHandlers      = new ConcurrentHashMap<>();
    private final Map<String, NotificationHandler> notificationHandlers = new ConcurrentHashMap<>();

    public void register(String method, RequestHandler handler) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");
        checkNotNull(handler, "Request handler must not be null");

        requestHandlers.put(method, handler);
    }

    public void register(String method, NotificationHandler handler) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");
        checkNotNull(handler, "Notification handler must not be null");

        notificationHandlers.put(method, handler);
    }

    public void unregisterRequestHandler(String method) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");

        requestHandlers.remove(method);
    }

    public void unregisterNotificationHandler(String method) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");

        notificationHandlers.remove(method);
    }

    public RequestHandler getRequestHandler(String method) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");

        return requestHandlers.get(method);
    }

    public NotificationHandler getNotificationHandler(String method) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");

        return notificationHandlers.get(method);
    }
}
