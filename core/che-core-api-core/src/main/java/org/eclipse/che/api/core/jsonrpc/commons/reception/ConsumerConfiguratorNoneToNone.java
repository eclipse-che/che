/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Operation configurator to define an operation to be applied when we
 * handle incoming JSON RPC notification with not params and no result value.
 */
public class ConsumerConfiguratorNoneToNone {
    private final static Logger LOGGER = getLogger(ConsumerConfiguratorNoneToNone.class);

    private final RequestHandlerManager handlerManager;

    private final String method;

    ConsumerConfiguratorNoneToNone(RequestHandlerManager handlerManager, String method) {
        this.handlerManager = handlerManager;

        this.method = method;
    }

    public void withConsumer(Consumer<String> consumer) {
        checkNotNull(consumer, "Notification consumer must not be null");
        LOGGER.debug("Configuring incoming request binary: consumer for method: " + method);
        handlerManager.registerNoneToNone(method, consumer);
    }
}
