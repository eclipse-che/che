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
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Result configurator provide means to configure result type in a
 * response that is to be received. Result types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO. This configurator is used when we have no defined request params.
 */
public class ResultConfiguratorFromNone {
    private final Logger                logger;
    private final LoggerFactory         loggerFactory;
    private final RequestHandlerManager requestHandlerManager;

    private final String method;

    ResultConfiguratorFromNone(LoggerFactory loggerFactory, RequestHandlerManager requestHandlerManager, String method) {
        this.logger = loggerFactory.get(getClass());
        this.loggerFactory = loggerFactory;
        this.requestHandlerManager = requestHandlerManager;

        this.method = method;
    }

    public <R> FunctionConfiguratorNoneToMany<R> resultAsListOfDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + rClass);

        return new FunctionConfiguratorNoneToMany<>(loggerFactory, requestHandlerManager, method, rClass);
    }

    public FunctionConfiguratorNoneToMany<String> resultAsListOfString() {
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + String.class);

        return new FunctionConfiguratorNoneToMany<>(loggerFactory, requestHandlerManager, method, String.class);
    }

    public FunctionConfiguratorNoneToMany<Double> resultAsListOfDouble() {
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + Double.class);

        return new FunctionConfiguratorNoneToMany<>(loggerFactory, requestHandlerManager, method, Double.class);
    }

    public FunctionConfiguratorNoneToMany<Boolean> resultAsListOfBoolean() {
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + Boolean.class);

        return new FunctionConfiguratorNoneToMany<>(loggerFactory, requestHandlerManager, method, Boolean.class);
    }

    public <R> FunctionConfiguratorNoneToOne<R> resultAsDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + rClass);

        return new FunctionConfiguratorNoneToOne<>(loggerFactory, requestHandlerManager, method, rClass);
    }

    public FunctionConfiguratorNoneToOne<String> resultAsString() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + String.class);

        return new FunctionConfiguratorNoneToOne<>(loggerFactory, requestHandlerManager, method, String.class);
    }

    public FunctionConfiguratorNoneToOne<Double> resultAsDouble() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + Double.class);

        return new FunctionConfiguratorNoneToOne<>(loggerFactory, requestHandlerManager, method, Double.class);
    }

    public FunctionConfiguratorNoneToOne<Boolean> resultAsBoolean() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + Boolean.class);

        return new FunctionConfiguratorNoneToOne<>(loggerFactory, requestHandlerManager, method, Boolean.class);
    }

    public ConsumerConfiguratorNoneToNone noResult() {
        logger.debug("Configuring incoming request result: " + "method: " + method + ", no result is expected.");

        return new ConsumerConfiguratorNoneToNone(loggerFactory, requestHandlerManager, method);
    }
}
