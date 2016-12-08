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
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;

public class ResultConfiguratorFromOne<P> {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         jsonRpcFactory;
    private final String                 method;
    private final Class<P>               paramsClass;

    public ResultConfiguratorFromOne(RequestHandlerRegistry registry, JsonRpcFactory jsonRpcFactory, String method,
                                     Class<P> paramsClass) {
        this.registry = registry;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
        this.paramsClass = paramsClass;
    }

    public <R> FunctionConfiguratorOneToList<P, R> resultAsListOfDto(Class<R> resultClass) {
        return new FunctionConfiguratorOneToList<>(registry, jsonRpcFactory, method, paramsClass, resultClass);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsDtoString() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDtoDouble() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsDtoBoolean() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Boolean.class);
    }

    public <R> FunctionConfiguratorOneToOne<P, R> resultAsDto(Class<R> resultClass) {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, resultClass);
    }

    public FunctionConfiguratorOneToOne<P, Void> resultAsEmpty() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Void.class);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsString() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDouble() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsBoolean() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Boolean.class);
    }

    public OperationConfiguratorOne<P> noResult() {
        return new OperationConfiguratorOne<>(registry, method, paramsClass);
    }
}
