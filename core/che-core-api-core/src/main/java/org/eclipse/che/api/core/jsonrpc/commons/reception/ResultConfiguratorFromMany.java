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
 * DTO. This configurator is used when we have defined request params
 * as a list.
 */
public class ResultConfiguratorFromMany<P> {
    private final Logger                logger;
    private final LoggerFactory         loggerFactory;
    private final RequestHandlerManager requestHandlerManager;

    private final String   method;
    private final Class<P> pClass;

    ResultConfiguratorFromMany(LoggerFactory loggerFactory, RequestHandlerManager requestHandlerManager, String method,
                                      Class<P> pClass) {
        this.logger = loggerFactory.get(getClass());
        this.loggerFactory = loggerFactory;
        this.requestHandlerManager = requestHandlerManager;

        this.method = method;
        this.pClass = pClass;
    }

    public <R> FunctionConfiguratorManyToMany<P, R> resultAsListOfDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + rClass);

        return new FunctionConfiguratorManyToMany<>(loggerFactory, requestHandlerManager, method, pClass, rClass);
    }

    public FunctionConfiguratorManyToMany<P, String> resultAsListOfString() {
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + String.class);

        return new FunctionConfiguratorManyToMany<>(loggerFactory, requestHandlerManager, method, pClass, String.class);
    }

    public FunctionConfiguratorManyToMany<P, Double> resultAsListOfDouble() {
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + Double.class);

        return new FunctionConfiguratorManyToMany<>(loggerFactory, requestHandlerManager, method, pClass, Double.class);
    }

    public FunctionConfiguratorManyToMany<P, Boolean> resultAsListOfBoolean() {
        logger.debug("Configuring incoming request result: method: " + method + ", result list items class: " + Boolean.class);

        return new FunctionConfiguratorManyToMany<>(loggerFactory, requestHandlerManager, method, pClass, Boolean.class);
    }

    public <R> FunctionConfiguratorManyToOne<P, R> resultAsDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + rClass);

        return new FunctionConfiguratorManyToOne<>(loggerFactory, requestHandlerManager, method, pClass, rClass);
    }

    public ConsumerConfiguratorManyToNone<P> resultAsEmpty() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + Void.class);

        return new ConsumerConfiguratorManyToNone<>(loggerFactory, requestHandlerManager, method, pClass);
    }

    public FunctionConfiguratorManyToOne<P, String> resultAsString() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + String.class);

        return new FunctionConfiguratorManyToOne<>(loggerFactory, requestHandlerManager, method, pClass, String.class);
    }

    public FunctionConfiguratorManyToOne<P, Double> resultAsDouble() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + Double.class);

        return new FunctionConfiguratorManyToOne<>(loggerFactory, requestHandlerManager, method, pClass, Double.class);
    }

    public FunctionConfiguratorManyToOne<P, Boolean> resultAsBoolean() {
        logger.debug("Configuring incoming request result: method: " + method + ", result object class: " + Double.class);

        return new FunctionConfiguratorManyToOne<>(loggerFactory, requestHandlerManager, method, pClass, Boolean.class);
    }

    public ConsumerConfiguratorManyToNone<P> noResult() {
        logger.debug("Configuring incoming request having no result");

        return new ConsumerConfiguratorManyToNone<>(loggerFactory, requestHandlerManager, method, pClass);
    }
}
