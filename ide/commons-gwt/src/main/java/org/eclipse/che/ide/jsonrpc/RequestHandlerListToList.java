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
package org.eclipse.che.ide.jsonrpc;

import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

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
public class RequestHandlerListToList<P, R> implements RequestHandler {
    private final JsonRpcFactory                             jsonRpcFactory;

    private final Class<P>                                   paramsClass;
    private final JsonRpcRequestBiFunction<List<P>, List<R>> biFunction;

    public RequestHandlerListToList(Class<P> pClass, JsonRpcRequestBiFunction<List<P>, List<R>> biFunction, JsonRpcFactory factory) {
        checkNotNull(pClass, "Params class must not be null");
        checkNotNull(biFunction, "Binary function must not be null");

        this.paramsClass = pClass;
        this.biFunction = biFunction;
        this.jsonRpcFactory = factory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
        checkNotNull(params, "Params must not be null");

        Log.debug(getClass(), "Handling request from: " + endpointId + ", with params: " + params);

        List<P> paramsList = params.getAsListOf(paramsClass);
        Log.debug(getClass(), "Created raw params list: " + paramsList);
        List<R> resultList = biFunction.apply(endpointId, paramsList);
        Log.debug(getClass(), "Received list result: " + resultList);

        return jsonRpcFactory.createResult(resultList);
    }
}
