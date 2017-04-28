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
public class ConsumerConfiguratorManyToNone<P> {
    private final Logger                logger;
    private final RequestHandlerManager handlerManager;

    private final String                 method;
    private final Class<P>               pClass;

    ConsumerConfiguratorManyToNone(LoggerFactory loggerFactory, RequestHandlerManager handlerManager, String method, Class<P> pClass) {
        this.logger = loggerFactory.get(getClass());
        this.handlerManager = handlerManager;

        this.method = method;
        this.pClass = pClass;
    }

    public void withConsumer(BiConsumer<String, List<P>> biConsumer) {
        checkNotNull(biConsumer, "Notification consumer must not be null");
        logger.debug("Configuring incoming request: " +
                     "binary consumer for method: " + method + ", " +
                     "params list items class: " + pClass);

        handlerManager.registerManyToNone(method, pClass, biConsumer);
    }
}
