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


import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;

/**
 * Transmits a JSON RPC response to an endpoint or broadcast it.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcResponseTransmitter {

    /**
     * Transmits a JSON RPC response to an endpoint
     *
     * @param response
     *         JSON RPC response instance
     * @param endpoint
     *         endpoint identifier
     */
    void transmit(JsonRpcResponse response, Integer endpoint);

    /**
     * Broadcasts a JSON RPC response to all endpoints
     *
     * @param response
     *         JSON RPC response instance
     */
    void transmit(JsonRpcResponse response);
}
