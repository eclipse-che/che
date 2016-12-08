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
import org.eclipse.che.ide.jsonrpc.RequestHandlerListToOne;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;

import java.util.List;

public class FunctionConfiguratorListToOne<P, R> {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         jsonRpcFactory;
    private final String                 method;
    private final Class<P>               paramsClass;
    private final Class<R>               resultClass;

    FunctionConfiguratorListToOne(RequestHandlerRegistry registry, JsonRpcFactory jsonRpcFactory, String method, Class<P> paramsClass,
                                  Class<R> resultClass) {
        this.registry = registry;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
        this.paramsClass = paramsClass;
        this.resultClass = resultClass;
    }

    public void withFunction(JsonRpcRequestBiFunction<List<P>, R> function) {
        RequestHandler handler = new RequestHandlerListToOne<>(paramsClass, resultClass, function, jsonRpcFactory);
        registry.register(method, handler);
    }
}
