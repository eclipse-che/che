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
 * Transmits a JSON RPC request to an endpoint or broadcast it.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcRequestTransmitter {
    /**
     * Transmits a JSON RPC request to an endpoint
     *
     * @param request
     *         JSON RPC request instance
     * @param endpoint
     *         endpoint identifier
     */
    void transmit(JsonRpcRequest request, Integer endpoint);

    /**
     * Broadcasts a JSON RPC request to all available endpoints
     *
     * @param request
     *         JSON RPC request instance
     */
    void transmit(JsonRpcRequest request);
}
