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
package org.eclipse.che.ide.jsonrpc.reception;

import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiOperation;
import org.eclipse.che.ide.jsonrpc.NotificationHandler;
import org.eclipse.che.ide.jsonrpc.NotificationHandlerListToNone;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;

import java.util.List;

/**
 * Operation configurator to define an operation to be applied when we
 * handle incoming JSON RPC notification with params object that is
 * represented by a list. As it is an operation there is no result.
 *
 * @param <P> type of params list items
 */
public class OperationConfiguratorListToNone<P> {
    private final RequestHandlerRegistry registry;
    private final String                 method;
    private final Class<P>               paramsClass;


    OperationConfiguratorListToNone(RequestHandlerRegistry registry, String method, Class<P> paramsClass) {
        this.registry = registry;
        this.method = method;
        this.paramsClass = paramsClass;
    }

    public void withOperation(JsonRpcRequestBiOperation<List<P>> operation) {
        NotificationHandler handler = new NotificationHandlerListToNone<>(paramsClass, operation);
        registry.register(method, handler);
    }
}
