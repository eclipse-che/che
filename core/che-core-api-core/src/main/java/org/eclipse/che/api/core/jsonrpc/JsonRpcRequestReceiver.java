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
package org.eclipse.che.api.core.jsonrpc;


import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;

/**
 * The implementation of this interface receives JSON RPC requests according to
 * the mapping. The mapping is defined via MapBinder in one of Guice modules that
 * are used in the application. Example is simple:
 *
 * <pre>
 *     <code>
 *         MapBinder<String, JsonRpcRequestReceiver> requestReceivers =
 *         MapBinder.newMapBinder(binder(), String.class, JsonRpcRequestReceiver.class);
 *         requestReceivers.addBinding("method-name").to(CustomJsonRpcRequestReceiver.class)
 *     </code>
 * </pre>
 *
 * All JSON RPC requests that has their method names equal to "method-name" will be
 * processed by instance of <code>CustomJsonRpcRequestReceiver</code>. Please note
 * that you can use regular expressions for method names in order to be able to map
 * single receiver implementation to a several kinds of requests.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcRequestReceiver {
    /**
     * Receives a JSON RPC request from an endpoint
     *
     * @param request
     *         request instance
     * @param endpoint
     *         endpoint identifier
     */
    void receive(JsonRpcRequest request, Integer endpoint);
}
