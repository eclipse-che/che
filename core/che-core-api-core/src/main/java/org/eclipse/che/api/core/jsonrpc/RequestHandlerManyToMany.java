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
 * parameters represented by a list while result is also represented by
 * a list of items.
 *
 * @param <P>
 *         type of request params list items
 * @param <R>
 *         type of request result list items
 */
public class RequestHandlerManyToMany<P, R> implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RequestHandlerManyToMany.class);

    private final JsonRpcFactory                       factory;
    private final Class<P>                             pClass;
    private final BiFunction<String, List<P>, List<R>> function;

    public RequestHandlerManyToMany(Class<P> pClass, BiFunction<String, List<P>, List<R>> function, JsonRpcFactory factory) {
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

        List<P> paramsList = params.getAsListOf(pClass);
        LOG.debug("Created raw params list: {}", paramsList);
        List<R> resultList = function.apply(endpointId, paramsList);
        LOG.debug("Received list result: {}", resultList);

        return factory.createResult(resultList);
    }
}
