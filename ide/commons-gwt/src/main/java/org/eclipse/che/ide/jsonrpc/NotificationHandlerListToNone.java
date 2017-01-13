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
 * Handler to contain an operation and all related metadata required for
 * processing incoming notification. This handler is used when we have
 * parameters represented by a list.
 *
 * @param <P>
 *         type of notification params list items
 */
public class NotificationHandlerListToNone<P> implements NotificationHandler {
    private final Class<P>                           paramsClass;
    private final JsonRpcRequestBiOperation<List<P>> biOperation;

    public NotificationHandlerListToNone(Class<P> pClass, JsonRpcRequestBiOperation<List<P>> biOperation) {
        checkNotNull(pClass, "Params class must not be null");
        checkNotNull(biOperation, "Binary operation must not be null");

        this.paramsClass = pClass;
        this.biOperation = biOperation;
    }

    public void handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
        checkNotNull(params, "Params must not be null");

        Log.debug(getClass(), "Handling notification from: " + endpointId + ", with list params: " + params);

        biOperation.apply(endpointId, params.getAsListOf(paramsClass));
    }
}
