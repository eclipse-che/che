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

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Function configurator to define a function to be applied when we
 * handle incoming JSON RPC request with no params object while the
 * result of a function is a single object.
 *
 * @param <R>
 *         type of result object
 */
public class FunctionConfiguratorNoneToOne<R> {
    private final Logger                logger;
    private final RequestHandlerManager handlerManager;

    private final String                 method;
    private final Class<R>               rClass;

    FunctionConfiguratorNoneToOne(LoggerFactory loggerFactory, RequestHandlerManager handlerManager, String method, Class<R> rClass) {
        this.logger = loggerFactory.get(getClass());
        this.handlerManager = handlerManager;

        this.method = method;
        this.rClass = rClass;
    }

    /**
     * Define a function to be applied
     *
     * @param function
     *         function
     */
    public void withFunction(Function<String, R> function) {
        checkNotNull(function, "Request function must not be null");

        logger.debug("Configuring incoming request binary: " +
                     "function for method: " + method + ", " +
                     "result object class: " + rClass);


        handlerManager.registerNoneToOne(method, rClass, function);
    }
}
