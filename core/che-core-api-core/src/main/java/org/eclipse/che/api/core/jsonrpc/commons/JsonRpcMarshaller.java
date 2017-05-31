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
package org.eclipse.che.api.core.jsonrpc.commons;

/**
 * Marshals outgoing JSON RPC requests and responses to a string
 * representation, used to separate the business logic and the platform/parser
 * specific logic that is responsible for parsing/composing json rpc entities.
 */
public interface JsonRpcMarshaller {
    /**
     * Serializes JSON RPC response object into a string
     *
     * @param response
     *         response
     * @return string representation
     */
    String marshall(JsonRpcResponse response);

    /**
     * Serializes JSON RPC request object into a string
     *
     * @param request
     *         request
     * @return string representation
     */
    String marshall(JsonRpcRequest request);
}
