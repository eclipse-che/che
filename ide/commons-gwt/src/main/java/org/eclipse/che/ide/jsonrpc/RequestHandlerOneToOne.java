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
    private final Class<P>                       paramsClass;
    private final JsonRpcRequestBiFunction<P, R> biFunction;
    private final JsonRpcFactory                 jsonRpcFactory;

    public RequestHandlerOneToOne(Class<P> paramsClass, JsonRpcRequestBiFunction<P, R> biFunction, JsonRpcFactory jsonRpcFactory) {

        this.paramsClass = paramsClass;
        this.biFunction = biFunction;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        R result = biFunction.apply(endpointId, params.getAs(paramsClass));
        return jsonRpcFactory.createResult(result);
    }
}
