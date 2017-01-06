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
 * as a single object.
 */
public class ResultConfiguratorFromOne<P> {
    private static final Logger LOG = LoggerFactory.getLogger(ResultConfiguratorFromMany.class);

    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         factory;
    private final String                 method;
    private final Class<P>               pClass;

    ResultConfiguratorFromOne(RequestHandlerRegistry registry, JsonRpcFactory factory, String method, Class<P> pClass) {
        this.registry = registry;
        this.factory = factory;
        this.method = method;
        this.pClass = pClass;
    }

    public <R> FunctionConfiguratorOneToMany<P, R> resultAsListOfDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + rClass);

        return new FunctionConfiguratorOneToMany<>(registry, factory, method, pClass, rClass);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsDtoString() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + String.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDtoDouble() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + Double.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsDtoBoolean() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result list items class: " + Boolean.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, Boolean.class);
    }

    public <R> FunctionConfiguratorOneToOne<P, R> resultAsDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + rClass);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, rClass);
    }

    public FunctionConfiguratorOneToOne<P, Void> resultAsEmpty() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + Void.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, Void.class);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsString() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + String.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDouble() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + Double.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsBoolean() {
        LOG.debug("Configuring incoming request result: " +
                  "method: " + method + ", " +
                  "result object class: " + Boolean.class);

        return new FunctionConfiguratorOneToOne<>(registry, factory, method, pClass, Boolean.class);
    }

    public OperationConfiguratorOneToNone<P> noResult() {
        LOG.debug("Configuring incoming request having no result");

        return new OperationConfiguratorOneToNone<>(registry, method, pClass);
    }
}
