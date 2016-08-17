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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;

/**
 * The implementation of this interface receives JSON RPC responses according to
 * the mapping. The mapping is defined via MapBinder in one of Gin modules that
 * are used in the application. Example is simple:
 *
 * <pre>
 *     <code>
 *         GinMapBinder<String, JsonRpcRequestReceiver> responseReceivers =
 *         GinMapBinder.newMapBinder(binder(), String.class, JsonRpcRequestReceiver.class);
 *         responseReceivers.addBinding("method-name").to(CustomJsonRpcResponseReceiver.class)
 *     </code>
 * </pre>
 *
 * In fact JSON RPC responses has no method defined in their body, though it is quite
 * possible to bind requests and responses by their identifiers thus we can know which
 * response correspond to which method. So responses that has their method names equal
 * to "method-name" will be processed by instance of <code>CustomJsonRpcResponseReceiver</code>.
 * Please note that you can use regular expressions for method names in order to be able
 * to map single receiver implementation to a several kinds of requests.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcResponseReceiver {
    /**
     * Receive JSON RPC response
     *
     * @param response response instance
     */
    void receive(JsonRpcResponse response);
}
