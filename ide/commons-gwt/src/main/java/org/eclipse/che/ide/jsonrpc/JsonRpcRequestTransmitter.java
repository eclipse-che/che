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


import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;

/**
 * Transmits a JSON RPC request.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcRequestTransmitter {
    /**
     * Transmits a JSON RPC request.
     *
     * @param request JSON RPC request instance
     */
    void transmit(JsonRpcRequest request);
}
