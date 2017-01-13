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

import com.google.inject.assistedinject.Assisted;

import java.util.List;

/**
 * Factory that should be used to create all JSON RPC related entities
 */
public interface JsonRpcFactory {
    /**
     * Create a JSON RPC request instance from a stringified json
     *
     * @param message
     *         stringified json
     *
     * @return JSON RPC request
     */
    JsonRpcRequest createRequest(@Assisted("message") String message);

    /**
     * Create a JSON RPC request instance by passing corresponding values
     *
     * @param id
     *         request identifier
     * @param method
     *         request method
     * @param params
     *         request params
     *
     * @return JSON RPC request
     */
    JsonRpcRequest createRequest(@Assisted("id") String id, @Assisted("method") String method, @Assisted("params") JsonRpcParams params);

    /**
     * Create a JSON RPC request instance by passing corresponding values
     *
     * @param method
     *         request method
     * @param params
     *         request params
     *
     * @return JSON RPC request
     */
    JsonRpcRequest createRequest(@Assisted("method") String method, @Assisted("params") JsonRpcParams params);

    /**
     * Create a JSON RPC response instance from a stringified json
     *
     * @param message
     *         stringified json
     *
     * @return JSON RPC response
     */
    JsonRpcResponse createResponse(@Assisted("message") String message);

    /**
     * Create a JSON RPC response instance by passing corresponding values
     *
     * @param id
     *         response identifier
     * @param result
     *         response result - should be null if error happened
     * @param error
     *         response error - should be null if result is present
     *
     * @return JSON RPC response
     */
    JsonRpcResponse createResponse(@Assisted("id") String id, @Assisted("result") JsonRpcResult result,
                                   @Assisted("error") JsonRpcError error);

    /**
     * Create a JSON RPC error instance by passing corresponding values
     *
     * @param code
     *         error code
     * @param message
     *         error message
     *
     * @return JSON RPC error
     */
    JsonRpcError createError(@Assisted("code") int code, @Assisted("message") String message);

    /**
     * Create a JSON RPC error instance from a stringified json
     *
     * @param message
     *         stringified json
     *
     * @return JSON RPC error
     */
    JsonRpcError createError(@Assisted("message") String message);

    /**
     * Create a JSON RPC result instance from a stringified json. Result can be
     * either a list of objects or a single object.
     *
     * @param message
     *         stringified json
     *
     * @return JSON RPC result
     */
    JsonRpcResult createResult(@Assisted("message") String message);

    /**
     * Create a JSON RPC result as a single object by passing corresponding
     * values.
     *
     * @param result
     *         result object
     *
     * @return JSON RPC result
     */
    JsonRpcResult createResult(@Assisted("result") Object result);

    /**
     * Create a JSON RPC result as a list of objects by passing corresponding
     * values.
     *
     * @param result
     *         result list
     *
     * @return JSON RPC result
     */
    JsonRpcResult createResultList(@Assisted("result") List<?> result);

    /**
     * Create a JSON RPC list instance from a stringified json
     *
     * @param message
     *         stringified json
     *
     * @return JSON RPC list
     */
    JsonRpcList createList(@Assisted("message") String message);

    /**
     * Create a JSON RPC list ba passing a corresponding value
     *
     * @param dtoObjectList
     *         list
     *
     * @return JSON RPC list
     */
    JsonRpcList createList(@Assisted("dtoObjectList") List<?> dtoObjectList);

    /**
     * Create a JSON RPC params instance from a stringified json. Params can be
     * either a list of objects or a single object.
     *
     * @param message
     *         stringified json
     *
     * @return JSON RPC params
     */
    JsonRpcParams createParams(@Assisted("message") String message);

    /**
     * Create a JSON RPC params instance by passing corresponding values.
     * Params should be represented by a single object.
     *
     * @param params
     *         params object
     *
     * @return JSON RPC params
     */
    JsonRpcParams createParams(@Assisted("params") Object params);

    /**
     * Create a JSON RPC params instance by passing corresponding values.
     * Params should be represented by a list of objects.
     *
     * @param params
     *         params list
     *
     * @return JSON RPC params
     */
    JsonRpcParams createParamsList(@Assisted("params") List<?> params);
}
