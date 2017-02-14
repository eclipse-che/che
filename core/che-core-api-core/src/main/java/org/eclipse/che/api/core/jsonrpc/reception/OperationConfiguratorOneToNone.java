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
package org.eclipse.che.api.core.jsonrpc.reception;

import org.eclipse.che.api.core.jsonrpc.NotificationHandler;
import org.eclipse.che.api.core.jsonrpc.NotificationHandlerOneToNone;
import org.eclipse.che.api.core.jsonrpc.RequestHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Operation configurator to define an operation to be applied when we
 * handle incoming JSON RPC notification with params object that is
 * represented by a single object. As it is an operation there is no result.
 *
 * @param <P>
 *         type of params object
 */
public class OperationConfiguratorOneToNone<P> {
    private static final Logger LOG = LoggerFactory.getLogger(OperationConfiguratorOneToNone.class);

    private final RequestHandlerRegistry registry;
    private final String                 method;
    private final Class<P>               pClass;

    OperationConfiguratorOneToNone(RequestHandlerRegistry registry, String method, Class<P> pClass) {
        this.registry = registry;
        this.method = method;
        this.pClass = pClass;
    }

    public void withConsumer(BiConsumer<String, P> consumer) {
        checkNotNull(consumer, "Notification consumer must not be null");

        LOG.debug("Configuring incoming request binary consumer for method: {}, params object class: {}", method, pClass);

        NotificationHandler handler = new NotificationHandlerOneToNone<>(pClass, consumer);
        registry.register(method, handler);
    }
}
