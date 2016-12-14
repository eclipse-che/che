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

    public NotificationHandlerListToNone(Class<P> paramsClass, JsonRpcRequestBiOperation<List<P>> biOperation) {
        this.paramsClass = paramsClass;
        this.biOperation = biOperation;
    }

    public void handle(String endpointId, JsonRpcParams params) throws JsonRpcException {
        biOperation.apply(endpointId, params.getAsListOf(paramsClass));
    }
}
