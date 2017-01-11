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

/**
 * Simple binary operation to define mapping of JSON RPC params object
 * into JSON RPC result object while processing incoming JSON RPC notification
 *
 * @param <P>
 *         type of params
 */
public interface JsonRpcRequestBiOperation<P> {
    /**
     * Performs defined actions
     *
     * @param endpointId
     *         endpoint identifier that is associated with request being handled
     * @param params
     *         incoming notification parameters
     *
     * @throws JsonRpcException
     *         is thrown any time we meet any erroneous situation
     */
    void apply(String endpointId, P params) throws JsonRpcException;
}
