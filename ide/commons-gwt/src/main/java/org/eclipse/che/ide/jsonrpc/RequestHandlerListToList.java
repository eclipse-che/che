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

public class RequestHandlerListToList<P, R> implements RequestHandler {
    private final Class<P>                                   paramsClass;
    private final Class<R>                                   resultClass;
    private final JsonRpcRequestBiFunction<List<P>, List<R>> biFunction;
    private final JsonRpcFactory                             jsonRpcFactory;

    public RequestHandlerListToList(Class<P> paramsClass, Class<R> resultClass,
                                    JsonRpcRequestBiFunction<List<P>, List<R>> biFunction,
                                    JsonRpcFactory jsonRpcFactory) {

        this.paramsClass = paramsClass;
        this.resultClass = resultClass;
        this.biFunction = biFunction;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public JsonRpcResult handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        List<R> resultList = biFunction.apply(endpointId, params.getAsListOf(paramsClass));
        return jsonRpcFactory.createResult(resultList);
    }
}
