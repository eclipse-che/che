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
package org.eclipse.che.ide.jsonrpc;

import java.util.List;

/**
 * Handler to contain a function and all related metadata required for
 * processing incoming requests. This handler is used when we have
 * parameters represented by a list while result is represented by
 * a single object.
 *
 * @param <P>
 *         type of request params list items
 * @param <R>
 *         type of request result object
 */
public class RequestHandlerListToOne<P, R> implements RequestHandler {
    private final Class<P>                             paramsClass;
    private final JsonRpcRequestBiFunction<List<P>, R> biFunction;
    private final JsonRpcFactory                       jsonRpcFactory;

    public RequestHandlerListToOne(Class<P> paramsClass, JsonRpcRequestBiFunction<List<P>, R> biFunction, JsonRpcFactory jsonRpcFactory) {

        this.paramsClass = paramsClass;
        this.biFunction = biFunction;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        R result = biFunction.apply(endpointId, params.getAsListOf(paramsClass));
        return jsonRpcFactory.createResult(result);
    }
}
