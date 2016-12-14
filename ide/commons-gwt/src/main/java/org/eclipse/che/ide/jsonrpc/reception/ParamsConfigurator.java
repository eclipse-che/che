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
 * Params configurator provide means to configure params type in a
 * request that is to be handled. Params types that are supported:
 * {@link String}, {@link Boolean}, {@link Double}, {@link Void} and
 * DTO.
 */
public class ParamsConfigurator {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         jsonRpcFactory;
    private final String                 method;

    public ParamsConfigurator(RequestHandlerRegistry registry, JsonRpcFactory jsonRpcFactory, String method) {
        this.registry = registry;
        this.jsonRpcFactory = jsonRpcFactory;
        this.method = method;
    }

    public <P> ResultConfiguratorFromList<P> paramsAsListOfDto(Class<P> paramsClass) {
        return new ResultConfiguratorFromList<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public ResultConfiguratorFromList<Double> paramsAsListOfDouble() {
        return new ResultConfiguratorFromList<>(registry, jsonRpcFactory, method, Double.class);
    }

    public ResultConfiguratorFromList<String> paramsAsListOfString() {
        return new ResultConfiguratorFromList<>(registry, jsonRpcFactory, method, String.class);
    }

    public ResultConfiguratorFromList<Boolean> paramsAsListOfBoolean() {
        return new ResultConfiguratorFromList<>(registry, jsonRpcFactory, method, Boolean.class);
    }

    public <P> ResultConfiguratorFromOne<P> paramsAsDto(Class<P> paramsClass) {
        return new ResultConfiguratorFromOne<>(registry, jsonRpcFactory, method, paramsClass);
    }

    public ResultConfiguratorFromOne<Void> paramsAsEmpty() {
        return new ResultConfiguratorFromOne<>(registry, jsonRpcFactory, method, Void.class);
    }

    public ResultConfiguratorFromOne<String> paramsAsString() {
        return new ResultConfiguratorFromOne<>(registry, jsonRpcFactory, method, String.class);
    }

    public ResultConfiguratorFromOne<Double> paramsAsDouble() {
        return new ResultConfiguratorFromOne<>(registry, jsonRpcFactory, method, Double.class);
    }

    public ResultConfiguratorFromOne<Double> paramsAsBoolean() {
        return new ResultConfiguratorFromOne<>(registry, jsonRpcFactory, method, Double.class);
    }
}
