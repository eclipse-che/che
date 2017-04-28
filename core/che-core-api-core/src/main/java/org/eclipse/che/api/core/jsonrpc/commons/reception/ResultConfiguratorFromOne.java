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
 * as a single object.
 */
public class ResultConfiguratorFromOne<P> {
    private final Logger                logger;
    private final LoggerFactory         loggerFactory;
    private final RequestHandlerManager requestHandlerManager;

    private final String   method;
    private final Class<P> pClass;

    ResultConfiguratorFromOne(LoggerFactory loggerFactory, RequestHandlerManager requestHandlerManager, String method, Class<P> pClass) {
        this.logger = loggerFactory.get(getClass());
        this.loggerFactory = loggerFactory;
        this.requestHandlerManager = requestHandlerManager;

        this.method = method;
        this.pClass = pClass;
    }

    public <R> FunctionConfiguratorOneToMany<P, R> resultAsListOfDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result list items class: " + rClass);

        return new FunctionConfiguratorOneToMany<>(loggerFactory, requestHandlerManager, method, pClass, rClass);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsDtoString() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result list items class: " + String.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDtoDouble() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result list items class: " + Double.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsDtoBoolean() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result list items class: " + Boolean.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, Boolean.class);
    }

    public <R> FunctionConfiguratorOneToOne<P, R> resultAsDto(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");

        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result object class: " + rClass);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, rClass);
    }

    public FunctionConfiguratorOneToOne<P, Void> resultAsEmpty() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result object class: " + Void.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, Void.class);
    }

    public FunctionConfiguratorOneToOne<P, String> resultAsString() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result object class: " + String.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, String.class);
    }

    public FunctionConfiguratorOneToOne<P, Double> resultAsDouble() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result object class: " + Double.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, Double.class);
    }

    public FunctionConfiguratorOneToOne<P, Boolean> resultAsBoolean() {
        logger.debug("Configuring incoming request result: " +
                     "method: " + method + ", " +
                     "result object class: " + Boolean.class);

        return new FunctionConfiguratorOneToOne<>(loggerFactory, requestHandlerManager, method, pClass, Boolean.class);
    }

    public ConsumerConfiguratorOneToNone<P> noResult() {
        logger.debug("Configuring incoming request having no result");

        return new ConsumerConfiguratorOneToNone<>(loggerFactory, requestHandlerManager, method, pClass);
    }
}
