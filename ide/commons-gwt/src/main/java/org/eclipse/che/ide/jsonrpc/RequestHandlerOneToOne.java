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

public class RequestHandlerOneToOne<P, R> implements RequestHandler {
    private final Class<P>                       paramsClass;
    private final Class<R>                       resultClass;
    private final JsonRpcRequestBiFunction<P, R> biFunction;
    private final JsonRpcFactory                 jsonRpcFactory;

    public RequestHandlerOneToOne(Class<P> paramsClass, Class<R> resultClass, JsonRpcRequestBiFunction<P, R> biFunction,
                                  JsonRpcFactory jsonRpcFactory) {

        this.paramsClass = paramsClass;
        this.resultClass = resultClass;
        this.biFunction = biFunction;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        R result = biFunction.apply(endpointId, params.getAs(paramsClass));
        return jsonRpcFactory.createResult(result);
    }
}
