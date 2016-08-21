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
 * When a rpc call is made, the Server MUST reply with a Response, except for in the
 * case of Notifications. The Response is expressed as a single JSON Object, with the
 * following members:
 *
 * <ul>
 * <li>jsonrpc</li>
 * A {@link String} specifying the version of the JSON-RPC protocol. MUST be
 * exactly "2.0".
 * <li>result</li>
 * This member is REQUIRED on success. This member MUST NOT exist if there was
 * an error invoking the method. The value of this member is determined by the
 * method invoked on the Server.
 * <li>error</li>
 * This member is REQUIRED on error. This member MUST NOT exist if there was no
 * error triggered during invocation.
 * <li>id</li>
 * This member is REQUIRED. It MUST be the same as the value of the id member in
 * the Request Object. If there was an error in detecting the id in the Request
 * object (e.g. Parse error/Invalid Request), it MUST be <code>null</code>.
 * </ul>
 *
 * Either the result member or error member MUST be included, but both members MUST NOT
 * be included.
 *
 * @author Dmitry Kuleshov
 * @see <a href="http://www.jsonrpc.org/specification#response_object">Response Object</a>
 */
@DTO
public interface JsonRpcResponse {

    String getJsonrpc();

    String getResult();

    JsonRpcError getError();

    Integer getId();

    JsonRpcResponse withJsonrpc(String jsonrpc);

    JsonRpcResponse withResult(String result);

    JsonRpcResponse withError(JsonRpcError error);

    JsonRpcResponse withId(Integer id);
}
