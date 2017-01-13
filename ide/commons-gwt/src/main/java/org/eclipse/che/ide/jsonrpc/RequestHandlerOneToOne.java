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
    private final Class<P>                       pClass;
    private final JsonRpcRequestBiFunction<P, R> biFunction;
    private final JsonRpcFactory                 factory;

    public RequestHandlerOneToOne(Class<P> pClass, JsonRpcRequestBiFunction<P, R> biFunction, JsonRpcFactory factory) {
        checkNotNull(pClass, "Params class must not be null");
        checkNotNull(biFunction, "Binary function must not be null");

        this.pClass = pClass;
        this.biFunction = biFunction;
        this.factory = factory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
        checkNotNull(params, "Params must not be null");

        Log.debug(getClass(), "Handling request from: " + endpointId + ", with params: " + params);

        P paramsObject = params.getAs(pClass);
        Log.debug(getClass(), "Created raw params object: " + paramsObject);
        R result = biFunction.apply(endpointId, paramsObject);
        Log.debug(getClass(), "Received result: " + result);

        return factory.createResult(result);
    }
}
