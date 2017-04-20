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
package org.eclipse.che.api.core.jsonrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handler to contain a function and all related metadata required for
 * processing incoming requests. This handler is used when we have
 * parameters represented by a single object while result is also represented
 * by a single object
 *
 * @param <P>
 *         type of request params object
 * @param <R>
 *         type of request result object
 */
public class RequestHandlerOneToOne<P, R> implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RequestHandlerManyToOne.class);

    private final Class<P>                 pClass;
    private final BiFunction<String, P, R> function;
    private final JsonRpcFactory           factory;

    public RequestHandlerOneToOne(Class<P> pClass, BiFunction<String, P, R> function, JsonRpcFactory factory) {
        checkNotNull(pClass, "Params class must not be null");
        checkNotNull(function, "Binary function must not be null");

        this.pClass = pClass;
        this.function = function;
        this.factory = factory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
        checkNotNull(params, "Params must not be null");

        LOG.debug("Handling request from: {}, with params: {}", endpointId, params);

        P paramsObject = params.getAs(pClass);
        LOG.debug("Created raw params object: {}", paramsObject);
        R result = function.apply(endpointId, paramsObject);
        LOG.debug("Received result: {}", result);

        return factory.createResult(result);
    }
}
