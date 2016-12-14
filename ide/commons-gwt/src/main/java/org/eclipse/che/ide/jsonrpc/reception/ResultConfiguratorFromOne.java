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

/**
 * Result configurator provide means to configure result type in a
 * response that is to be received. Result types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO. This configurator is used when we have defined request params
 * as a single object.
 */
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
        return new FunctionConfiguratorOneToList<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsDtoString() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDtoDouble() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsDtoBoolean() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public <R> FunctionConfiguratorOneToOne<P, R> resultAsDto(Class<R> resultClass) {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, Void> resultAsEmpty() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsString() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDouble() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsBoolean() {
        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public OperationConfiguratorOneToNone<P> noResult() {
        return new OperationConfiguratorOneToNone<>(registry, method, paramsClass);
    }
}
