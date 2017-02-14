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
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;
import org.eclipse.che.ide.util.loging.Log;

import static com.google.common.base.Preconditions.checkNotNull;

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

    public <R> FunctionConfiguratorOneToMany<P, R> resultAsListOfDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result list items class: " + rClass);

        return new FunctionConfiguratorOneToMany<>(registry, jsonRpcFactory, method, paramsClass, rClass);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsDtoString() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result list items class: " +  String.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDtoDouble() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result list items class: " +  Double.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsDtoBoolean() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result list items class: " +  Boolean.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Boolean.class);
    }

    public <R> FunctionConfiguratorOneToOne<P, R> resultAsDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result object class: " + rClass);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, rClass);
    }

    public FunctionConfiguratorOneToOne<P, Void> resultAsEmpty() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result object class: " + Void.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Void.class);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsString() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result object class: " + String.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDouble() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result object class: " + Double.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsBoolean() {
        Log.debug(getClass(), "Configuring incoming request result: " +
                              "method: " + method + ", " +
                              "result object class: " + Boolean.class);

        return new FunctionConfiguratorOneToOne<>(registry, jsonRpcFactory, method, paramsClass, Boolean.class);
    }

    public OperationConfiguratorOneToNone<P> noResult()    {
        Log.debug(getClass(), "Configuring incoming request having no result");

        return new OperationConfiguratorOneToNone<>(registry, method, paramsClass);
    }
}
