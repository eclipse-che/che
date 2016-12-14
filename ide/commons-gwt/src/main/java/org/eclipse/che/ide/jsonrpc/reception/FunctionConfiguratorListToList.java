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
package org.eclipse.che.ide.jsonrpc.reception;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiFunction;
import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.jsonrpc.RequestHandlerListToList;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;

import java.util.List;

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
public class FunctionConfiguratorListToList<P, R> {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         jsonRpcFactory;
    private final String                 method;
    private final Class<P>               paramsClass;


    FunctionConfiguratorListToList(RequestHandlerRegistry registry, JsonRpcFactory jsonRpcFactory, String method, Class<P> paramsClass) {
        this.registry = registry;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
        this.paramsClass = paramsClass;
    }

    /**
     * Define a function to be applied
     *
     * @param function
     *         function
     */
    public void withFunction(JsonRpcRequestBiFunction<List<P>, List<R>> function) {
        RequestHandler handler = new RequestHandlerListToList<>(paramsClass, function, jsonRpcFactory);
        registry.register(method, handler);
    }
}
