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
 * Simple binary function to define mapping of JSON RPC params object
 * into JSON RPC result object while processing incoming JSON RPC request
 *
 * @param <P>
 *         type of params
 * @param <R>
 *         type of result
 */
public interface JsonRpcRequestBiFunction<P, R> {
    /**
     * Performs defined actions
     *
     * @param endpointId
     *         endpoint identifier that is associated with request being handled
     * @param params
     *         incoming request parameters
     *
     * @return outcoming result
     *
     * @throws JsonRpcException
     *         is thrown any time we meet any erroneous situation
     */
    R apply(String endpointId, P params) throws JsonRpcException;
}
