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
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Operation configurator to define an operation to be applied when we
 * handle incoming JSON RPC notification with not params and no result value.
 */
public class ConsumerConfiguratorNoneToNone {
    private final Logger                logger;
    private final RequestHandlerManager handlerManager;

    private final String method;

    ConsumerConfiguratorNoneToNone(LoggerFactory loggerFactory, RequestHandlerManager handlerManager, String method) {
        this.logger = loggerFactory.get(getClass());
        this.handlerManager = handlerManager;

        this.method = method;
    }

    public void withConsumer(Consumer<String> consumer) {
        checkNotNull(consumer, "Notification consumer must not be null");
        logger.debug("Configuring incoming request binary: consumer for method: " + method);
        handlerManager.registerNoneToNone(method, consumer);
    }
}
