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
package org.eclipse.che.ide.jsonrpc.reception;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiFunction;
import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.jsonrpc.RequestHandlerOneToList;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Function configurator to define a function to be applied when we
 * handle incoming JSON RPC request with params object that is
 * represented by a single object while the result of a function is a
 * list of objects.
 *
 * @param <P>
 *         type of params object
 * @param <R>
 *         type of result list items
 */
public class FunctionConfiguratorOneToMany<P, R> {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         factory;
    private final String                 method;
    private final Class<P>               pClass;
    private final Class<R>               rClass;

    FunctionConfiguratorOneToMany(RequestHandlerRegistry registry, JsonRpcFactory factory, String method, Class<P> pClass,
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
    public void withFunction(JsonRpcRequestBiFunction<P, List<R>> function) {
        checkNotNull(function, "Request function must not be null");

        Log.debug(getClass(), "Configuring incoming request binary function for " +
                              "method: " + method + ", " +
                              "params object class: " + pClass + ", " +
                              "result list items class: " + rClass);

        RequestHandler handler = new RequestHandlerOneToList<>(pClass, function, factory);
        registry.register(method, handler);
    }
}
