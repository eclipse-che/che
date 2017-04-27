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

import java.util.List;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handler to contain a function and all related metadata required for
 * processing incoming requests. This handler is used when we have
 * parameters represented by a single object while result is represented by
 * a list of items.
 *
 * @param <P>
 *         type of request params object
 * @param <R>
 *         type of request result list items
 */
public class RequestHandlerOneToMany<P, R> implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RequestHandlerManyToOne.class);

    private final Class<P>                       pClass;
    private final BiFunction<String, P, List<R>> function;
    private final JsonRpcFactory                 factory;

    public RequestHandlerOneToMany(Class<P> pClass, BiFunction<String, P, List<R>> function, JsonRpcFactory factory) {
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
        List<R> resultList = function.apply(endpointId, paramsObject);
        LOG.debug("Received result list: {}", resultList);

        return factory.createResult(resultList);
    }
}
