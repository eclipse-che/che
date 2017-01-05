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
package org.eclipse.che.ide.jsonrpc.reception;

import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiOperation;
import org.eclipse.che.ide.jsonrpc.NotificationHandler;
import org.eclipse.che.ide.jsonrpc.NotificationHandlerListToNone;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Operation configurator to define an operation to be applied when we
 * handle incoming JSON RPC notification with params object that is
 * represented by a list. As it is an operation there is no result.
 *
 * @param <P> type of params list items
 */
public class OperationConfiguratorManyToNone<P> {
    private final RequestHandlerRegistry registry;
    private final String                 method;
    private final Class<P>               pClass;


    OperationConfiguratorManyToNone(RequestHandlerRegistry registry, String method, Class<P> pClass) {
        this.registry = registry;
        this.method = method;
        this.pClass = pClass;
    }

    public void withOperation(JsonRpcRequestBiOperation<List<P>> operation) {
        checkNotNull(operation, "Notification operation must not be null");

        Log.debug(getClass(), "Configuring incoming request binary operation for " +
                              "method: " + method + ", " +
                              "params list items class: " + pClass);

        NotificationHandler handler = new NotificationHandlerListToNone<>(pClass, operation);
        registry.register(method, handler);
    }
}
