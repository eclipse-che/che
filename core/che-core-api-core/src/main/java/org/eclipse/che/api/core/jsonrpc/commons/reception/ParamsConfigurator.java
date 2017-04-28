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
 * Params configurator provide means to configure params type in a
 * request that is to be handled. Params types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO.
 */
public class ParamsConfigurator {
    private final Logger                logger;
    private final LoggerFactory         loggerFactory;
    private final RequestHandlerManager requestHandlerManager;

    private final String method;

    ParamsConfigurator(LoggerFactory loggerFactory, RequestHandlerManager requestHandlerManager, String method) {
        this.logger = loggerFactory.get(getClass());
        this.loggerFactory = loggerFactory;
        this.requestHandlerManager = requestHandlerManager;

        this.method = method;
    }

    public <P> ResultConfiguratorFromMany<P> paramsAsListOfDto(Class<P> pClass) {
        checkNotNull(pClass, "Params class must not be null");

        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params list items class: " + pClass);

        return new ResultConfiguratorFromMany<>(loggerFactory, requestHandlerManager, method, pClass);
    }

    public ResultConfiguratorFromMany<Double> paramsAsListOfDouble() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params list items class: " + Double.class);

        return new ResultConfiguratorFromMany<>(loggerFactory, requestHandlerManager, method, Double.class);
    }

    public ResultConfiguratorFromMany<String> paramsAsListOfString() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params list items class: " + String.class);

        return new ResultConfiguratorFromMany<>(loggerFactory, requestHandlerManager, method, String.class);
    }

    public ResultConfiguratorFromMany<Boolean> paramsAsListOfBoolean() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params list items class: " + Boolean.class);

        return new ResultConfiguratorFromMany<>(loggerFactory, requestHandlerManager, method, Boolean.class);
    }

    public <P> ResultConfiguratorFromOne<P> paramsAsDto(Class<P> pClass) {
        checkNotNull(pClass, "Params class must not be null");

        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params object class: " + pClass);

        return new ResultConfiguratorFromOne<>(loggerFactory, requestHandlerManager, method, pClass);
    }

    public ResultConfiguratorFromNone noParams() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params object class: " + Void.class);

        return new ResultConfiguratorFromNone(loggerFactory, requestHandlerManager, method);
    }

    public ResultConfiguratorFromOne<String> paramsAsString() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params object class: " + String.class);

        return new ResultConfiguratorFromOne<>(loggerFactory, requestHandlerManager, method, String.class);
    }

    public ResultConfiguratorFromOne<Double> paramsAsDouble() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params object class: " + Double.class);

        return new ResultConfiguratorFromOne<>(loggerFactory, requestHandlerManager, method, Double.class);
    }

    public ResultConfiguratorFromOne<Boolean> paramsAsBoolean() {
        logger.debug("Configuring incoming request params: " +
                     "method: " + method + ", " +
                     "params object class: " + Boolean.class);

        return new ResultConfiguratorFromOne<>(loggerFactory, requestHandlerManager, method, Boolean.class);
    }
}
