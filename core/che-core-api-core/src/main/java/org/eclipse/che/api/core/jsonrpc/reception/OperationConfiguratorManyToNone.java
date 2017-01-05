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
import org.eclipse.che.api.core.jsonrpc.NotificationHandlerManyToNone;
import org.eclipse.che.api.core.jsonrpc.RequestHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Operation configurator to define an operation to be applied when we
 * handle incoming JSON RPC notification with params object that is
 * represented by a list. As it is an operation there is no result.
 *
 * @param <P>
 *         type of params list items
 */
public class OperationConfiguratorManyToNone<P> {
    private static final Logger LOG = LoggerFactory.getLogger(OperationConfiguratorManyToNone.class);

    private final RequestHandlerRegistry registry;
    private final String                 method;
    private final Class<P>               pClass;


    OperationConfiguratorManyToNone(RequestHandlerRegistry registry, String method, Class<P> pClass) {
        this.registry = registry;
        this.method = method;
        this.pClass = pClass;
    }

    public void withConsumer(BiConsumer<String, List<P>> consumer) {
        checkNotNull(consumer, "Notification consumer must not be null");

        LOG.debug("Configuring incoming request binary consumer for method: {}, params list items class: {}", method, pClass);

        NotificationHandler handler = new NotificationHandlerManyToNone<>(pClass, consumer);
        registry.register(method, handler);
    }
}
