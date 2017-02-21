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

import org.eclipse.che.api.core.jsonrpc.JsonRpcFactory;
import org.eclipse.che.api.core.jsonrpc.RequestHandler;
import org.eclipse.che.api.core.jsonrpc.RequestHandlerManyToMany;
import org.eclipse.che.api.core.jsonrpc.RequestHandlerRegistry;
import org.eclipse.che.api.core.jsonrpc.transmission.EndpointIdConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Function configurator to define a function to be applied when we
 * handle incoming JSON RPC request with params object that is
 * represented by a list. The result of a function is also a list.
 *
 * @param <P>
 *         type of params list items
 * @param <R>
 *         type of resulting list items
 */
public class FunctionConfiguratorManyToMany<P, R> {
    private static final Logger LOG = LoggerFactory.getLogger(FunctionConfiguratorManyToMany.class);

    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         factory;
    private final String                 method;
    private final Class<P>               pClass;
    private final Class<R>               rClass;


    FunctionConfiguratorManyToMany(RequestHandlerRegistry registry, JsonRpcFactory factory, String method, Class<P> pClass,
                                   Class<R> rClass) {
        this.registry = registry;
        this.factory = factory;
        this.method = method;
        this.pClass = pClass;
        this.rClass = rClass;
    }

    /**
     * Define a function to be applied
     *
     * @param function
     *         function
     */
    public void withFunction(BiFunction<String, List<P>, List<R>> function) {
        checkNotNull(function, "Request function must not be null");

        LOG.debug("Configuring incoming request binary function for method: {}, params list items class:  {}, result list items class:  {}",
                  method, pClass, rClass);

        RequestHandler handler = new RequestHandlerManyToMany<>(pClass, function, factory);
        registry.register(method, handler);
    }
}
