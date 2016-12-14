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
 * as a list.
 */
public class ResultConfiguratorFromList<P> {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         jsonRpcFactory;
    private final String                 method;
    private final Class<P>               paramsClass;

    public ResultConfiguratorFromList(RequestHandlerRegistry registry,
                                      JsonRpcFactory jsonRpcFactory,
                                      String method,
                                      Class<P> paramsClass) {
        this.registry = registry;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
        this.paramsClass = paramsClass;
    }

    public <R> FunctionConfiguratorListToList<P, R> resultAsListOfDto(Class<R> resultClass) {
        return new FunctionConfiguratorListToList<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToList<P, String> resultAsListOfString() {
        return new FunctionConfiguratorListToList<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToList<P, Double> resultAsListOfDouble() {
        return new FunctionConfiguratorListToList<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToList<P, Boolean> resultAsListOfBoolean() {
        return new FunctionConfiguratorListToList<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public <R> FunctionConfiguratorListToOne<P, R> resultAsDto(Class<R> resultClass) {
        return new FunctionConfiguratorListToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToOne<P, Void> resultAsEmpty() {
        return new FunctionConfiguratorListToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToOne<P, String> resultAsString() {
        return new FunctionConfiguratorListToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToOne<P, Double> resultAsDouble() {
        return new FunctionConfiguratorListToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public FunctionConfiguratorListToOne<P, Boolean> resultAsBoolean() {
        return new FunctionConfiguratorListToOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public OperationConfiguratorListToNone<P> noResult() {
        return new OperationConfiguratorListToNone<>(registry, method, paramsClass);
    }
}
