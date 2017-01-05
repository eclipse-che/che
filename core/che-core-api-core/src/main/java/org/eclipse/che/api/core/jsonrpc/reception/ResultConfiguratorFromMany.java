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
import org.eclipse.che.api.core.jsonrpc.RequestHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Result configurator provide means to configure result type in a
 * response that is to be received. Result types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO. This configurator is used when we have defined request params
 * as a list.
 */
public class ResultConfiguratorFromMany<P> {
    private static final Logger LOG = LoggerFactory.getLogger(ResultConfiguratorFromMany.class);

    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         factory;
    private final String                 method;
    private final Class<P>               pClass;

    public ResultConfiguratorFromMany(RequestHandlerRegistry registry, JsonRpcFactory factory, String method, Class<P> pClass) {
        this.registry = registry;
        this.factory = factory;
        this.method = method;
        this.pClass = pClass;
    }

    public <R> FunctionConfiguratorManyToMany<P, R> resultAsListOfDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + rClass);

        return new FunctionConfiguratorManyToMany<>(registry, factory, method, pClass, rClass);
    }

    public FunctionConfiguratorManyToMany<P, String> resultAsListOfString() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + String.class);

        return new FunctionConfiguratorManyToMany<>(registry, factory, method, pClass, String.class);
    }

    public FunctionConfiguratorManyToMany<P, Double> resultAsListOfDouble() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + Double.class);

        return new FunctionConfiguratorManyToMany<>(registry, factory, method, pClass, Double.class);
    }

    public FunctionConfiguratorManyToMany<P, Boolean> resultAsListOfBoolean() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + Boolean.class);

        return new FunctionConfiguratorManyToMany<>(registry, factory, method, pClass, Boolean.class);
    }

    public <R> FunctionConfiguratorManyToOne<P, R> resultAsDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + rClass);

        return new FunctionConfiguratorManyToOne<>(registry, factory, method, pClass, rClass);
    }

    public FunctionConfiguratorManyToOne<P, Void> resultAsEmpty() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + Void.class);

        return new FunctionConfiguratorManyToOne<>(registry, factory, method, pClass, Void.class);
    }

    public FunctionConfiguratorManyToOne<P, String> resultAsString() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + String.class);

        return new FunctionConfiguratorManyToOne<>(registry, factory, method, pClass, String.class);
    }

    public FunctionConfiguratorManyToOne<P, Double> resultAsDouble() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + Double.class);

        return new FunctionConfiguratorManyToOne<>(registry, factory, method, pClass, Double.class);
    }

    public FunctionConfiguratorManyToOne<P, Boolean> resultAsBoolean() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + Boolean.class);

        return new FunctionConfiguratorManyToOne<>(registry, factory, method, pClass, Boolean.class);
    }

    public OperationConfiguratorManyToNone<P> noResult() {
        LOG.debug("Configuring incoming request having no result");

        return new OperationConfiguratorManyToNone<>(registry, method, pClass);
    }
}
