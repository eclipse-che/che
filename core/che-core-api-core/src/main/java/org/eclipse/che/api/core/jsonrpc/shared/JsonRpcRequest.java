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
package org.eclipse.che.api.core.jsonrpc.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * A rpc call is represented by sending a Request object to a Server. The Request object
 * has the following members:
 *
 * <ul>
 * <li>jsonrpc</li>
 * A String specifying the version of the JSON-RPC protocol. MUST be exactly "2.0".
 * <li>method</li>
 * A String containing the name of the method to be invoked. Method names that begin with
 * the word rpc followed by a period character (U+002E or ASCII 46) are reserved for rpc-
 * internal methods and extensions and MUST NOT be used for anything else.
 * <li>params</li>
 * A Structured value that holds the parameter values to be used during the invocation of
 * the method. This member MAY be omitted.
 * <li>id</li>
 * An identifier established by the Client that MUST contain a String, Number, or NULL
 * value if included. If it is not included it is assumed to be a notification. The value
 * SHOULD normally not be <code>null</code> and Numbers SHOULD NOT contain fractional parts
 * </ul>
 *
 * @author Dmitry Kuleshov
 * @see <a href="http://www.jsonrpc.org/specification#request_object">Request Object</a>
 */
@DTO
public interface JsonRpcRequest {

    Integer getId();

    JsonRpcRequest withId(Integer id);

    String getJsonrpc();

    String getMethod();

    String getParams();

    JsonRpcRequest withJsonrpc(String jsonrpc);

    JsonRpcRequest withMethod(String method);

    JsonRpcRequest withParams(String params);
}
