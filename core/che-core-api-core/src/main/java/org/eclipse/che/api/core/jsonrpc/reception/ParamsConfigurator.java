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
 * Params configurator provide means to configure params type in a
 * request that is to be handled. Params types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO.
 */
public class ParamsConfigurator {
    private static final Logger LOG = LoggerFactory.getLogger(ParamsConfigurator.class);

    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         factory;
    private final String                 method;

    ParamsConfigurator(RequestHandlerRegistry registry, JsonRpcFactory factory, String method) {
        this.registry = registry;
        this.factory = factory;
        this.method = method;
    }

    public <P> ResultConfiguratorFromMany<P> paramsAsListOfDto(Class<P> pClass) {
        checkNotNull(pClass, "Params class must not be null");

        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params list items class: " + pClass);

        return new ResultConfiguratorFromMany<>(registry, factory, method, pClass);
    }

    public ResultConfiguratorFromMany<Double> paramsAsListOfDouble() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params list items class: " + Double.class);

        return new ResultConfiguratorFromMany<>(registry, factory, method, Double.class);
    }

    public ResultConfiguratorFromMany<String> paramsAsListOfString() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params list items class: " + String.class);

        return new ResultConfiguratorFromMany<>(registry, factory, method, String.class);
    }

    public ResultConfiguratorFromMany<Boolean> paramsAsListOfBoolean() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params list items class: " + Boolean.class);

        return new ResultConfiguratorFromMany<>(registry, factory, method, Boolean.class);
    }

    public <P> ResultConfiguratorFromOne<P> paramsAsDto(Class<P> pClass) {
        checkNotNull(pClass, "Params class must not be null");

        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params object class: " + pClass);

        return new ResultConfiguratorFromOne<>(registry, factory, method, pClass);
    }

    public ResultConfiguratorFromOne<Void> paramsAsEmpty() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params object class: " + Void.class);

        return new ResultConfiguratorFromOne<>(registry, factory, method, Void.class);
    }

    public ResultConfiguratorFromOne<String> paramsAsString() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params object class: " + String.class);

        return new ResultConfiguratorFromOne<>(registry, factory, method, String.class);
    }

    public ResultConfiguratorFromOne<Double> paramsAsDouble() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params object class: " + Double.class);

        return new ResultConfiguratorFromOne<>(registry, factory, method, Double.class);
    }

    public ResultConfiguratorFromOne<Boolean> paramsAsBoolean() {
        LOG.debug("Configuring incoming request params: " +
                              "method: " + method + ", " +
                              "params object class: " + Boolean.class);

        return new ResultConfiguratorFromOne<>(registry, factory, method, Boolean.class);
    }
}
